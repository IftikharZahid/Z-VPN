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
  val remark: String = "",
  val rawUri: String = ""
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
        trimmed.startsWith("wireguard://", ignoreCase = true) -> parseWireguard(trimmed)
        trimmed.startsWith("hy2://", ignoreCase = true) -> parseHysteria2(trimmed)
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

  private fun parseVless(uriStr: String): ParsedVpnConfig {
    val uri = Uri.parse(uriStr)
    val userInfo = uri.userInfo ?: ""
    val host = uri.host ?: "127.0.0.1"
    val port = if (uri.port > 0) uri.port else 443
    val security = uri.getQueryParameter("security") ?: "none"
    val sni = uri.getQueryParameter("sni") ?: host
    val path = uri.getQueryParameter("path") ?: ""
    val type = uri.getQueryParameter("type") ?: "tcp"
    val remark = uri.fragment ?: ""

    return ParsedVpnConfig(
      protocol = "VLESS",
      host = host,
      port = port,
      uuidOrPassword = userInfo,
      security = security,
      sni = sni,
      path = path,
      network = type,
      remark = remark,
      rawUri = uriStr
    )
  }

  private fun parseTrojan(uriStr: String): ParsedVpnConfig {
    val uri = Uri.parse(uriStr)
    val password = uri.userInfo ?: ""
    val host = uri.host ?: "127.0.0.1"
    val port = if (uri.port > 0) uri.port else 443
    val sni = uri.getQueryParameter("sni") ?: host
    val type = uri.getQueryParameter("type") ?: "tcp"
    val remark = uri.fragment ?: ""

    return ParsedVpnConfig(
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
  }

  private fun parseVmess(uriStr: String): ParsedVpnConfig {
    val b64 = uriStr.removePrefix("vmess://").trim()
    val decoded = String(Base64.decode(b64, Base64.DEFAULT), StandardCharsets.UTF_8)
    val json = JSONObject(decoded)
    val host = json.optString("add", "127.0.0.1")
    val port = json.optInt("port", 443)
    val id = json.optString("id", "")
    val net = json.optString("net", "ws")
    val path = json.optString("path", "")
    val tls = json.optString("tls", "none")
    val hostHeader = json.optString("host", host)
    val ps = json.optString("ps", "")

    return ParsedVpnConfig(
      protocol = "VMess",
      host = host,
      port = port,
      uuidOrPassword = id,
      security = tls,
      sni = hostHeader,
      path = path,
      network = net,
      remark = ps,
      rawUri = uriStr
    )
  }

  private fun parseShadowsocks(uriStr: String): ParsedVpnConfig {
    val uri = Uri.parse(uriStr)
    val host = uri.host ?: "127.0.0.1"
    val port = if (uri.port > 0) uri.port else 8388
    val userInfo = uri.userInfo ?: ""
    val remark = uri.fragment ?: ""

    return ParsedVpnConfig(
      protocol = "Shadowsocks",
      host = host,
      port = port,
      uuidOrPassword = userInfo,
      remark = remark,
      rawUri = uriStr
    )
  }

  private fun parseWireguard(uriStr: String): ParsedVpnConfig {
    val uri = Uri.parse(uriStr)
    val host = uri.host ?: "127.0.0.1"
    val port = if (uri.port > 0) uri.port else 51820
    val pubKey = uri.getQueryParameter("public_key") ?: ""
    val remark = uri.fragment ?: ""

    return ParsedVpnConfig(
      protocol = "WireGuard",
      host = host,
      port = port,
      uuidOrPassword = pubKey,
      remark = remark,
      rawUri = uriStr
    )
  }

  private fun parseHysteria2(uriStr: String): ParsedVpnConfig {
    val uri = Uri.parse(uriStr)
    val host = uri.host ?: "127.0.0.1"
    val port = if (uri.port > 0) uri.port else 443
    val password = uri.userInfo ?: ""
    val sni = uri.getQueryParameter("sni") ?: host
    val remark = uri.fragment ?: ""

    return ParsedVpnConfig(
      protocol = "Hysteria2",
      host = host,
      port = port,
      uuidOrPassword = password,
      sni = sni,
      remark = remark,
      rawUri = uriStr
    )
  }
}
