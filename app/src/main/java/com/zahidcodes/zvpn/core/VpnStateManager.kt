package com.zahidcodes.zvpn.core

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.zahidcodes.zvpn.model.Server
import com.zahidcodes.zvpn.model.ServerCategory
import com.zahidcodes.zvpn.model.VpnStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object VpnStateManager {
  private const val TAG = "VpnStateManager"
  private const val PREFS_NAME = "zvpn_persistent_state"

  private const val KEY_IS_ACTIVE = "key_is_active"
  private const val KEY_SERVER_ID = "key_server_id"
  private const val KEY_SERVER_CITY = "key_server_city"
  private const val KEY_SERVER_COUNTRY = "key_server_country"
  private const val KEY_SERVER_COUNTRY_CODE = "key_server_country_code"
  private const val KEY_SERVER_IP = "key_server_ip"
  private const val KEY_SERVER_PORT = "key_server_port"
  private const val KEY_SERVER_PROTOCOL = "key_server_protocol"
  private const val KEY_SERVER_CONFIG_URI = "key_server_config_uri"
  private const val KEY_SERVER_UUID = "key_server_uuid"
  private const val KEY_CONNECTED_SINCE = "key_connected_since"
  private const val KEY_TOTAL_DL_BYTES = "key_total_dl_bytes"
  private const val KEY_TOTAL_UP_BYTES = "key_total_up_bytes"

  private val _vpnStatus = MutableStateFlow(VpnStatus.DISCONNECTED)
  val vpnStatus: StateFlow<VpnStatus> = _vpnStatus.asStateFlow()

  private val _activeServer = MutableStateFlow<Server?>(null)
  val activeServer: StateFlow<Server?> = _activeServer.asStateFlow()

  private val _connectedSinceMs = MutableStateFlow(0L)
  val connectedSinceMs: StateFlow<Long> = _connectedSinceMs.asStateFlow()

  private val _currentIp = MutableStateFlow("—")
  val currentIp: StateFlow<String> = _currentIp.asStateFlow()

  private val _downloadSpeed = MutableStateFlow("—")
  val downloadSpeed: StateFlow<String> = _downloadSpeed.asStateFlow()

  private val _uploadSpeed = MutableStateFlow("—")
  val uploadSpeed: StateFlow<String> = _uploadSpeed.asStateFlow()

  private val _totalDownloadedMb = MutableStateFlow(0.0)
  val totalDownloadedMb: StateFlow<Double> = _totalDownloadedMb.asStateFlow()

  private val _totalUploadedMb = MutableStateFlow(0.0)
  val totalUploadedMb: StateFlow<Double> = _totalUploadedMb.asStateFlow()

  private val _statusMessage = MutableStateFlow<String?>(null)
  val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

  private val _activeNetworkType = MutableStateFlow("Wi-Fi / Cellular")
  val activeNetworkType: StateFlow<String> = _activeNetworkType.asStateFlow()

  private val _reconnectCount = MutableStateFlow(0)
  val reconnectCount: StateFlow<Int> = _reconnectCount.asStateFlow()

  @Volatile
  var isServiceRunning: Boolean = false

  fun init(context: Context) {
    val prefs = getPrefs(context)
    val isActive = prefs.getBoolean(KEY_IS_ACTIVE, false)
    val connectedSince = prefs.getLong(KEY_CONNECTED_SINCE, 0L)
    val serverId = prefs.getString(KEY_SERVER_ID, null)
    val totalDl = prefs.getLong(KEY_TOTAL_DL_BYTES, 0L)
    val totalUp = prefs.getLong(KEY_TOTAL_UP_BYTES, 0L)

    _totalDownloadedMb.value = totalDl / (1024.0 * 1024.0)
    _totalUploadedMb.value = totalUp / (1024.0 * 1024.0)

    if (serverId != null) {
      val server = Server(
        id = serverId,
        city = prefs.getString(KEY_SERVER_CITY, "New York") ?: "New York",
        country = prefs.getString(KEY_SERVER_COUNTRY, "United States") ?: "United States",
        countryCode = prefs.getString(KEY_SERVER_COUNTRY_CODE, "US") ?: "US",
        pingMs = 24,
        loadPercent = 38,
        ipAddress = prefs.getString(KEY_SERVER_IP, "185.241.44.12") ?: "185.241.44.12",
        port = prefs.getInt(KEY_SERVER_PORT, 443),
        protocolSupport = prefs.getString(KEY_SERVER_PROTOCOL, "VLESS · REALITY") ?: "VLESS · REALITY",
        configUri = prefs.getString(KEY_SERVER_CONFIG_URI, "") ?: "",
        uuid = prefs.getString(KEY_SERVER_UUID, "") ?: ""
      )
      _activeServer.value = server
      _connectedSinceMs.value = connectedSince
      _currentIp.value = server.ipAddress

      if (isServiceRunning) {
        _vpnStatus.value = VpnStatus.CONNECTED
        Log.i(TAG, "Restored state: VPN Service is running for ${server.city}")
      } else if (isActive) {
        // App process was recreated while VPN service is still alive or recovering
        _vpnStatus.value = VpnStatus.CONNECTED
        Log.i(TAG, "Restored state: Active VPN connection recovered from persistence for ${server.city}")
      }
    }
  }

  fun onServiceStarted(server: Server, context: Context) {
    isServiceRunning = true
    _activeServer.value = server
    _vpnStatus.value = VpnStatus.CONNECTING
    _statusMessage.value = "Establishing secure encrypted tunnel..."
    Log.d(TAG, "Service started: CONNECTING to ${server.city}")
  }

  fun onTunnelConnected(server: Server, startTimeMs: Long, context: Context) {
    isServiceRunning = true
    _vpnStatus.value = VpnStatus.CONNECTED
    _activeServer.value = server
    _connectedSinceMs.value = startTimeMs
    _currentIp.value = server.ipAddress
    _statusMessage.value = "Full-device VPN connected: ${server.city} (${server.protocolSupport})"

    getPrefs(context).edit()
      .putBoolean(KEY_IS_ACTIVE, true)
      .putString(KEY_SERVER_ID, server.id)
      .putString(KEY_SERVER_CITY, server.city)
      .putString(KEY_SERVER_COUNTRY, server.country)
      .putString(KEY_SERVER_COUNTRY_CODE, server.countryCode)
      .putString(KEY_SERVER_IP, server.ipAddress)
      .putInt(KEY_SERVER_PORT, server.port)
      .putString(KEY_SERVER_PROTOCOL, server.protocolSupport)
      .putString(KEY_SERVER_CONFIG_URI, server.configUri)
      .putString(KEY_SERVER_UUID, server.uuid)
      .putLong(KEY_CONNECTED_SINCE, startTimeMs)
      .apply()

    Log.i(TAG, "Tunnel connected: ${server.city} at $startTimeMs")
  }

  fun onTunnelReconnecting(reason: String, attempt: Int) {
    _vpnStatus.value = VpnStatus.RECONNECTING
    _reconnectCount.value = attempt
    _statusMessage.value = "Reconnecting (Attempt $attempt): $reason"
    Log.w(TAG, "Reconnecting attempt $attempt: $reason")
  }

  fun onServiceStopped(context: Context, normalDisconnect: Boolean = true) {
    isServiceRunning = false
    _vpnStatus.value = VpnStatus.DISCONNECTED
    _downloadSpeed.value = "—"
    _uploadSpeed.value = "—"
    _connectedSinceMs.value = 0L
    _statusMessage.value = if (normalDisconnect) "Disconnected from VPN" else "VPN connection terminated"

    getPrefs(context).edit()
      .putBoolean(KEY_IS_ACTIVE, false)
      .putLong(KEY_CONNECTED_SINCE, 0L)
      .apply()

    Log.i(TAG, "Service stopped. Normal disconnect: $normalDisconnect")
  }

  fun updateTelemetry(dlBytesSec: Long, upBytesSec: Long, totalDlBytes: Long, totalUpBytes: Long, context: Context? = null) {
    val totalDlMb = totalDlBytes / (1024.0 * 1024.0)
    val totalUpMb = totalUpBytes / (1024.0 * 1024.0)

    _totalDownloadedMb.value = totalDlMb
    _totalUploadedMb.value = totalUpMb

    val dlMbps = (dlBytesSec * 8f) / 1_000_000f
    val upMbps = (upBytesSec * 8f) / 1_000_000f

    _downloadSpeed.value = if (dlMbps > 0.05f) String.format(Locale.US, "%.1f Mbps", dlMbps) else "—"
    _uploadSpeed.value = if (upMbps > 0.05f) String.format(Locale.US, "%.1f Mbps", upMbps) else "—"

    context?.let { ctx ->
      getPrefs(ctx).edit()
        .putLong(KEY_TOTAL_DL_BYTES, totalDlBytes)
        .putLong(KEY_TOTAL_UP_BYTES, totalUpBytes)
        .apply()
    }
  }

  fun updateNetworkType(type: String) {
    _activeNetworkType.value = type
  }

  fun setStatusMessage(msg: String?) {
    _statusMessage.value = msg
  }

  fun setCurrentIp(ip: String) {
    _currentIp.value = ip
  }

  fun clearStatusMessage() {
    _statusMessage.value = null
  }

  fun getPersistedServer(context: Context): Server? {
    val prefs = getPrefs(context)
    val serverId = prefs.getString(KEY_SERVER_ID, null) ?: return null
    return Server(
      id = serverId,
      city = prefs.getString(KEY_SERVER_CITY, "Global Server") ?: "Global Server",
      country = prefs.getString(KEY_SERVER_COUNTRY, "Global") ?: "Global",
      countryCode = prefs.getString(KEY_SERVER_COUNTRY_CODE, "US") ?: "US",
      pingMs = 24,
      loadPercent = 35,
      ipAddress = prefs.getString(KEY_SERVER_IP, "185.241.44.12") ?: "185.241.44.12",
      port = prefs.getInt(KEY_SERVER_PORT, 443),
      protocolSupport = prefs.getString(KEY_SERVER_PROTOCOL, "VLESS · REALITY") ?: "VLESS · REALITY",
      configUri = prefs.getString(KEY_SERVER_CONFIG_URI, "") ?: "",
      uuid = prefs.getString(KEY_SERVER_UUID, "") ?: ""
    )
  }

  fun isPersistedActive(context: Context): Boolean {
    return getPrefs(context).getBoolean(KEY_IS_ACTIVE, false)
  }

  private fun getPrefs(context: Context): SharedPreferences {
    return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }
}
