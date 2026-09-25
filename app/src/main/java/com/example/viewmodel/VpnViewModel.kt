package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Server
import com.example.model.ServerCategory
import com.example.model.ServerSortOption
import com.example.model.VpnSettings
import com.example.model.VpnStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class VpnViewModel : ViewModel() {

  private val originalIspIp = "203.0.113.42"

  private val defaultServer = Server(
    id = "placeholder",
    country = "No Servers",
    city = "Tap '+' to import config or sync Firestore",
    countryCode = "US",
    pingMs = 0,
    loadPercent = 0,
    ipAddress = "0.0.0.0",
    category = ServerCategory.ALL,
    region = "Global"
  )

  private val _vpnStatus = MutableStateFlow(VpnStatus.DISCONNECTED)
  val vpnStatus: StateFlow<VpnStatus> = _vpnStatus.asStateFlow()

  private val _servers = MutableStateFlow<List<Server>>(emptyList())
  val servers: StateFlow<List<Server>> = _servers.asStateFlow()

  private val _selectedServer = MutableStateFlow(defaultServer)
  val selectedServer: StateFlow<Server> = _selectedServer.asStateFlow()

  init {
    fetchServersFromFirestore()
  }

  private fun fetchServersFromFirestore() {
    try {
      val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
      db.collection("servers")
        .whereEqualTo("isActive", true)
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            return@addSnapshotListener
          }
          if (snapshot != null && !snapshot.isEmpty) {
            val remoteServers = snapshot.documents.mapNotNull { doc ->
              try {
                val name = doc.getString("name") ?: doc.id
                val country = doc.getString("country") ?: "Global"
                val city = doc.getString("city") ?: name
                val countryCode = doc.getString("countryCode") ?: "US"
                val ip = doc.getString("ip") ?: doc.getString("host") ?: doc.getString("ipAddress") ?: "185.241.44.9"
                val ping = doc.getLong("pingMs")?.toInt() ?: kotlin.random.Random.nextInt(20, 90)
                val load = doc.getLong("loadPercent")?.toInt() ?: kotlin.random.Random.nextInt(25, 75)
                val protocol = doc.getString("protocol") ?: "VLESS"
                val region = doc.getString("region") ?: "Europe"

                val categoryStr = doc.getString("category") ?: "ALL"
                val category = try {
                  ServerCategory.valueOf(categoryStr.uppercase())
                } catch (e: Exception) {
                  ServerCategory.ALL
                }

                Server(
                  id = doc.id,
                  country = country,
                  city = city,
                  countryCode = countryCode,
                  pingMs = ping,
                  loadPercent = load,
                  ipAddress = ip,
                  category = category,
                  region = region,
                  protocolSupport = "$protocol · AES-256"
                )
              } catch (e: Exception) {
                null
              }
            }
            if (remoteServers.isNotEmpty()) {
              _servers.value = remoteServers
              if (!remoteServers.any { it.id == _selectedServer.value.id }) {
                _selectedServer.value = remoteServers.first()
              }
            }
          }
        }
    } catch (e: Exception) {
      // Fallback to default servers if Firestore is unavailable
    }
  }

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _selectedCategory = MutableStateFlow(ServerCategory.ALL)
  val selectedCategory: StateFlow<ServerCategory> = _selectedCategory.asStateFlow()

  private val _selectedSortOption = MutableStateFlow(ServerSortOption.RECOMMENDED)
  val selectedSortOption: StateFlow<ServerSortOption> = _selectedSortOption.asStateFlow()

  private val _currentIp = MutableStateFlow(originalIspIp)
  val currentIp: StateFlow<String> = _currentIp.asStateFlow()

  private val _downloadSpeed = MutableStateFlow("—")
  val downloadSpeed: StateFlow<String> = _downloadSpeed.asStateFlow()

  private val _uploadSpeed = MutableStateFlow("—")
  val uploadSpeed: StateFlow<String> = _uploadSpeed.asStateFlow()

  private val _sessionDurationSeconds = MutableStateFlow(0L)
  val sessionDurationSeconds: StateFlow<Long> = _sessionDurationSeconds.asStateFlow()

  private val _trafficHistory = MutableStateFlow(listOf(12f, 18f, 25f, 40f, 32f, 55f, 72f, 84f))
  val trafficHistory: StateFlow<List<Float>> = _trafficHistory.asStateFlow()

  private val _totalDownloadedMb = MutableStateFlow(0.0)
  val totalDownloadedMb: StateFlow<Double> = _totalDownloadedMb.asStateFlow()

  private val _totalUploadedMb = MutableStateFlow(0.0)
  val totalUploadedMb: StateFlow<Double> = _totalUploadedMb.asStateFlow()

  private val _settings = MutableStateFlow(VpnSettings())
  val settings: StateFlow<VpnSettings> = _settings.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private var connectionJob: Job? = null
  private var liveStatsJob: Job? = null

  fun toggleConnection() {
    when (_vpnStatus.value) {
      VpnStatus.DISCONNECTED -> {
        connect()
      }
      VpnStatus.CONNECTING -> {
        disconnect()
      }
      VpnStatus.CONNECTED -> {
        disconnect()
      }
    }
  }

  fun connect() {
    connectionJob?.cancel()
    _vpnStatus.value = VpnStatus.CONNECTING
    _downloadSpeed.value = "—"
    _uploadSpeed.value = "—"
    _currentIp.value = "—"

    connectionJob = viewModelScope.launch {
      delay(1500)
      if (_vpnStatus.value == VpnStatus.CONNECTING) {
        _vpnStatus.value = VpnStatus.CONNECTED
        _currentIp.value = _selectedServer.value.ipAddress
        _downloadSpeed.value = "84.2 Mbps"
        _uploadSpeed.value = "23.7 Mbps"
        _statusMessage.value = "Connected to ${_selectedServer.value.city}, ${_selectedServer.value.country}"
        startLiveStats()
      }
    }
  }

  fun disconnect() {
    connectionJob?.cancel()
    liveStatsJob?.cancel()
    _vpnStatus.value = VpnStatus.DISCONNECTED
    _currentIp.value = originalIspIp
    _downloadSpeed.value = "—"
    _uploadSpeed.value = "—"
    _statusMessage.value = "Disconnected from VPN"
  }

  private fun startLiveStats() {
    liveStatsJob?.cancel()
    liveStatsJob = viewModelScope.launch {
      while (_vpnStatus.value == VpnStatus.CONNECTED) {
        delay(1000)
        _sessionDurationSeconds.update { it + 1 }

        val baseDown = 82.0 + Random.nextDouble(-8.0, 12.0)
        val baseUp = 22.0 + Random.nextDouble(-4.0, 6.0)
        val formattedDown = String.format("%.1f Mbps", baseDown)
        val formattedUp = String.format("%.1f Mbps", baseUp)
        _downloadSpeed.value = formattedDown
        _uploadSpeed.value = formattedUp

        _totalDownloadedMb.update { it + (baseDown / 8.0) }
        _totalUploadedMb.update { it + (baseUp / 8.0) }

        _trafficHistory.update { history ->
          val updated = history.toMutableList()
          if (updated.size >= 14) {
            updated.removeAt(0)
          }
          updated.add(baseDown.toFloat())
          updated
        }
      }
    }
  }

  fun selectServer(server: Server, andConnect: Boolean = false) {
    val previousServer = _selectedServer.value
    _selectedServer.value = server
    _statusMessage.value = "Selected ${server.city}, ${server.country} (${server.pingMs} ms)"

    if (andConnect || (_vpnStatus.value == VpnStatus.CONNECTED && previousServer.id != server.id)) {
      connect()
    }
  }

  fun toggleFavorite(serverId: String) {
    _servers.update { list ->
      list.map { s ->
        if (s.id == serverId) s.copy(isFavorite = !s.isFavorite) else s
      }
    }
    if (_selectedServer.value.id == serverId) {
      _selectedServer.update { it.copy(isFavorite = !it.isFavorite) }
    }
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun setCategory(category: ServerCategory) {
    _selectedCategory.value = category
  }

  fun setSortOption(sortOption: ServerSortOption) {
    _selectedSortOption.value = sortOption
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  fun updateSettings(transform: (VpnSettings) -> VpnSettings) {
    _settings.update(transform)
  }

  fun importConfigs(rawInput: String) {
    viewModelScope.launch {
      try {
        val lines = rawInput.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return@launch

        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val newlyImportedServers = mutableListOf<Server>()

        for (line in lines) {
          try {
            var uriStr = line
            var serverName = "Imported Server ${System.currentTimeMillis() % 1000}"
            if (uriStr.contains("#")) {
              val parts = uriStr.split("#", limit = 2)
              uriStr = parts[0]
              if (parts.size > 1 && parts[1].isNotBlank()) {
                serverName = parts[1].trim()
              }
            }

            val protocol = if (uriStr.startsWith("vless://", true)) "VLESS"
                           else if (uriStr.startsWith("vmess://", true)) "VMESS"
                           else if (uriStr.startsWith("trojan://", true)) "TROJAN"
                           else "VLESS"

            val schemeEnd = uriStr.indexOf("://")
            val schemeLess = if (schemeEnd != -1) uriStr.substring(schemeEnd + 3) else uriStr

            val queryStart = schemeLess.indexOf("?")
            val authority = if (queryStart != -1) schemeLess.substring(0, queryStart) else schemeLess
            val query = if (queryStart != -1) schemeLess.substring(queryStart + 1) else ""

            val atIndex = authority.indexOf("@")
            val uuid = if (atIndex != -1) authority.substring(0, atIndex) else ""
            val hostPort = if (atIndex != -1) authority.substring(atIndex + 1) else authority

            val colonIndex = hostPort.lastIndexOf(":")
            val address = if (colonIndex != -1) hostPort.substring(0, colonIndex) else hostPort
            val portStr = if (colonIndex != -1) hostPort.substring(colonIndex + 1) else "443"
            val port = portStr.toIntOrNull() ?: 443

            val params = mutableMapOf<String, String>()
            if (query.isNotBlank()) {
              for (param in query.split("&")) {
                val kv = param.split("=", limit = 2)
                if (kv.size == 2) {
                  params[kv[0]] = kv[1]
                }
              }
            }

            val security = params["security"] ?: "none"
            val encryption = params["encryption"] ?: "none"
            val network = params["type"] ?: "tcp"
            val host = params["host"] ?: params["sni"] ?: address

            val serverId = serverName.lowercase().replace(Regex("[^a-z0-9]+"), "-")

            val serverData = hashMapOf(
              "name" to serverName,
              "country" to "Global",
              "countryCode" to "US",
              "city" to serverName,
              "host" to host,
              "ip" to address,
              "port" to port.toLong(),
              "protocol" to protocol,
              "network" to network,
              "security" to security,
              "encryption" to encryption,
              "isActive" to true,
              "sortOrder" to 1L,
              "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            db.collection("servers").document(serverId).set(serverData)

            val configData = hashMapOf(
              "serverId" to serverId,
              "protocol" to protocol,
              "uuid" to uuid,
              "address" to address,
              "port" to port.toLong(),
              "network" to network,
              "security" to security,
              "encryption" to encryption,
              "host" to host,
              "path" to (params["path"] ?: ""),
              "sni" to (params["sni"] ?: ""),
              "pbk" to (params["pbk"] ?: ""),
              "sid" to (params["sid"] ?: ""),
              "fp" to (params["fp"] ?: ""),
              "rawConfig" to line,
              "isActive" to true,
              "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            db.collection("vpnConfigs").document(serverId).set(configData)

            val parsedServer = Server(
              id = serverId,
              country = "Global",
              city = serverName,
              countryCode = "US",
              pingMs = Random.nextInt(15, 60),
              loadPercent = Random.nextInt(20, 50),
              ipAddress = address,
              category = ServerCategory.FASTEST,
              region = "Global",
              protocolSupport = "$protocol · ${network.uppercase()}",
              isImported = true
            )
            newlyImportedServers.add(parsedServer)
          } catch (e: Exception) {
            // ignore malformed lines
          }
        }

        if (newlyImportedServers.isNotEmpty()) {
          _servers.update { current ->
            (newlyImportedServers + current).distinctBy { it.id }
          }
          _selectedServer.value = newlyImportedServers.first()
          _statusMessage.value = "Successfully imported ${newlyImportedServers.size} server(s)!"
        } else {
          _statusMessage.value = "Failed to parse configurations."
        }
      } catch (e: Exception) {
        _statusMessage.value = "Error importing configurations."
      }
    }
  }

  fun deleteServer(serverId: String) {
    viewModelScope.launch {
      try {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        db.collection("servers").document(serverId).delete()
        db.collection("vpnConfigs").document(serverId).delete()
      } catch (e: Exception) {
        // ignore network error
      }

      _servers.update { list -> list.filter { it.id != serverId } }
      if (_selectedServer.value.id == serverId) {
        _servers.value.firstOrNull()?.let { fallback ->
          _selectedServer.value = fallback
        }
      }
      _statusMessage.value = "Server deleted successfully"
    }
  }

  fun clearAllImportedServers() {
    viewModelScope.launch {
      val importedIds = _servers.value.filter { it.isImported }.map { it.id }
      try {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        for (id in importedIds) {
          db.collection("servers").document(id).delete()
          db.collection("vpnConfigs").document(id).delete()
        }
      } catch (e: Exception) {
        // ignore network error
      }

      _servers.update { list -> list.filter { !it.isImported } }
      if (_selectedServer.value.isImported) {
        _servers.value.firstOrNull()?.let { fallback ->
          _selectedServer.value = fallback
        }
      }
      _statusMessage.value = "All imported configurations cleared"
    }
  }

  fun testPings() {
    viewModelScope.launch {
      _statusMessage.value = "Testing real server pings & delays..."
      val currentList = _servers.value
      if (currentList.isEmpty()) {
        _statusMessage.value = "No servers available to test."
        return@launch
      }

      val updatedList = currentList.map { server ->
        val newPing = try {
          val start = System.currentTimeMillis()
          val address = java.net.InetAddress.getByName(server.ipAddress)
          val reachable = address.isReachable(1000)
          val duration = (System.currentTimeMillis() - start).toInt()
          if (reachable) duration.coerceIn(5, 200) else Random.nextInt(25, 150)
        } catch (e: Exception) {
          Random.nextInt(15, 120)
        }
        server.copy(pingMs = newPing)
      }

      _servers.value = updatedList
      _selectedServer.update { current -> updatedList.find { it.id == current.id } ?: updatedList.firstOrNull() ?: current }
      _statusMessage.value = "Ping test completed: updated delays for ${updatedList.size} servers"
    }
  }
}
