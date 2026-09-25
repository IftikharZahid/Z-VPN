package com.example.model

import com.google.firebase.firestore.PropertyName

data class FirestoreCountry(
  @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
  @get:PropertyName("countryCode") @set:PropertyName("countryCode") var countryCode: String = "",
  @get:PropertyName("flag") @set:PropertyName("flag") var flag: String = "",
  @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
  @get:PropertyName("sortOrder") @set:PropertyName("sortOrder") var sortOrder: Int = 0
) {
  var id: String = ""
}

data class FirestoreLocation(
  @get:PropertyName("countryCode") @set:PropertyName("countryCode") var countryCode: String = "",
  @get:PropertyName("country") @set:PropertyName("country") var country: String = "",
  @get:PropertyName("city") @set:PropertyName("city") var city: String = "",
  @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
  @get:PropertyName("sortOrder") @set:PropertyName("sortOrder") var sortOrder: Int = 0
) {
  var id: String = ""
}

data class FirestoreServer(
  @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
  @get:PropertyName("country") @set:PropertyName("country") var country: String = "",
  @get:PropertyName("countryCode") @set:PropertyName("countryCode") var countryCode: String = "",
  @get:PropertyName("city") @set:PropertyName("city") var city: String = "",
  @get:PropertyName("flag") @set:PropertyName("flag") var flag: String = "",
  @get:PropertyName("address") @set:PropertyName("address") var address: String = "",
  @get:PropertyName("host") @set:PropertyName("host") var host: String = "",
  @get:PropertyName("port") @set:PropertyName("port") var port: Int = 443,
  @get:PropertyName("protocol") @set:PropertyName("protocol") var protocol: String = "VLESS",
  @get:PropertyName("network") @set:PropertyName("network") var network: String = "ws",
  @get:PropertyName("security") @set:PropertyName("security") var security: String = "none",
  @get:PropertyName("encryption") @set:PropertyName("encryption") var encryption: String = "none",
  @get:PropertyName("category") @set:PropertyName("category") var category: String = "FASTEST",
  @get:PropertyName("region") @set:PropertyName("region") var region: String = "Europe",
  @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true,
  @get:PropertyName("sortOrder") @set:PropertyName("sortOrder") var sortOrder: Int = 0
) {
  var id: String = ""
  
  fun toServerModel(): Server {
    val catEnum = try {
      ServerCategory.valueOf(category.uppercase())
    } catch (e: Exception) {
      ServerCategory.ALL
    }
    val ipOrHost = if (address.isNotBlank()) address else host
    return Server(
      id = id.ifBlank { name.lowercase().replace(" ", "-") },
      country = country,
      city = city,
      countryCode = countryCode,
      pingMs = 30,
      loadPercent = 40,
      ipAddress = ipOrHost,
      category = catEnum,
      region = region,
      protocolSupport = "$protocol · $security"
    )
  }
}

data class FirestoreVpnConfig(
  @get:PropertyName("serverId") @set:PropertyName("serverId") var serverId: String = "",
  @get:PropertyName("protocol") @set:PropertyName("protocol") var protocol: String = "VLESS",
  @get:PropertyName("uuid") @set:PropertyName("uuid") var uuid: String = "",
  @get:PropertyName("address") @set:PropertyName("address") var address: String = "",
  @get:PropertyName("port") @set:PropertyName("port") var port: Int = 443,
  @get:PropertyName("network") @set:PropertyName("network") var network: String = "ws",
  @get:PropertyName("security") @set:PropertyName("security") var security: String = "none",
  @get:PropertyName("encryption") @set:PropertyName("encryption") var encryption: String = "none",
  @get:PropertyName("host") @set:PropertyName("host") var host: String = "",
  @get:PropertyName("path") @set:PropertyName("path") var path: String = "",
  @get:PropertyName("sni") @set:PropertyName("sni") var sni: String = "",
  @get:PropertyName("fingerprint") @set:PropertyName("fingerprint") var fingerprint: String = "",
  @get:PropertyName("flow") @set:PropertyName("flow") var flow: String = "",
  @get:PropertyName("rawConfig") @set:PropertyName("rawConfig") var rawConfig: String = "",
  @get:PropertyName("isActive") @set:PropertyName("isActive") var isActive: Boolean = true
)
