package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import com.example.ui.components.VpnScreen
import com.example.ui.components.ZvpnAppBar
import com.example.ui.components.ZvpnBottomBar
import com.example.ui.dialogs.ImportConfigDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ServersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBgEnd
import com.example.ui.theme.DarkBgMid
import com.example.ui.theme.DarkBgStart
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.ZvpnTheme
import com.example.viewmodel.VpnViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ZvpnTheme {
        ZvpnApp()
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

  val vpnStatus by viewModel.vpnStatus.collectAsState()
  val selectedServer by viewModel.selectedServer.collectAsState()
  val servers by viewModel.servers.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val selectedCategory by viewModel.selectedCategory.collectAsState()
  val selectedSortOption by viewModel.selectedSortOption.collectAsState()
  val currentIp by viewModel.currentIp.collectAsState()
  val downloadSpeed by viewModel.downloadSpeed.collectAsState()
  val uploadSpeed by viewModel.uploadSpeed.collectAsState()
  val sessionDuration by viewModel.sessionDurationSeconds.collectAsState()
  val trafficHistory by viewModel.trafficHistory.collectAsState()
  val totalDownloadedMb by viewModel.totalDownloadedMb.collectAsState()
  val totalUploadedMb by viewModel.totalUploadedMb.collectAsState()
  val settings by viewModel.settings.collectAsState()
  val statusMessage by viewModel.statusMessage.collectAsState()

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

  Scaffold(
    bottomBar = {
      ZvpnBottomBar(
        currentScreen = currentScreen,
        onScreenSelected = { currentScreen = it }
      )
    },
    containerColor = Color.Transparent
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(backgroundBrush)
        .padding(bottom = innerPadding.calculateBottomPadding())
        .statusBarsPadding()
    ) {
      androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        // Shared App Bar
        ZvpnAppBar(
          onImportClick = { showImportDialog = true },
          onTestPingClick = { viewModel.testPings() }
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
                  downloadSpeed = downloadSpeed,
                  uploadSpeed = uploadSpeed,
                  onToggleConnection = { viewModel.toggleConnection() },
                  onSelectServerClick = { currentScreen = VpnScreen.SERVERS }
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
                  onSearchChange = { viewModel.setSearchQuery(it) },
                  onCategorySelect = { viewModel.setCategory(it) },
                  onSortSelect = { viewModel.setSortOption(it) },
                  onServerSelect = { server, andConnect ->
                    viewModel.selectServer(server, andConnect)
                    currentScreen = VpnScreen.HOME
                  },
                  onToggleFavorite = { viewModel.toggleFavorite(it) },
                  onDeleteServer = { viewModel.deleteServer(it) },
                  onClearAllImported = { viewModel.clearAllImportedServers() },
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
      onDismiss = { showImportDialog = false },
      onImport = { rawConfigs ->
        viewModel.importConfigs(rawConfigs)
        showImportDialog = false
      }
    )
  }
}
