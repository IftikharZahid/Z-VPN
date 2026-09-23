package com.example.model

enum class VpnStatus {
  DISCONNECTED,
  CONNECTING,
  CONNECTED
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
  val protocolSupport: String = "WireGuard · OpenVPN"
)

data class VpnTrafficPoint(
  val timestamp: Long,
  val downloadMbps: Float,
  val uploadMbps: Float
)

data class VpnSettings(
  val killSwitchEnabled: Boolean = true,
  val splitTunnelingEnabled: Boolean = false,
  val autoConnectWifi: Boolean = true,
  val protocol: String = "WireGuard",
  val adBlockerEnabled: Boolean = true,
  val dnsLeakProtection: Boolean = true
)
