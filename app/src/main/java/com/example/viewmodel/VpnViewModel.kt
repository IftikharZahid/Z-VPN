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

  private val defaultServers = listOf(
    Server(
      id = "nl-ams-1",
      country = "Netherlands",
      city = "Amsterdam",
      countryCode = "NL",
      pingMs = 24,
      loadPercent = 38,
      ipAddress = "185.241.44.9",
      category = ServerCategory.FASTEST,
      region = "Europe",
      isFavorite = true
    ),
    Server(
      id = "de-fra-1",
      country = "Germany",
      city = "Frankfurt",
      countryCode = "DE",
      pingMs = 28,
      loadPercent = 45,
      ipAddress = "194.36.191.12",
      category = ServerCategory.FASTEST,
      region = "Europe",
      isFavorite = true
    ),
    Server(
      id = "gb-lon-1",
      country = "United Kingdom",
      city = "London",
      countryCode = "GB",
      pingMs = 32,
      loadPercent = 50,
      ipAddress = "185.120.77.3",
      category = ServerCategory.P2P,
      region = "Europe",
      isFavorite = true
    ),
    Server(
      id = "fr-par-1",
      country = "France",
      city = "Paris",
      countryCode = "FR",
      pingMs = 33,
      loadPercent = 41,
      ipAddress = "195.154.122.8",
      category = ServerCategory.STREAMING,
      region = "Europe",
      isFavorite = false
    ),
    Server(
      id = "ch-zur-1",
      country = "Switzerland",
      city = "Zurich",
      countryCode = "CH",
      pingMs = 36,
      loadPercent = 29,
      ipAddress = "178.209.51.20",
      category = ServerCategory.SECURE,
      region = "Europe",
      isFavorite = true
    ),
    Server(
      id = "se-sto-1",
      country = "Sweden",
      city = "Stockholm",
      countryCode = "SE",
      pingMs = 42,
      loadPercent = 31,
      ipAddress = "193.180.119.5",
      category = ServerCategory.P2P,
      region = "Europe",
      isFavorite = false
    ),
    Server(
      id = "us-nyc-1",
      country = "United States",
      city = "New York",
      countryCode = "US",
      pingMs = 74,
      loadPercent = 62,
      ipAddress = "198.51.100.88",
      category = ServerCategory.STREAMING,
      region = "Americas",
      isFavorite = false
    ),
    Server(
      id = "us-chi-1",
      country = "United States",
      city = "Chicago",
      countryCode = "US",
      pingMs = 78,
      loadPercent = 49,
      ipAddress = "198.51.100.210",
      category = ServerCategory.FASTEST,
      region = "Americas",
      isFavorite = false
    ),
    Server(
      id = "ca-tor-1",
      country = "Canada",
      city = "Toronto",
      countryCode = "CA",
      pingMs = 82,
      loadPercent = 48,
      ipAddress = "142.44.212.180",
      category = ServerCategory.STREAMING,
      region = "Americas",
      isFavorite = false
    ),
    Server(
      id = "us-sfo-1",
      country = "United States",
      city = "San Francisco",
      countryCode = "US",
      pingMs = 89,
      loadPercent = 55,
      ipAddress = "198.51.100.142",
      category = ServerCategory.STREAMING,
      region = "Americas",
      isFavorite = false
    ),
    Server(
      id = "br-sao-1",
      country = "Brazil",
      city = "São Paulo",
      countryCode = "BR",
      pingMs = 165,
      loadPercent = 58,
      ipAddress = "177.54.144.10",
      category = ServerCategory.P2P,
      region = "Americas",
      isFavorite = false
    ),
    Server(
      id = "jp-tyo-1",
      country = "Japan",
      city = "Tokyo",
      countryCode = "JP",
      pingMs = 128,
      loadPercent = 42,
      ipAddress = "133.242.18.91",
      category = ServerCategory.STREAMING,
      region = "Asia Pacific",
      isFavorite = false
    ),
    Server(
      id = "kr-seo-1",
      country = "South Korea",
      city = "Seoul",
      countryCode = "KR",
      pingMs = 135,
      loadPercent = 47,
      ipAddress = "211.233.77.19",
      category = ServerCategory.FASTEST,
      region = "Asia Pacific",
      isFavorite = false
    ),
    Server(
      id = "sg-sin-1",
      country = "Singapore",
      city = "Singapore",
      countryCode = "SG",
      pingMs = 142,
      loadPercent = 33,
      ipAddress = "103.253.144.5",
      category = ServerCategory.P2P,
      region = "Asia Pacific",
      isFavorite = false
    ),
    Server(
      id = "au-syd-1",
      country = "Australia",
      city = "Sydney",
      countryCode = "AU",
      pingMs = 195,
      loadPercent = 27,
      ipAddress = "139.130.4.5",
      category = ServerCategory.SECURE,
      region = "Asia Pacific",
      isFavorite = false
    )
  )

  private val _vpnStatus = MutableStateFlow(VpnStatus.DISCONNECTED)
  val vpnStatus: StateFlow<VpnStatus> = _vpnStatus.asStateFlow()

  private val _servers = MutableStateFlow(defaultServers)
  val servers: StateFlow<List<Server>> = _servers.asStateFlow()

  private val _selectedServer = MutableStateFlow(defaultServers.first())
  val selectedServer: StateFlow<Server> = _selectedServer.asStateFlow()

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
}
