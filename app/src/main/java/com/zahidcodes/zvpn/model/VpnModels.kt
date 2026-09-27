package com.zahidcodes.zvpn.model

enum class VpnStatus {
  DISCONNECTED,
  CONNECTING,
  CONNECTED,
  RECONNECTING
}

enum class ServerCategory(val label: String) {
  ALL("All"),
  FASTEST("Fastest"),
  STREAMING("Streaming"),
  P2P("P2P"),
  SECURE("Secure Core")
}

enum class ServerSortOption(val label: String) {
  RECOMMENDED("Recommended"),
  PING("Lowest Ping"),
  LOAD("Lowest Load"),
  NAME("Country Name")
}

data class Server(
  val id: String,
  val country: String,
  val city: String,
  val countryCode: String,
  val pingMs: Int,
  val loadPercent: Int,
  val ipAddress: String,
  val category: ServerCategory = ServerCategory.ALL,
  val region: String = "Europe",
  val isFavorite: Boolean = false,
  val protocolSupport: String = "WireGuard · OpenVPN",
  val isImported: Boolean = false,
  val isOnline: Boolean = true,
  val isExpired: Boolean = false,
  val configUri: String = "",
  val port: Int = 443,
  val uuid: String = ""
) {
  val flagEmoji: String
    get() {
      if (countryCode.length != 2) return "🌐"
      return try {
        val firstChar = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
        String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
      } catch (e: Exception) {
        "🌐"
      }
    }
}

data class VpnTrafficPoint(
  val timestamp: Long,
  val downloadMbps: Float,
  val uploadMbps: Float
)

data class VpnSettings(
  val killSwitchEnabled: Boolean = true,
  val splitTunnelingEnabled: Boolean = false,
  val autoConnectWifi: Boolean = true,
  val protocol: String = "VLESS (REALITY)",
  val forceStealthProtocol: Boolean = true,
  val adBlockerEnabled: Boolean = true,
  val dnsLeakProtection: Boolean = true
)

data class IpDetails(
  val ip: String = "—",
  val country: String = "Detecting…",
  val countryCode: String = "",
  val city: String = "",
  val isp: String = "Scanning ISP details…",
  val isVpnProtected: Boolean = false,
  val lastUpdated: Long = 0L,
  val isLoading: Boolean = false
) {
  val flagEmoji: String
    get() {
      if (countryCode.length != 2) return "🌐"
      return try {
        val firstChar = Character.codePointAt(countryCode.uppercase(), 0) - 0x41 + 0x1F1E6
        val secondChar = Character.codePointAt(countryCode.uppercase(), 1) - 0x41 + 0x1F1E6
        String(Character.toChars(firstChar)) + String(Character.toChars(secondChar))
      } catch (e: Exception) {
        "🌐"
      }
    }
}
