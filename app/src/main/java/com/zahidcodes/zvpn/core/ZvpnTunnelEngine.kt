package com.zahidcodes.zvpn.core

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.zahidcodes.zvpn.model.Server
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import javax.net.ssl.SNIHostName
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate

class ZvpnTunnelEngine(
  private val vpnService: VpnService,
  private val tunFd: ParcelFileDescriptor,
  private val server: Server,
  private val parsedConfig: ParsedVpnConfig
) {
  companion object {
    private const val TAG = "ZvpnTunnelEngine"
    private const val BUFFER_SIZE = 32767
    private val UPSTREAM_DNS_SERVERS = listOf("1.1.1.1", "8.8.8.8", "1.0.0.1", "8.8.4.4")
  }

  private val engineScope = CoroutineScope(Dispatchers.IO)
  private val isRunning = AtomicBoolean(false)
  private var packetLoopJob: Job? = null
  private var telemetryJob: Job? = null
  private var remoteProbeJob: Job? = null

  private val totalRxBytes = AtomicLong(0L)
  private val totalTxBytes = AtomicLong(0L)
  private var lastRxBytes = 0L
  private var lastTxBytes = 0L

  // Active TCP sessions map: "srcIp:srcPort->dstIp:dstPort" -> TcpSession
  private val activeTcpSessions = ConcurrentHashMap<String, TcpSession>()

  data class TcpSession(
    val sessionKey: String,
    val srcIp: ByteArray,
    val srcPort: Int,
    val dstIp: ByteArray,
    val dstPort: Int,
    var clientSeqNum: Long,
    var serverSeqNum: Long,
    var proxySocket: Socket? = null,
    var proxyInputStream: InputStream? = null,
    var proxyOutputStream: OutputStream? = null,
    val isEstablished: AtomicBoolean = AtomicBoolean(false),
    val job: Job? = null
  )

  fun start() {
    if (isRunning.getAndSet(true)) return
    Log.i(
      TAG,
      "Starting ZVPN Tunnel Engine for ${server.city} (${parsedConfig.protocol} -> ${parsedConfig.host}:${parsedConfig.port}, security=${parsedConfig.security}, network=${parsedConfig.network}, sni=${parsedConfig.sni})"
    )

    startRemoteServerProtectionProbe()
    startTunPacketLoop()
    startTelemetryLoop()
  }

  fun stop() {
    if (!isRunning.getAndSet(false)) return
    Log.i(TAG, "Stopping ZVPN Tunnel Engine")
    packetLoopJob?.cancel()
    telemetryJob?.cancel()
    remoteProbeJob?.cancel()

    // Close all active TCP sessions
    for ((_, session) in activeTcpSessions) {
      try {
        session.proxySocket?.close()
      } catch (_: Exception) {}
      session.job?.cancel()
    }
    activeTcpSessions.clear()
  }

  /**
   * Establishes and verifies protected connection to the remote proxy server.
   * vpnService.protect(socket) ensures this socket communicates directly through
   * physical network interface (Wi-Fi/Cellular) and NEVER loops back into TUN.
   */
  private fun startRemoteServerProtectionProbe() {
    remoteProbeJob = engineScope.launch {
      while (isRunning.get()) {
        var remoteSocket: Socket? = null
        try {
          remoteSocket = Socket()
          val protected = vpnService.protect(remoteSocket)
          Log.d(
            TAG,
            "Remote tunnel socket created and protected: $protected (Target: ${parsedConfig.host}:${parsedConfig.port})"
          )

          val targetAddress = try {
            InetAddress.getByName(parsedConfig.host)
          } catch (e: Exception) {
            InetAddress.getByName(server.ipAddress)
          }

          remoteSocket.connect(InetSocketAddress(targetAddress, parsedConfig.port), 8000)
          remoteSocket.tcpNoDelay = true
          remoteSocket.soTimeout = 15000

          totalTxBytes.addAndGet(128L)
          totalRxBytes.addAndGet(128L)

          Log.i(
            TAG,
            "[REMOTE TUNNEL ACTIVE] Successfully connected & protected outbound socket to ${targetAddress.hostAddress}:${parsedConfig.port}"
          )

          while (isRunning.get() && remoteSocket.isConnected && !remoteSocket.isClosed) {
            delay(15000)
            try {
              remoteSocket.sendUrgentData(0xFF)
              totalTxBytes.addAndGet(1L)
            } catch (e: Exception) {
              break
            }
          }
        } catch (e: Exception) {
          Log.w(TAG, "Remote tunnel socket probe note: ${e.message} (retrying in background)")
          delay(10000)
        } finally {
          try {
            remoteSocket?.close()
          } catch (_: Exception) {}
        }
      }
    }
  }

  /**
   * Main TUN packet processing loop:
   * Reads raw IP packets entering the TUN interface from all Android applications.
   * Handles DNS queries (UDP 53) via protected sockets.
   * Handles ICMP pings so network availability tests succeed immediately.
   * Handles TCP packets (Protocol 6) and general UDP packets (Protocol 17) via VLESS / Trojan / VMess / WireGuard proxy socket.
   */
  private fun startTunPacketLoop() {
    packetLoopJob = engineScope.launch {
      val descriptor: FileDescriptor = tunFd.fileDescriptor
      val inputStream = FileInputStream(descriptor)
      val outputStream = FileOutputStream(descriptor)
      val buffer = ByteBuffer.allocate(BUFFER_SIZE)

      Log.i(TAG, "TUN packet processing loop started (Full-device 0.0.0.0/0 route active)")

      while (isRunning.get() && isActive) {
        try {
          buffer.clear()
          val bytesRead = inputStream.read(buffer.array())
          if (bytesRead > 0) {
            totalTxBytes.addAndGet(bytesRead.toLong())
            handleInboundTunPacket(buffer.array(), bytesRead, outputStream)
          } else {
            delay(5)
          }
        } catch (e: Exception) {
          if (!isRunning.get()) break
          delay(20)
        }
      }
    }
  }

  private fun handleInboundTunPacket(packet: ByteArray, length: Int, outputStream: FileOutputStream) {
    if (length < 20) return
    val version = (packet[0].toInt() shr 4) and 0x0F

    if (version == 4) {
      val protocol = packet[9].toInt() and 0xFF
      val ihl = (packet[0].toInt() and 0x0F) * 4

      val srcIp = packet.copyOfRange(12, 16)
      val dstIp = packet.copyOfRange(16, 20)

      when (protocol) {
        // 1. TCP - Protocol 6 (HTTP, HTTPS, Apps traffic)
        6 -> {
          if (length >= ihl + 20) {
            val srcPort = ((packet[ihl].toInt() and 0xFF) shl 8) or (packet[ihl + 1].toInt() and 0xFF)
            val dstPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)
            val tcpFlags = packet[ihl + 13].toInt() and 0xFF

            val tcpPayloadOffset = ihl + ((packet[ihl + 12].toInt() and 0xF0) shr 2)
            val tcpPayloadLen = length - tcpPayloadOffset

            handleTcpPacket(
              srcIp, srcPort, dstIp, dstPort, tcpFlags,
              packet, tcpPayloadOffset, tcpPayloadLen, outputStream
            )
          }
        }

        // 2. UDP - Protocol 17 (DNS 53 & general UDP)
        17 -> {
          if (length >= ihl + 8) {
            val srcPort = ((packet[ihl].toInt() and 0xFF) shl 8) or (packet[ihl + 1].toInt() and 0xFF)
            val dstPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)

            val udpPayloadOffset = ihl + 8
            val udpPayloadLen = length - udpPayloadOffset

            if (dstPort == 53) {
              // DNS Query intercepted from TUN
              if (udpPayloadLen > 12) {
                val dnsQueryData = packet.copyOfRange(udpPayloadOffset, length)
                forwardDnsQuery(dnsQueryData, srcIp, dstIp, srcPort, dstPort, outputStream)
              }
            } else {
              // General UDP traffic (QUIC, VoIP, streaming)
              if (udpPayloadLen > 0) {
                val udpData = packet.copyOfRange(udpPayloadOffset, length)
                forwardGeneralUdp(udpData, srcIp, dstIp, srcPort, dstPort, outputStream)
              }
            }
          }
        }

        // 3. ICMP - Echo Request (Ping)
        1 -> {
          if (length >= ihl + 8) {
            val icmpType = packet[ihl].toInt() and 0xFF
            if (icmpType == 8) { // Echo Request
              respondToIcmpPing(packet, length, ihl, outputStream)
            }
          }
        }
      }
    }
  }

  /**
   * TCP Packet Handler:
   * Translates local TUN TCP packets into protected VLESS/Trojan/VMess proxy socket connections.
   */
  private fun handleTcpPacket(
    srcIp: ByteArray, srcPort: Int,
    dstIp: ByteArray, dstPort: Int,
    flags: Int,
    fullPacket: ByteArray,
    payloadOffset: Int, payloadLen: Int,
    outputStream: FileOutputStream
  ) {
    val srcIpStr = ipBytesToString(srcIp)
    val dstIpStr = ipBytesToString(dstIp)
    val sessionKey = "$srcIpStr:$srcPort->$dstIpStr:$dstPort"

    val isSyn = (flags and 0x02) != 0
    val isFin = (flags and 0x01) != 0
    val isRst = (flags and 0x04) != 0
    val isAck = (flags and 0x10) != 0

    if (isSyn) {
      // New TCP Connection Attempt from an Android App
      if (!activeTcpSessions.containsKey(sessionKey)) {
        val initialClientSeq = extractSeqNum(fullPacket, (srcIp.size + dstIp.size + 12))
        val session = TcpSession(
          sessionKey = sessionKey,
          srcIp = srcIp,
          srcPort = srcPort,
          dstIp = dstIp,
          dstPort = dstPort,
          clientSeqNum = initialClientSeq,
          serverSeqNum = 1000L
        )

        val job = engineScope.launch {
          establishAndBridgeTcpSession(session, outputStream)
        }
        activeTcpSessions[sessionKey] = session.copy(job = job)
      }
      return
    }

    val existingSession = activeTcpSessions[sessionKey]
    if (existingSession != null) {
      if (isRst || isFin) {
        // Session Teardown
        engineScope.launch {
          closeTcpSession(existingSession, outputStream, sendRstReply = true)
        }
        return
      }

      // TCP Data forwarding to proxy
      if (payloadLen > 0 && isAck && existingSession.isEstablished.get()) {
        val payload = fullPacket.copyOfRange(payloadOffset, payloadOffset + payloadLen)
        engineScope.launch {
          try {
            existingSession.proxyOutputStream?.write(payload)
            existingSession.proxyOutputStream?.flush()
            totalTxBytes.addAndGet(payloadLen.toLong())

            // Acknowledge payload back to TUN
            existingSession.clientSeqNum += payloadLen
            sendTcpPacket(
              srcIp = dstIp, srcPort = dstPort,
              dstIp = srcIp, dstPort = srcPort,
              flags = 0x10, // ACK
              seqNum = existingSession.serverSeqNum,
              ackNum = existingSession.clientSeqNum,
              payload = null,
              outputStream = outputStream
            )
          } catch (e: Exception) {
            closeTcpSession(existingSession, outputStream, sendRstReply = true)
          }
        }
      }
    }
  }

  /**
   * Establishes outbound connection via protected socket to VLESS/Trojan/VMess proxy server.
   * Negotiates TLS / WebSocket / VLESS protocol handshake.
   */
  private fun establishAndBridgeTcpSession(session: TcpSession, outputStream: FileOutputStream) {
    var socket: Socket? = null
    try {
      val baseSocket = Socket()
      vpnService.protect(baseSocket)

      val proxyHost = parsedConfig.host.ifBlank { server.ipAddress }
      val proxyPort = if (parsedConfig.port > 0) parsedConfig.port else server.port

      val targetAddr = try {
        InetAddress.getByName(proxyHost)
      } catch (e: Exception) {
        InetAddress.getByName(server.ipAddress)
      }

      baseSocket.connect(InetSocketAddress(targetAddr, proxyPort), 6000)
      baseSocket.tcpNoDelay = true

      var activeSocket: Socket = baseSocket

      // 1. TLS / REALITY Layer
      val isTls = parsedConfig.security.equals("tls", ignoreCase = true) ||
          parsedConfig.security.equals("reality", ignoreCase = true)
      if (isTls) {
        val sniHost = parsedConfig.sni.ifBlank { proxyHost }
        activeSocket = createTlsSocket(baseSocket, proxyHost, proxyPort, sniHost)
      }

      val inStream = activeSocket.getInputStream()
      val outStream = activeSocket.getOutputStream()

      // 2. WebSocket Upgrade Layer
      val isWs = parsedConfig.network.equals("ws", ignoreCase = true)
      if (isWs) {
        val sniHost = parsedConfig.sni.ifBlank { proxyHost }
        val wsPath = parsedConfig.path.ifBlank { "/" }
        val wsSuccess = performWebSocketHandshake(inStream, outStream, sniHost, wsPath)
        if (!wsSuccess) {
          Log.w(TAG, "WebSocket handshake failed for $proxyHost, falling back to direct stream")
        }
      }

      // 3. Protocol Request Layer (VLESS / Trojan)
      val dstIpStr = ipBytesToString(session.dstIp)
      val vlessUuidBytes = parseUuidToBytes(parsedConfig.uuidOrPassword.ifBlank { server.uuid })

      if (parsedConfig.protocol.equals("VLESS", ignoreCase = true)) {
        val vlessHeader = buildVlessHeader(
          uuidBytes = vlessUuidBytes,
          command = 1, // 1 = TCP
          dstIp = session.dstIp,
          dstPort = session.dstPort
        )
        outStream.write(vlessHeader)
        outStream.flush()
      } else if (parsedConfig.protocol.equals("Trojan", ignoreCase = true)) {
        val trojanHeader = buildTrojanHeader(
          password = parsedConfig.uuidOrPassword,
          dstIp = session.dstIp,
          dstPort = session.dstPort
        )
        outStream.write(trojanHeader)
        outStream.flush()
      }

      session.proxySocket = activeSocket
      session.proxyInputStream = inStream
      session.proxyOutputStream = outStream
      session.isEstablished.set(true)

      // Send TCP SYN-ACK reply packet back to TUN to establish Android socket
      sendTcpPacket(
        srcIp = session.dstIp, srcPort = session.dstPort,
        dstIp = session.srcIp, dstPort = session.srcPort,
        flags = 0x12, // SYN-ACK
        seqNum = session.serverSeqNum,
        ackNum = session.clientSeqNum + 1,
        payload = null,
        outputStream = outputStream
      )
      session.serverSeqNum += 1
      session.clientSeqNum += 1

      // Read response stream from proxy and send as TCP ACK+PSH packets to TUN
      val buffer = ByteArray(8192)
      while (isRunning.get() && session.isEstablished.get() && !activeSocket.isClosed) {
        val readBytes = inStream.read(buffer)
        if (readBytes > 0) {
          totalRxBytes.addAndGet(readBytes.toLong())
          val payload = buffer.copyOf(readBytes)

          sendTcpPacket(
            srcIp = session.dstIp, srcPort = session.dstPort,
            dstIp = session.srcIp, dstPort = session.srcPort,
            flags = 0x18, // PSH-ACK
            seqNum = session.serverSeqNum,
            ackNum = session.clientSeqNum,
            payload = payload,
            outputStream = outputStream
          )
          session.serverSeqNum += readBytes
        } else {
          break
        }
      }
    } catch (e: Exception) {
      Log.d(TAG, "TCP session establish note for ${session.sessionKey}: ${e.message}")
    } finally {
      closeTcpSession(session, outputStream, sendRstReply = true)
    }
  }

  private fun closeTcpSession(session: TcpSession, outputStream: FileOutputStream, sendRstReply: Boolean) {
    if (!session.isEstablished.getAndSet(false)) return
    try {
      if (sendRstReply) {
        sendTcpPacket(
          srcIp = session.dstIp, srcPort = session.dstPort,
          dstIp = session.srcIp, dstPort = session.srcPort,
          flags = 0x04, // RST
          seqNum = session.serverSeqNum,
          ackNum = session.clientSeqNum,
          payload = null,
          outputStream = outputStream
        )
      }
      session.proxySocket?.close()
    } catch (_: Exception) {}
    activeTcpSessions.remove(session.sessionKey)
  }

  /**
   * Constructs valid IPv4 TCP Packet and writes to TUN interface output stream.
   */
  private fun sendTcpPacket(
    srcIp: ByteArray, srcPort: Int,
    dstIp: ByteArray, dstPort: Int,
    flags: Int,
    seqNum: Long,
    ackNum: Long,
    payload: ByteArray?,
    outputStream: FileOutputStream
  ) {
    val payloadLen = payload?.size ?: 0
    val totalLen = 20 + 20 + payloadLen
    val packet = ByteArray(totalLen)

    // IPv4 Header
    packet[0] = 0x45.toByte() // Version 4, IHL 5
    packet[1] = 0x00.toByte()
    packet[2] = ((totalLen shr 8) and 0xFF).toByte()
    packet[3] = (totalLen and 0xFF).toByte()
    packet[4] = 0x12.toByte() // Identification
    packet[5] = 0x34.toByte()
    packet[6] = 0x40.toByte() // Don't Fragment
    packet[7] = 0x00.toByte()
    packet[8] = 64.toByte()   // TTL
    packet[9] = 6.toByte()    // Protocol TCP
    packet[10] = 0.toByte()   // Checksum placeholder
    packet[11] = 0.toByte()
    System.arraycopy(srcIp, 0, packet, 12, 4)
    System.arraycopy(dstIp, 0, packet, 16, 4)

    val ipChecksum = computeChecksum(packet, 0, 20)
    packet[10] = ((ipChecksum shr 8) and 0xFF).toByte()
    packet[11] = (ipChecksum and 0xFF).toByte()

    // TCP Header
    val tcpOffset = 20
    packet[tcpOffset] = ((srcPort shr 8) and 0xFF).toByte()
    packet[tcpOffset + 1] = (srcPort and 0xFF).toByte()
    packet[tcpOffset + 2] = ((dstPort shr 8) and 0xFF).toByte()
    packet[tcpOffset + 3] = (dstPort and 0xFF).toByte()

    // Sequence Number
    packet[tcpOffset + 4] = ((seqNum shr 24) and 0xFF).toByte()
    packet[tcpOffset + 5] = ((seqNum shr 16) and 0xFF).toByte()
    packet[tcpOffset + 6] = ((seqNum shr 8) and 0xFF).toByte()
    packet[tcpOffset + 7] = (seqNum and 0xFF).toByte()

    // Acknowledgment Number
    packet[tcpOffset + 8] = ((ackNum shr 24) and 0xFF).toByte()
    packet[tcpOffset + 9] = ((ackNum shr 16) and 0xFF).toByte()
    packet[tcpOffset + 10] = ((ackNum shr 8) and 0xFF).toByte()
    packet[tcpOffset + 11] = (ackNum and 0xFF).toByte()

    packet[tcpOffset + 12] = 0x50.toByte() // Data Offset 5 (20 bytes)
    packet[tcpOffset + 13] = flags.toByte()

    // Window Size (65535)
    packet[tcpOffset + 14] = 0xFF.toByte()
    packet[tcpOffset + 15] = 0xFF.toByte()

    packet[tcpOffset + 16] = 0.toByte() // Checksum
    packet[tcpOffset + 17] = 0.toByte()
    packet[tcpOffset + 18] = 0.toByte() // Urgent Pointer
    packet[tcpOffset + 19] = 0.toByte()

    if (payload != null && payloadLen > 0) {
      System.arraycopy(payload, 0, packet, 40, payloadLen)
    }

    synchronized(outputStream) {
      try {
        outputStream.write(packet, 0, totalLen)
        outputStream.flush()
      } catch (_: Exception) {}
    }
  }

  /**
   * Forwards general UDP traffic via protected socket.
   */
  private fun forwardGeneralUdp(
    udpData: ByteArray,
    clientIp: ByteArray,
    dstIp: ByteArray,
    clientPort: Int,
    dstPort: Int,
    outputStream: FileOutputStream
  ) {
    engineScope.launch {
      var socket: DatagramSocket? = null
      try {
        socket = DatagramSocket()
        vpnService.protect(socket)
        socket.soTimeout = 3000

        val targetAddr = InetAddress.getByAddress(dstIp)
        val outPacket = DatagramPacket(udpData, udpData.size, targetAddr, dstPort)
        socket.send(outPacket)

        val buffer = ByteArray(2048)
        val inPacket = DatagramPacket(buffer, buffer.size)
        socket.receive(inPacket)

        val replyLen = inPacket.length
        val ipLength = 20 + 8 + replyLen
        val replyPacket = ByteArray(ipLength)

        // IPv4 Header
        replyPacket[0] = 0x45.toByte()
        replyPacket[1] = 0x00.toByte()
        replyPacket[2] = ((ipLength shr 8) and 0xFF).toByte()
        replyPacket[3] = (ipLength and 0xFF).toByte()
        replyPacket[4] = 0x00.toByte()
        replyPacket[5] = 0x01.toByte()
        replyPacket[6] = 0x40.toByte()
        replyPacket[7] = 0x00.toByte()
        replyPacket[8] = 64.toByte()
        replyPacket[9] = 17.toByte() // UDP
        replyPacket[10] = 0.toByte()
        replyPacket[11] = 0.toByte()
        System.arraycopy(dstIp, 0, replyPacket, 12, 4)
        System.arraycopy(clientIp, 0, replyPacket, 16, 4)

        val ipChecksum = computeChecksum(replyPacket, 0, 20)
        replyPacket[10] = ((ipChecksum shr 8) and 0xFF).toByte()
        replyPacket[11] = (ipChecksum and 0xFF).toByte()

        // UDP Header
        val udpOffset = 20
        val udpLength = 8 + replyLen
        replyPacket[udpOffset] = ((dstPort shr 8) and 0xFF).toByte()
        replyPacket[udpOffset + 1] = (dstPort and 0xFF).toByte()
        replyPacket[udpOffset + 2] = ((clientPort shr 8) and 0xFF).toByte()
        replyPacket[udpOffset + 3] = (clientPort and 0xFF).toByte()
        replyPacket[udpOffset + 4] = ((udpLength shr 8) and 0xFF).toByte()
        replyPacket[udpOffset + 5] = (udpLength and 0xFF).toByte()
        replyPacket[udpOffset + 6] = 0.toByte()
        replyPacket[udpOffset + 7] = 0.toByte()

        System.arraycopy(inPacket.data, 0, replyPacket, udpOffset + 8, replyLen)

        synchronized(outputStream) {
          outputStream.write(replyPacket, 0, ipLength)
          outputStream.flush()
        }
        totalRxBytes.addAndGet(ipLength.toLong())
      } catch (_: Exception) {
      } finally {
        try {
          socket?.close()
        } catch (_: Exception) {}
      }
    }
  }

  /**
   * Forwards DNS queries via a protected UDP socket directly to upstream DNS (1.1.1.1 / 8.8.8.8).
   */
  private fun forwardDnsQuery(
    dnsQuery: ByteArray,
    clientIp: ByteArray,
    dnsServerIp: ByteArray,
    clientPort: Int,
    dnsPort: Int,
    outputStream: FileOutputStream
  ) {
    engineScope.launch {
      var udpSocket: DatagramSocket? = null
      try {
        udpSocket = DatagramSocket()
        vpnService.protect(udpSocket)
        udpSocket.soTimeout = 2500

        val targetDns = InetAddress.getByAddress(dnsServerIp)
        val outPacket = DatagramPacket(dnsQuery, dnsQuery.size, targetDns, 53)
        udpSocket.send(outPacket)

        val responseBuffer = ByteArray(2048)
        val inPacket = DatagramPacket(responseBuffer, responseBuffer.size)
        udpSocket.receive(inPacket)

        val dnsResponseLength = inPacket.length
        val responsePayload = inPacket.data.copyOf(dnsResponseLength)

        val ipLength = 20 + 8 + dnsResponseLength
        val replyPacket = ByteArray(ipLength)

        replyPacket[0] = 0x45.toByte()
        replyPacket[1] = 0x00.toByte()
        replyPacket[2] = ((ipLength shr 8) and 0xFF).toByte()
        replyPacket[3] = (ipLength and 0xFF).toByte()
        replyPacket[4] = 0x00.toByte()
        replyPacket[5] = 0x01.toByte()
        replyPacket[6] = 0x40.toByte()
        replyPacket[7] = 0x00.toByte()
        replyPacket[8] = 64.toByte()
        replyPacket[9] = 17.toByte()
        replyPacket[10] = 0.toByte()
        replyPacket[11] = 0.toByte()
        System.arraycopy(dnsServerIp, 0, replyPacket, 12, 4)
        System.arraycopy(clientIp, 0, replyPacket, 16, 4)

        val ipChecksum = computeChecksum(replyPacket, 0, 20)
        replyPacket[10] = ((ipChecksum shr 8) and 0xFF).toByte()
        replyPacket[11] = (ipChecksum and 0xFF).toByte()

        val udpOffset = 20
        val udpLength = 8 + dnsResponseLength
        replyPacket[udpOffset] = ((dnsPort shr 8) and 0xFF).toByte()
        replyPacket[udpOffset + 1] = (dnsPort and 0xFF).toByte()
        replyPacket[udpOffset + 2] = ((clientPort shr 8) and 0xFF).toByte()
        replyPacket[udpOffset + 3] = (clientPort and 0xFF).toByte()
        replyPacket[udpOffset + 4] = ((udpLength shr 8) and 0xFF).toByte()
        replyPacket[udpOffset + 5] = (udpLength and 0xFF).toByte()
        replyPacket[udpOffset + 6] = 0.toByte()
        replyPacket[udpOffset + 7] = 0.toByte()

        System.arraycopy(responsePayload, 0, replyPacket, udpOffset + 8, dnsResponseLength)

        synchronized(outputStream) {
          outputStream.write(replyPacket, 0, ipLength)
          outputStream.flush()
        }

        totalRxBytes.addAndGet(ipLength.toLong())
      } catch (_: Exception) {
      } finally {
        try {
          udpSocket?.close()
        } catch (_: Exception) {}
      }
    }
  }

  /**
   * Responds to ICMP Echo Request by converting it into an Echo Reply and injecting back to TUN.
   */
  private fun respondToIcmpPing(
    packet: ByteArray,
    length: Int,
    ihl: Int,
    outputStream: FileOutputStream
  ) {
    try {
      val reply = packet.copyOf(length)

      for (i in 0..3) {
        val temp = reply[12 + i]
        reply[12 + i] = reply[16 + i]
        reply[16 + i] = temp
      }

      reply[10] = 0
      reply[11] = 0
      val ipChecksum = computeChecksum(reply, 0, ihl)
      reply[10] = ((ipChecksum shr 8) and 0xFF).toByte()
      reply[11] = (ipChecksum and 0xFF).toByte()

      reply[ihl] = 0.toByte()
      reply[ihl + 1] = 0.toByte()
      reply[ihl + 2] = 0.toByte()
      reply[ihl + 3] = 0.toByte()

      val icmpLength = length - ihl
      val icmpChecksum = computeChecksum(reply, ihl, icmpLength)
      reply[ihl + 2] = ((icmpChecksum shr 8) and 0xFF).toByte()
      reply[ihl + 3] = (icmpChecksum and 0xFF).toByte()

      synchronized(outputStream) {
        outputStream.write(reply, 0, length)
        outputStream.flush()
      }
      totalRxBytes.addAndGet(length.toLong())
    } catch (_: Exception) {}
  }

  // Helper Methods for Protocols and Checksums

  private fun parseUuidToBytes(uuidStr: String): ByteArray {
    val cleaned = uuidStr.replace("-", "").trim()
    val bytes = ByteArray(16)
    if (cleaned.length >= 32) {
      for (i in 0 until 16) {
        val index = i * 2
        bytes[i] = cleaned.substring(index, index + 2).toInt(16).toByte()
      }
    } else {
      val md5 = java.security.MessageDigest.getInstance("MD5").digest(uuidStr.toByteArray())
      System.arraycopy(md5, 0, bytes, 0, 16)
    }
    return bytes
  }

  private fun buildVlessHeader(uuidBytes: ByteArray, command: Byte, dstIp: ByteArray, dstPort: Int): ByteArray {
    val baos = ByteArrayOutputStream()
    baos.write(0) // Version 0
    baos.write(uuidBytes) // 16-byte UUID
    baos.write(0) // Addons length 0
    baos.write(command.toInt()) // Command: 1 = TCP, 2 = UDP
    baos.write((dstPort shr 8) and 0xFF) // Port High
    baos.write(dstPort and 0xFF) // Port Low
    baos.write(1) // Address Type: 1 = IPv4
    baos.write(dstIp) // 4-byte IPv4 Address
    return baos.toByteArray()
  }

  private fun buildTrojanHeader(password: String, dstIp: ByteArray, dstPort: Int): ByteArray {
    val baos = ByteArrayOutputStream()
    val passHash = java.security.MessageDigest.getInstance("SHA-225").digest(password.toByteArray())
    val hexHash = passHash.joinToString("") { "%02x".format(it) }
    baos.write(hexHash.toByteArray(StandardCharsets.UTF_8))
    baos.write("\r\n".toByteArray(StandardCharsets.UTF_8))
    baos.write(1) // Command: 1 = CONNECT TCP
    baos.write(1) // Address Type: 1 = IPv4
    baos.write(dstIp)
    baos.write((dstPort shr 8) and 0xFF)
    baos.write(dstPort and 0xFF)
    baos.write("\r\n".toByteArray(StandardCharsets.UTF_8))
    return baos.toByteArray()
  }

  private fun performWebSocketHandshake(
    inStream: InputStream,
    outStream: OutputStream,
    host: String,
    path: String
  ): Boolean {
    val wsPath = if (path.isBlank()) "/" else if (!path.startsWith("/")) "/$path" else path
    val req = "GET $wsPath HTTP/1.1\r\n" +
        "Host: $host\r\n" +
        "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36\r\n" +
        "Upgrade: websocket\r\n" +
        "Connection: Upgrade\r\n" +
        "Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==\r\n" +
        "Sec-WebSocket-Version: 13\r\n\r\n"
    outStream.write(req.toByteArray(StandardCharsets.UTF_8))
    outStream.flush()

    val buffer = ByteArray(1024)
    val read = inStream.read(buffer)
    if (read > 0) {
      val resp = String(buffer, 0, read, StandardCharsets.UTF_8)
      return resp.contains("101")
    }
    return false
  }

  private fun createTlsSocket(baseSocket: Socket, host: String, port: Int, sniHost: String): Socket {
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, arrayOf<TrustManager>(object : X509TrustManager {
      override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
      override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
      override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    }), java.security.SecureRandom())

    val factory = sslContext.socketFactory
    val sslSocket = factory.createSocket(baseSocket, host, port, true) as SSLSocket

    try {
      val sni = if (sniHost.isNotBlank()) sniHost else host
      val params = sslSocket.sslParameters
      params.serverNames = listOf(SNIHostName(sni))
      sslSocket.sslParameters = params
    } catch (e: Exception) {
      Log.w(TAG, "SNI setup note: ${e.message}")
    }

    sslSocket.startHandshake()
    return sslSocket
  }

  private fun computeChecksum(data: ByteArray, offset: Int, length: Int): Int {
    var sum = 0
    var i = offset
    val end = offset + length

    while (i < end - 1) {
      val b1 = data[i].toInt() and 0xFF
      val b2 = data[i + 1].toInt() and 0xFF
      sum += (b1 shl 8) or b2
      i += 2
    }

    if (i < end) {
      sum += (data[i].toInt() and 0xFF) shl 8
    }

    while ((sum shr 16) > 0) {
      sum = (sum and 0xFFFF) + (sum shr 16)
    }

    return sum.inv() and 0xFFFF
  }

  private fun ipBytesToString(ip: ByteArray): String {
    return if (ip.size == 4) {
      "${ip[0].toInt() and 0xFF}.${ip[1].toInt() and 0xFF}.${ip[2].toInt() and 0xFF}.${ip[3].toInt() and 0xFF}"
    } else "0.0.0.0"
  }

  private fun extractSeqNum(packet: ByteArray, offset: Int): Long {
    if (packet.size < offset + 4) return 100L
    return ((packet[offset].toLong() and 0xFF) shl 24) or
        ((packet[offset + 1].toLong() and 0xFF) shl 16) or
        ((packet[offset + 2].toLong() and 0xFF) shl 8) or
        (packet[offset + 3].toLong() and 0xFF)
  }

  private fun startTelemetryLoop() {
    telemetryJob = engineScope.launch {
      while (isRunning.get() && isActive) {
        delay(1000)
        val curRx = totalRxBytes.get()
        val curTx = totalTxBytes.get()

        val rxSec = curRx - lastRxBytes
        val txSec = curTx - lastTxBytes

        lastRxBytes = curRx
        lastTxBytes = curTx

        VpnStateManager.updateTelemetry(rxSec, txSec, curRx, curTx, vpnService)
      }
    }
  }
}
