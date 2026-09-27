package com.zahidcodes.zvpn

import android.app.Activity
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zahidcodes.zvpn.model.VpnStatus
import com.zahidcodes.zvpn.ui.components.VpnScreen
import com.zahidcodes.zvpn.ui.components.ZvpnAppBar
import com.zahidcodes.zvpn.ui.components.ZvpnBottomBar
import com.zahidcodes.zvpn.ui.components.ZvpnNavigationRail
import com.zahidcodes.zvpn.ui.dialogs.ImportConfigDialog
import com.zahidcodes.zvpn.ui.screens.HomeScreen
import com.zahidcodes.zvpn.ui.screens.ServersScreen
import com.zahidcodes.zvpn.ui.screens.SettingsScreen
import com.zahidcodes.zvpn.ui.screens.StatsScreen
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkBgEnd
import com.zahidcodes.zvpn.ui.theme.DarkBgMid
import com.zahidcodes.zvpn.ui.theme.DarkBgStart
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceElevated
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import com.zahidcodes.zvpn.ui.theme.ZvpnTheme
import com.zahidcodes.zvpn.core.PlayUpdateManager
import com.zahidcodes.zvpn.core.PlayUpdateStatus
import com.zahidcodes.zvpn.ui.screens.ForceUpdateScreen
import com.zahidcodes.zvpn.viewmodel.VpnViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = androidx.activity.SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
    )

    // Initialize and trigger auto-update check from Google Play Console upon app startup
    PlayUpdateManager.init(this)
    PlayUpdateManager.checkForUpdates(this, this)

    setContent {
      ZvpnTheme {
        var showSplash by remember { mutableStateOf(true) }

        if (showSplash) {
          com.zahidcodes.zvpn.ui.screens.SplashScreen(
            onSplashFinished = { showSplash = false }
          )
        } else {
          ZvpnApp()
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
    // Resume in-progress update flow if user switched back to the app
    PlayUpdateManager.resumeUpdateIfInProgress(this)
  }

  @Deprecated("Deprecated in Java")
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == PlayUpdateManager.REQUEST_CODE_IMMEDIATE_UPDATE) {
      if (resultCode != Activity.RESULT_OK) {
        // If mandatory update was canceled or failed, re-trigger check to enforce update
        PlayUpdateManager.checkForUpdates(this, this)
      }
    }
  }
}


@Composable
fun ZvpnApp(
  viewModel: VpnViewModel = viewModel()
) {
  var currentScreen by remember { mutableStateOf(VpnScreen.HOME) }
  var showImportDialog by remember { mutableStateOf(false) }
  var importDialogInitialTab by remember { androidx.compose.runtime.mutableIntStateOf(0) }

  val vpnStatus by viewModel.vpnStatus.collectAsState()
  val selectedServer by viewModel.selectedServer.collectAsState()
  val servers by viewModel.servers.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val selectedSortOption by viewModel.selectedSortOption.collectAsState()
  val currentIp by viewModel.currentIp.collectAsState()
  val ipDetails by viewModel.ipDetails.collectAsState()
  val downloadSpeed by viewModel.downloadSpeed.collectAsState()
  val uploadSpeed by viewModel.uploadSpeed.collectAsState()
  val sessionDuration by viewModel.sessionDurationSeconds.collectAsState()
  val trafficHistory by viewModel.trafficHistory.collectAsState()
  val totalDownloadedMb by viewModel.totalDownloadedMb.collectAsState()
  val totalUploadedMb by viewModel.totalUploadedMb.collectAsState()
  val pingingServerIds by viewModel.pingingServerIds.collectAsState()
  val settings by viewModel.settings.collectAsState()
  val statusMessage by viewModel.statusMessage.collectAsState()
  val activeNetworkType by viewModel.activeNetworkType.collectAsState()

  // Auto dismiss status notification after 3 seconds
  LaunchedEffect(statusMessage) {
    if (statusMessage != null) {
      delay(3000)
      viewModel.clearStatusMessage()
    }
  }

  // Deep cyber gradient background matching HTML radial-gradient
  val backgroundBrush = Brush.verticalGradient(
    colors = listOf(
      DarkBgStart,
      DarkBgMid,
      DarkBgEnd
    )
  )

  val context = androidx.compose.ui.platform.LocalContext.current

  val vpnPrepareLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
      viewModel.connect(context)
    } else {
      viewModel.onPermissionDenied()
    }
  }

  fun requestConnection() {
    val prepareIntent = VpnService.prepare(context)
    if (prepareIntent != null) {
      vpnPrepareLauncher.launch(prepareIntent)
    } else {
      viewModel.connect(context)
    }
  }

  fun handleToggleConnection() {
    if (vpnStatus == VpnStatus.CONNECTED || vpnStatus == VpnStatus.CONNECTING || vpnStatus == VpnStatus.RECONNECTING) {
      viewModel.disconnect(context)
    } else {
      requestConnection()
    }
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val isWideScreen = maxWidth >= 600.dp

    Scaffold(
      bottomBar = {
        if (!isWideScreen) {
          ZvpnBottomBar(
            currentScreen = currentScreen,
            onScreenSelected = { currentScreen = it }
          )
        }
      },
      containerColor = Color.Transparent
    ) { innerPadding ->
      Row(
        modifier = Modifier
          .fillMaxSize()
          .background(backgroundBrush)
          .padding(bottom = if (!isWideScreen) innerPadding.calculateBottomPadding() else 0.dp)
          .statusBarsPadding()
      ) {
        if (isWideScreen) {
          ZvpnNavigationRail(
            currentScreen = currentScreen,
            onScreenSelected = { currentScreen = it },
            modifier = Modifier.width(88.dp)
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
          contentAlignment = Alignment.TopCenter
        ) {
          Column(
            modifier = Modifier
              .fillMaxHeight()
              .widthIn(max = 720.dp)
          ) {
            // Shared App Bar
            ZvpnAppBar(
              onImportClick = {
                importDialogInitialTab = 0
                showImportDialog = true
              },
              onAddManualServerClick = {
                importDialogInitialTab = 1
                showImportDialog = true
              },
              onTestPingClick = { viewModel.pingAllServers() }
            )

        // Screen Content with smooth crossfade
        Box(modifier = Modifier.weight(1f)) {
          Crossfade(
            targetState = currentScreen,
            animationSpec = tween(durationMillis = 220),
            label = "screen_crossfade"
          ) { screen ->
            when (screen) {
              VpnScreen.HOME -> {
                HomeScreen(
                  vpnStatus = vpnStatus,
                  selectedServer = selectedServer,
                  ipAddress = currentIp,
                  ipDetails = ipDetails,
                  downloadSpeed = downloadSpeed,
                  uploadSpeed = uploadSpeed,
                  sessionSeconds = sessionDuration,
                  totalDownloadedMb = totalDownloadedMb,
                  totalUploadedMb = totalUploadedMb,
                  activeNetworkType = activeNetworkType,
                  settings = settings,
                  onToggleConnection = { handleToggleConnection() },
                  onSelectServerClick = { currentScreen = VpnScreen.SERVERS },
                  onRefreshIp = { viewModel.fetchRealPublicIp() }
                )
              }
              VpnScreen.SERVERS -> {
                ServersScreen(
                  servers = servers,
                  selectedServer = selectedServer,
                  vpnStatus = vpnStatus,
                  searchQuery = searchQuery,
                  selectedCategory = selectedCategory,
                  selectedSortOption = selectedSortOption,
                  pingingServerIds = pingingServerIds,
                  onSearchChange = { viewModel.setSearchQuery(it) },
                  onCategorySelect = { viewModel.setCategory(it) },
                  onSortSelect = { viewModel.setSortOption(it) },
                  onServerSelect = { server, andConnect ->
                    viewModel.selectServer(server, false, context)
                    currentScreen = VpnScreen.HOME
                    if (andConnect) {
                      requestConnection()
                    }
                  },
                  onToggleFavorite = { viewModel.toggleFavorite(it) },
                  onPingServer = { viewModel.pingServer(it) },
                  onPingAll = { viewModel.pingAllServers() },
                  onDeleteServer = { viewModel.deleteServer(it) },
                  onClearAllImported = { viewModel.clearAllImportedServers() },
                  onImportClick = {
                    importDialogInitialTab = 0
                    showImportDialog = true
                  },
                  onAddManualClick = {
                    importDialogInitialTab = 1
                    showImportDialog = true
                  },
                  onUploadFirestore = { viewModel.uploadCurrentServersToFirestore() },
                  schemaJson = viewModel.getServerSchemaSampleJson(),
                  onBackClick = { currentScreen = VpnScreen.HOME }
                )
              }
              VpnScreen.STATS -> {
                StatsScreen(
                  vpnStatus = vpnStatus,
                  selectedServer = selectedServer,
                  sessionSeconds = sessionDuration,
                  downloadSpeed = downloadSpeed,
                  uploadSpeed = uploadSpeed,
                  totalDownloadedMb = totalDownloadedMb,
                  totalUploadedMb = totalUploadedMb,
                  trafficHistory = trafficHistory
                )
              }
              VpnScreen.SETTINGS -> {
                SettingsScreen(
                  settings = settings,
                  onUpdateSettings = { viewModel.updateSettings(it) }
                )
              }
            }
          }
        }
      }

          // Floating status toast notification
          Box(
            modifier = Modifier
              .align(Alignment.TopCenter)
              .padding(top = 10.dp, start = 16.dp, end = 16.dp)
          ) {
            androidx.compose.animation.AnimatedVisibility(
              visible = statusMessage != null,
              enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
              exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
              statusMessage?.let { msg ->
                Row(
                  modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = OkEmerald,
                    modifier = Modifier.size(18.dp)
                  )
                  Text(
                    text = msg,
                    color = TextPrimary,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  if (showImportDialog) {
    ImportConfigDialog(
      initialTab = importDialogInitialTab,
      onDismiss = { showImportDialog = false },
      onImport = { rawConfigs ->
        viewModel.importConfigs(rawConfigs, context)
        showImportDialog = false
      },
      onAddManualServer = { manualServer, andConnect ->
        viewModel.addManualServer(manualServer, false, context)
        showImportDialog = false
        if (andConnect) {
          requestConnection()
        }
      }
    )
  }
}
