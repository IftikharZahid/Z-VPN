package com.zahidcodes.zvpn.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.zahidcodes.zvpn.MainActivity
import com.zahidcodes.zvpn.core.ParsedVpnConfig
import com.zahidcodes.zvpn.core.VpnConfigParser
import com.zahidcodes.zvpn.core.VpnStateManager
import com.zahidcodes.zvpn.core.ZvpnTunnelEngine
import com.zahidcodes.zvpn.model.Server
import com.zahidcodes.zvpn.model.VpnStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ZvpnService : VpnService() {

  companion object {
    private const val TAG = "ZvpnService"
    const val ACTION_CONNECT = "com.zahidcodes.zvpn.service.ACTION_CONNECT"
    const val ACTION_DISCONNECT = "com.zahidcodes.zvpn.service.ACTION_DISCONNECT"

    const val EXTRA_SERVER_ID = "extra_server_id"
    const val EXTRA_SERVER_NAME = "extra_server_name"
    const val EXTRA_SERVER_COUNTRY = "extra_server_country"
    const val EXTRA_SERVER_COUNTRY_CODE = "extra_server_country_code"
    const val EXTRA_SERVER_IP = "extra_server_ip"
    const val EXTRA_PROTOCOL = "extra_protocol"
    const val EXTRA_CONFIG_URI = "extra_config_uri"
    const val EXTRA_PORT = "extra_port"
    const val EXTRA_UUID = "extra_uuid"

    private const val NOTIFICATION_ID = 1001
    private const val CHANNEL_ID = "zvpn_service_channel"

    private val RECONNECT_DELAYS_MS = listOf(2000L, 5000L, 10000L, 20000L, 30000L)
  }

  private var tunInterface: ParcelFileDescriptor? = null
  private var tunnelEngine: ZvpnTunnelEngine? = null
  private val serviceScope = CoroutineScope(Dispatchers.IO)
  private var reconnectJob: Job? = null

  private var connectivityManager: ConnectivityManager? = null
  private var networkCallback: ConnectivityManager.NetworkCallback? = null
  private var currentTransport = "Detecting..."

  private var activeServer: Server? = null
  private var parsedConfig: ParsedVpnConfig? = null
  private var isConnecting = false

  override fun onCreate() {
    super.onCreate()
    Log.i(TAG, "ZvpnService onCreate called")
    VpnStateManager.init(this)
    createNotificationChannel()
    registerConnectivityManager()
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val action = intent?.action

    if (intent == null) {
      Log.i(TAG, "ZvpnService restarted by Android system (START_STICKY). Attempting session recovery...")
      recoverPersistedConnection()
      return START_STICKY
    }

    Log.i(TAG, "onStartCommand received action: $action")
    when (action) {
      ACTION_CONNECT -> {
        val server = Server(
          id = intent.getStringExtra(EXTRA_SERVER_ID) ?: "default-server",
          city = intent.getStringExtra(EXTRA_SERVER_NAME) ?: "New York",
          country = intent.getStringExtra(EXTRA_SERVER_COUNTRY) ?: "United States",
          countryCode = intent.getStringExtra(EXTRA_SERVER_COUNTRY_CODE) ?: "US",
          pingMs = 24,
          loadPercent = 38,
          ipAddress = intent.getStringExtra(EXTRA_SERVER_IP) ?: "185.241.44.12",
          port = intent.getIntExtra(EXTRA_PORT, 443),
          protocolSupport = intent.getStringExtra(EXTRA_PROTOCOL) ?: "VLESS · REALITY",
          configUri = intent.getStringExtra(EXTRA_CONFIG_URI) ?: "",
          uuid = intent.getStringExtra(EXTRA_UUID) ?: ""
        )
        connectVpnTunnel(server)
      }
      ACTION_DISCONNECT -> {
        disconnectVpnTunnel(normalUserDisconnect = true)
      }
      else -> {
        Log.w(TAG, "Unhandled intent action: $action")
      }
    }

    return START_STICKY
  }

  private fun connectVpnTunnel(server: Server) {
    if (isConnecting) return
    isConnecting = true
    reconnectJob?.cancel()
    activeServer = server

    VpnStateManager.onServiceStarted(server, this)

    serviceScope.launch {
      try {
        parsedConfig = VpnConfigParser.parse(server.configUri, server.ipAddress, server.port)
        Log.i(TAG, "Parsed configuration for ${server.city}: protocol=${parsedConfig?.protocol}, host=${parsedConfig?.host}:${parsedConfig?.port}")

        // Post persistent foreground notification
        val notification = buildNotification(server.city, server.protocolSupport, isReconnecting = false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
          startForeground(NOTIFICATION_ID, notification)
        }

        // Close any stale TUN descriptor before establishing a fresh interface
        closeTunInterface()

        // Configure full-device Android VpnService.Builder
        Log.i(TAG, "Constructing TUN Builder: IPv4 0.0.0.0/0 full-device route, MTU 1400, DNS 1.1.1.1, 8.8.8.8")
        val builder = Builder()
          .setSession("ZVPN - ${server.city}")
          .addAddress("10.0.0.2", 24)
          .addRoute("0.0.0.0", 0) // Route ALL IPv4 traffic on the entire device
          .addDnsServer("1.1.1.1")
          .addDnsServer("8.8.8.8")
          .addDnsServer("1.0.0.1")
          .addDnsServer("8.8.4.4")
          .setMtu(1400)
          .setBlocking(true)

        // Prevent IPv6 leaks by routing or claiming IPv6 if supported
        try {
          builder.addAddress("fd00:1:fd00:1::2", 64)
          builder.addRoute("::", 0)
        } catch (e: Exception) {
          Log.w(TAG, "IPv6 route setup note: ${e.message} (device full-device IPv4 routing active)")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          builder.setMetered(false)
        }

        try {
          builder.addDisallowedApplication(packageName)
        } catch (e: Exception) {
          Log.w(TAG, "Disallowed application setup note: ${e.message}")
        }

        val pfd = builder.establish()
        if (pfd == null) {
          throw IllegalStateException("VpnService.Builder.establish() returned null. Permission might be missing or revoked.")
        }
        tunInterface = pfd

        // Start dedicated packet processing engine with socket protection
        val engine = ZvpnTunnelEngine(
          vpnService = this@ZvpnService,
          tunFd = pfd,
          server = server,
          parsedConfig = parsedConfig!!
        )
        tunnelEngine = engine
        engine.start()

        val connectTimestamp = System.currentTimeMillis()
        VpnStateManager.onTunnelConnected(server, connectTimestamp, this@ZvpnService)
        isConnecting = false

        Log.i(TAG, "ZVPN full-device tunnel established and running for ${server.city}")
      } catch (e: Exception) {
        isConnecting = false
        Log.e(TAG, "Failed to connect VPN tunnel: ${e.message}", e)
        handleConnectionFailure(server, e.localizedMessage ?: "Connection failed")
      }
    }
  }

  private fun handleConnectionFailure(server: Server, reason: String) {
    serviceScope.launch {
      val attempt = VpnStateManager.reconnectCount.value + 1
      if (attempt <= RECONNECT_DELAYS_MS.size) {
        val delayMs = RECONNECT_DELAYS_MS[attempt - 1]
        VpnStateManager.onTunnelReconnecting(reason, attempt)

        val notification = buildNotification(server.city, "${server.protocolSupport} (Reconnecting attempt $attempt)", isReconnecting = true)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)

        Log.w(TAG, "Connection failed. Retrying in ${delayMs / 1000}s (Attempt $attempt of ${RECONNECT_DELAYS_MS.size})...")
        delay(delayMs)

        if (VpnStateManager.vpnStatus.value == VpnStatus.RECONNECTING) {
          connectVpnTunnel(server)
        }
      } else {
        Log.e(TAG, "Max reconnection attempts reached. Terminating tunnel.")
        disconnectVpnTunnel(normalUserDisconnect = false)
      }
    }
  }

  private fun recoverPersistedConnection() {
    val server = VpnStateManager.getPersistedServer(this)
    val wasActive = VpnStateManager.isPersistedActive(this)
    if (wasActive && server != null) {
      Log.i(TAG, "Recovering active VPN session for ${server.city} from persistence")
      connectVpnTunnel(server)
    } else {
      Log.i(TAG, "No active persisted session found. Stopping service.")
      stopSelf()
    }
  }

  private fun disconnectVpnTunnel(normalUserDisconnect: Boolean) {
    Log.i(TAG, "disconnectVpnTunnel called (normalUserDisconnect=$normalUserDisconnect)")
    reconnectJob?.cancel()
    isConnecting = false

    tunnelEngine?.stop()
    tunnelEngine = null

    closeTunInterface()

    VpnStateManager.onServiceStopped(this, normalUserDisconnect)

    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  private fun closeTunInterface() {
    try {
      tunInterface?.close()
      tunInterface = null
      Log.d(TAG, "TUN interface descriptor closed")
    } catch (e: Exception) {
      Log.w(TAG, "Error closing TUN interface: ${e.message}")
    }
  }

  /**
   * CRITICAL: When the user swipes ZVPN away from Recents, onTaskRemoved is called.
   * A professional Android VPN must NOT disconnect merely because the Activity is closed!
   */
  override fun onTaskRemoved(rootIntent: Intent?) {
    super.onTaskRemoved(rootIntent)
    Log.i(TAG, "ZVPN UI swiped from Recents. VPN foreground service remains ACTIVE and running.")
    // Intentionally do NOT stop the VPN tunnel or service here!
  }

  override fun onDestroy() {
    Log.i(TAG, "ZvpnService onDestroy called")
    unregisterConnectivityManager()
    disconnectVpnTunnel(normalUserDisconnect = false)
    super.onDestroy()
  }

  private fun registerConnectivityManager() {
    try {
      val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
      connectivityManager = cm

      val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
        .build()

      networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
          super.onAvailable(network)
          val caps = cm.getNetworkCapabilities(network)
          val transport = resolveTransport(caps)
          handleNetworkSwitch(transport, "Network available")
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
          super.onCapabilitiesChanged(network, networkCapabilities)
          val transport = resolveTransport(networkCapabilities)
          handleNetworkSwitch(transport, "Capabilities changed")
        }

        override fun onLost(network: Network) {
          super.onLost(network)
          val activeCaps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
          val remainingTransport = resolveTransport(activeCaps)
          handleNetworkSwitch(remainingTransport, "Network interface lost")
        }
      }

      cm.registerNetworkCallback(request, networkCallback!!)
      val initialCaps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
      currentTransport = resolveTransport(initialCaps)
      VpnStateManager.updateNetworkType(currentTransport)
    } catch (e: Exception) {
      Log.e(TAG, "Failed to register NetworkCallback: ${e.message}", e)
    }
  }

  private fun handleNetworkSwitch(newTransport: String, reason: String) {
    if (newTransport == currentTransport || newTransport == "Disconnected") return
    val oldTransport = currentTransport
    currentTransport = newTransport
    VpnStateManager.updateNetworkType(newTransport)
    Log.i(TAG, "[NETWORK SWITCH] Transition: $oldTransport -> $newTransport ($reason)")

    if (VpnStateManager.vpnStatus.value == VpnStatus.CONNECTED && activeServer != null) {
      Log.i(TAG, "Re-protecting sockets and refreshing tunnel for new transport: $newTransport")
      activeServer?.let { s ->
        connectVpnTunnel(s)
      }
    }
  }

  private fun resolveTransport(caps: NetworkCapabilities?): String {
    if (caps == null) return "Disconnected"
    return when {
      caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular (5G/4G)"
      caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
      else -> "Mobile Data"
    }
  }

  private fun unregisterConnectivityManager() {
    try {
      if (networkCallback != null && connectivityManager != null) {
        connectivityManager?.unregisterNetworkCallback(networkCallback!!)
        networkCallback = null
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error unregistering ConnectivityManager: ${e.message}")
    }
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        "ZVPN Connection Status",
        NotificationManager.IMPORTANCE_LOW
      ).apply {
        description = "Shows real-time status and quick controls for active ZVPN tunnel"
        setShowBadge(false)
      }
      val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  private fun buildNotification(serverName: String, protocol: String, isReconnecting: Boolean): Notification {
    val openAppIntent = Intent(this, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    val contentPendingIntent = PendingIntent.getActivity(
      this,
      0,
      openAppIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val disconnectIntent = Intent(this, ZvpnService::class.java).apply {
      action = ACTION_DISCONNECT
    }
    val disconnectPendingIntent = PendingIntent.getService(
      this,
      1,
      disconnectIntent,
      PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val title = if (isReconnecting) "ZVPN · Reconnecting…" else "ZVPN · Connected to $serverName"
    val text = if (isReconnecting) "Restoring encrypted tunnel connection..." else "Routing full-device traffic via $protocol"

    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle(title)
      .setContentText(text)
      .setSmallIcon(android.R.drawable.ic_lock_lock)
      .setOngoing(true)
      .setContentIntent(contentPendingIntent)
      .addAction(
        android.R.drawable.ic_menu_close_clear_cancel,
        "Disconnect",
        disconnectPendingIntent
      )
      .setPriority(NotificationCompat.PRIORITY_LOW)
      .setCategory(NotificationCompat.CATEGORY_SERVICE)
      .build()
  }
}
