package com.zahidcodes.zvpn.core

import android.util.Base64
import android.net.Uri
import org.json.JSONObject
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

data class ParsedVpnConfig(
  val protocol: String,
  val host: String,
  val port: Int,
  val uuidOrPassword: String = "",
  val security: String = "none",
  val sni: String = "",
  val path: String = "",
  val network: String = "tcp",
  val encryption: String = "none",
  val remark: String = "",
  val rawUri: String = "",
  val alpn: String = "http/1.1"
)

object VpnConfigParser {

  fun parse(rawUri: String, fallbackHost: String, fallbackPort: Int): ParsedVpnConfig {
    if (rawUri.isBlank()) {
      return ParsedVpnConfig(
        protocol = "VLESS",
        host = fallbackHost,
        port = fallbackPort,
        uuidOrPassword = "zvpn-default-uuid",
        security = "reality",
        sni = fallbackHost,
        remark = "ZVPN Server",
        rawUri = rawUri
      )
    }

    val trimmed = rawUri.trim()
    return try {
      when {
        trimmed.startsWith("vless://", ignoreCase = true) -> parseVless(trimmed)
        trimmed.startsWith("trojan://", ignoreCase = true) -> parseTrojan(trimmed)
        trimmed.startsWith("vmess://", ignoreCase = true) -> parseVmess(trimmed)
        trimmed.startsWith("ss://", ignoreCase = true) -> parseShadowsocks(trimmed)
        trimmed.startsWith("wireguard://", ignoreCase = true) || trimmed.startsWith("wg://", ignoreCase = true) -> parseWireguard(trimmed)
        trimmed.startsWith("hy2://", ignoreCase = true) || trimmed.startsWith("hysteria2://", ignoreCase = true) -> parseHysteria2(trimmed)
        else -> ParsedVpnConfig(
          protocol = "VLESS",
          host = fallbackHost,
          port = fallbackPort,
          rawUri = trimmed
        )
      }
    } catch (e: Exception) {
      ParsedVpnConfig(
        protocol = "VLESS",
        host = fallbackHost,
        port = fallbackPort,
        rawUri = trimmed
      )
    }
  }

  fun parseVless(uriStr: String): ParsedVpnConfig {
    return try {
      var raw = uriStr.trim()
      var remark = ""
      if (raw.contains("#")) {
        val hashSplit = raw.split("#", limit = 2)
        raw = hashSplit[0]
        if (hashSplit.size > 1) {
          remark = try {
            URLDecoder.decode(hashSplit[1].trim(), StandardCharsets.UTF_8.name())
          } catch (e: Exception) {
            hashSplit[1].trim()
          }
        }
      }

      val schemeEnd = raw.indexOf("://")
      val afterScheme = if (schemeEnd != -1) raw.substring(schemeEnd + 3) else raw
      val queryStart = afterScheme.indexOf("?")
      val authority = if (queryStart != -1) afterScheme.substring(0, queryStart) else afterScheme
      val queryString = if (queryStart != -1) afterScheme.substring(queryStart + 1) else ""

      // Clean authority (strip any path after host:port)
      val cleanAuth = if (authority.contains("/")) authority.substring(0, authority.indexOf("/")) else authority
      val atIndex = cleanAuth.indexOf("@")
      val uuid = if (atIndex != -1) cleanAuth.substring(0, atIndex) else ""
      val hostPort = if (atIndex != -1) cleanAuth.substring(atIndex + 1) else cleanAuth
      val colonIndex = hostPort.lastIndexOf(":")
      val host = if (colonIndex != -1) hostPort.substring(0, colonIndex) else hostPort
      val port = if (colonIndex != -1) hostPort.substring(colonIndex + 1).toIntOrNull() ?: 443 else 443

      // Parse query params
      val params = mutableMapOf<String, String>()
      if (queryString.isNotBlank()) {
        for (part in queryString.split("&")) {
          val kv = part.split("=", limit = 2)
          if (kv.isNotEmpty()) {
            val k = kv[0].lowercase().trim()
            val v = if (kv.size > 1) {
              try {
                URLDecoder.decode(kv[1], StandardCharsets.UTF_8.name())
              } catch (e: Exception) {
                kv[1]
              }
            } else ""
            params[k] = v
          }
        }
      }

      val security = params["security"] ?: "none"
      val sni = params["sni"] ?: params["host"] ?: host
      val path = params["path"] ?: ""
      val type = params["type"] ?: params["net"] ?: "tcp"
      val encryption = params["encryption"] ?: "none"
      val alpn = params["alpn"] ?: "http/1.1"

      ParsedVpnConfig(
        protocol = "VLESS",
        host = host.ifBlank { "127.0.0.1" },
        port = port,
        uuidOrPassword = uuid,
        security = security,
        sni = sni,
        path = path,
        network = type,
        encryption = encryption,
        remark = remark,
        rawUri = uriStr,
        alpn = alpn
      )
    } catch (e: Exception) {
      ParsedVpnConfig(
        protocol = "VLESS",
        host = "127.0.0.1",
        port = 443,
        rawUri = uriStr
      )
    }
  }

  private fun parseTrojan(uriStr: String): ParsedVpnConfig {
    return try {
      var raw = uriStr.trim()
      var remark = ""
      if (raw.contains("#")) {
        val hashSplit = raw.split("#", limit = 2)
        raw = hashSplit[0]
        if (hashSplit.size > 1) {
          remark = try {
            URLDecoder.decode(hashSplit[1].trim(), StandardCharsets.UTF_8.name())
          } catch (e: Exception) {
            hashSplit[1].trim()
          }
        }
      }
      val schemeEnd = raw.indexOf("://")
      val afterScheme = if (schemeEnd != -1) raw.substring(schemeEnd + 3) else raw
      val queryStart = afterScheme.indexOf("?")
      val authority = if (queryStart != -1) afterScheme.substring(0, queryStart) else afterScheme
      val cleanAuth = if (authority.contains("/")) authority.substring(0, authority.indexOf("/")) else authority
      val atIndex = cleanAuth.indexOf("@")
      val password = if (atIndex != -1) cleanAuth.substring(0, atIndex) else ""
      val hostPort = if (atIndex != -1) cleanAuth.substring(atIndex + 1) else cleanAuth
      val colonIndex = hostPort.lastIndexOf(":")
      val host = if (colonIndex != -1) hostPort.substring(0, colonIndex) else hostPort
      val port = if (colonIndex != -1) hostPort.substring(colonIndex + 1).toIntOrNull() ?: 443 else 443

      val uri = Uri.parse(uriStr)
      val sni = uri.getQueryParameter("sni") ?: uri.getQueryParameter("host") ?: host
      val type = uri.getQueryParameter("type") ?: "tcp"

      ParsedVpnConfig(
        protocol = "Trojan",
        host = host,
        port = port,
        uuidOrPassword = password,
        security = "tls",
        sni = sni,
        network = type,
        remark = remark,
        rawUri = uriStr
      )
    } catch (e: Exception) {
      ParsedVpnConfig(protocol = "Trojan", host = "127.0.0.1", port = 443, rawUri = uriStr)
    }
  }

  private fun parseVmess(uriStr: String): ParsedVpnConfig {
    return try {
      val b64 = uriStr.removePrefix("vmess://").trim()
      val decodedBytes = try {
        Base64.decode(b64, Base64.DEFAULT)
      } catch (e: Exception) {
        Base64.decode(b64, Base64.URL_SAFE or Base64.NO_PADDING)
      }
      val decoded = String(decodedBytes, StandardCharsets.UTF_8)
      val json = JSONObject(decoded)
      val host = json.optString("add", "127.0.0.1")
      val port = json.optInt("port", 443)
      val id = json.optString("id", "")
      val net = json.optString("net", "ws")
      val path = json.optString("path", "")
      val tls = json.optString("tls", "none")
      val hostHeader = json.optString("host", host)
      val sni = json.optString("sni", hostHeader)
      val ps = json.optString("ps", "")
      val alpn = json.optString("alpn", "http/1.1")

      ParsedVpnConfig(
        protocol = "VMess",
        host = host,
        port = port,
        uuidOrPassword = id,
        security = tls,
        sni = sni.ifBlank { hostHeader },
        path = path,
        network = net,
        remark = ps,
        rawUri = uriStr,
        alpn = alpn
      )
    } catch (e: Exception) {
      ParsedVpnConfig(protocol = "VMess", host = "127.0.0.1", port = 443, rawUri = uriStr)
    }
  }

  private fun parseShadowsocks(uriStr: String): ParsedVpnConfig {
    return try {
      val uri = Uri.parse(uriStr)
      val host = uri.host ?: "127.0.0.1"
      val port = if (uri.port > 0) uri.port else 8388
      val userInfo = uri.userInfo ?: ""
      val remark = uri.fragment ?: ""

      ParsedVpnConfig(
        protocol = "Shadowsocks",
        host = host,
        port = port,
        uuidOrPassword = userInfo,
        remark = remark,
        rawUri = uriStr
      )
    } catch (e: Exception) {
      ParsedVpnConfig(protocol = "Shadowsocks", host = "127.0.0.1", port = 8388, rawUri = uriStr)
    }
  }

  private fun parseWireguard(uriStr: String): ParsedVpnConfig {
    return try {
      val uri = Uri.parse(uriStr)
      val host = uri.host ?: "127.0.0.1"
      val port = if (uri.port > 0) uri.port else 51820
      val pubKey = uri.getQueryParameter("public_key") ?: ""
      val remark = uri.fragment ?: ""

      ParsedVpnConfig(
        protocol = "WireGuard",
        host = host,
        port = port,
        uuidOrPassword = pubKey,
        remark = remark,
        rawUri = uriStr
      )
    } catch (e: Exception) {
      ParsedVpnConfig(protocol = "WireGuard", host = "127.0.0.1", port = 51820, rawUri = uriStr)
    }
  }

  private fun parseHysteria2(uriStr: String): ParsedVpnConfig {
    return try {
      val uri = Uri.parse(uriStr)
      val host = uri.host ?: "127.0.0.1"
      val port = if (uri.port > 0) uri.port else 443
      val password = uri.userInfo ?: ""
      val sni = uri.getQueryParameter("sni") ?: uri.getQueryParameter("host") ?: host
      val remark = uri.fragment ?: ""

      ParsedVpnConfig(
        protocol = "Hysteria2",
        host = host,
        port = port,
        uuidOrPassword = password,
        sni = sni,
        remark = remark,
        rawUri = uriStr
      )
    } catch (e: Exception) {
      ParsedVpnConfig(protocol = "Hysteria2", host = "127.0.0.1", port = 443, rawUri = uriStr)
    }
  }
}
