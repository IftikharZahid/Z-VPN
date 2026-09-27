package com.zahidcodes.zvpn.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.model.IpDetails
import com.zahidcodes.zvpn.model.Server
import com.zahidcodes.zvpn.model.VpnSettings
import com.zahidcodes.zvpn.model.VpnStatus
import com.zahidcodes.zvpn.ui.components.ConnectionOrb
import com.zahidcodes.zvpn.ui.components.ConnectionTelemetryCard
import com.zahidcodes.zvpn.ui.components.ServerCard
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceCard
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextMuted
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import com.zahidcodes.zvpn.ui.theme.VibrantBlue
import com.zahidcodes.zvpn.ui.theme.WarningAmber

@Composable
fun HomeScreen(
  vpnStatus: VpnStatus,
  selectedServer: Server,
  ipAddress: String,
  ipDetails: IpDetails = IpDetails(),
  downloadSpeed: String,
  uploadSpeed: String,
  sessionSeconds: Long = 0L,
  totalDownloadedMb: Double = 0.0,
  totalUploadedMb: Double = 0.0,
  activeNetworkType: String = "Wi-Fi / Cellular",
  settings: VpnSettings = VpnSettings(),
  onToggleConnection: () -> Unit,
  onSelectServerClick: () -> Unit,
  onRefreshIp: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val titleColor by animateColorAsState(
    targetValue = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> TextPrimary
      VpnStatus.CONNECTING -> VibrantBlue
      VpnStatus.CONNECTED -> OkEmerald
      VpnStatus.RECONNECTING -> WarningAmber
    },
    label = "status_title_color"
  )

  val titleText = when (vpnStatus) {
    VpnStatus.DISCONNECTED -> "Not Connected"
    VpnStatus.CONNECTING -> "Connecting…"
    VpnStatus.CONNECTED -> "Connected"
    VpnStatus.RECONNECTING -> "Reconnecting…"
  }

  val subText = when (vpnStatus) {
    VpnStatus.DISCONNECTED -> "Tap button to secure your device traffic"
    VpnStatus.CONNECTING -> "Establishing high-speed encrypted tunnel"
    VpnStatus.CONNECTED -> "Your phone network traffic is fully protected"
    VpnStatus.RECONNECTING -> "Restoring encrypted tunnel connection"
  }

  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
      .padding(horizontal = 14.dp, vertical = 4.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 640.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Compact Hero Section with Power Orb & Timer
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp)
      ) {
        ConnectionOrb(
          vpnStatus = vpnStatus,
          onToggle = onToggleConnection
        )

        Text(
          text = titleText,
          color = titleColor,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = (-0.3).sp,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .padding(top = 4.dp, bottom = 1.dp)
            .testTag("connection_status_title")
        )

        Text(
          text = subText,
          color = TextMuted,
          fontSize = 11.sp,
          lineHeight = 14.sp,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .padding(horizontal = 12.dp)
            .testTag("connection_status_subtitle")
        )

        // Active Connection Timer Display
        if (vpnStatus == VpnStatus.CONNECTED) {
          val formattedTime = formatDuration(sessionSeconds)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
              .padding(top = 4.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(OkEmerald.copy(alpha = 0.12f))
              .border(1.dp, OkEmerald.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
              .padding(horizontal = 10.dp, vertical = 2.dp)
              .testTag("connection_timer_badge")
          ) {
            Icon(
              imageVector = Icons.Rounded.Timer,
              contentDescription = "Connection Duration",
              tint = OkEmerald,
              modifier = Modifier.size(12.dp)
            )
            Text(
              text = "CONNECTED DURATION  ·  $formattedTime",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = OkEmerald,
              letterSpacing = 0.4.sp
            )
          }
        }
      }

      // Selected Server Card (Ultra-Compact)
      ServerCard(
        server = selectedServer,
        onClick = onSelectServerClick
      )

      // Real-Time Telemetry Card (Download & Upload Speeds & Totals)
      ConnectionTelemetryCard(
        vpnStatus = vpnStatus,
        selectedServer = selectedServer,
        ipAddress = ipAddress,
        ipDetails = ipDetails,
        downloadSpeed = downloadSpeed,
        uploadSpeed = uploadSpeed,
        totalDownloadedMb = totalDownloadedMb,
        totalUploadedMb = totalUploadedMb,
        activeNetworkType = activeNetworkType,
        onRefreshIp = onRefreshIp
      )

      // Security / Protocol Footer Pill
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 6.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(DarkSurfaceCard)
          .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(8.dp))
          .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
          Icon(
            imageVector = if (vpnStatus == VpnStatus.CONNECTED) Icons.Rounded.Shield else Icons.Rounded.Lock,
            contentDescription = null,
            tint = if (vpnStatus == VpnStatus.CONNECTED) OkEmerald else CyanAccent,
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = if (vpnStatus == VpnStatus.CONNECTED) {
              if (settings.killSwitchEnabled) "${selectedServer.protocolSupport} · Kill Switch" else "${selectedServer.protocolSupport} Active"
            } else {
              if (settings.killSwitchEnabled) "Kill Switch Active · Encrypted" else "AES-256 Encryption Ready"
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
              if (vpnStatus == VpnStatus.CONNECTED) OkEmerald.copy(alpha = 0.15f)
              else Color(0x14FFFFFF)
            )
            .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
          Text(
            text = if (vpnStatus == VpnStatus.CONNECTED) "PROTECTED" else "READY",
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = if (vpnStatus == VpnStatus.CONNECTED) OkEmerald else TextMuted
          )
        }
      }
    }
  }
}

private fun formatDuration(seconds: Long): String {
  val hrs = seconds / 3600
  val mins = (seconds % 3600) / 60
  val secs = seconds % 60
  return if (hrs > 0) {
    String.format("%02d:%02d:%02d", hrs, mins, secs)
  } else {
    String.format("%02d:%02d", mins, secs)
  }
}
