package com.zahidcodes.zvpn.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import kotlinx.coroutines.delay

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

@Composable
fun HomeScreenIpSummaryCard(
  vpnStatus: VpnStatus,
  selectedServer: Server,
  ipAddress: String,
  ipDetails: IpDetails,
  onRefreshIp: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isConnected = vpnStatus == VpnStatus.CONNECTED
  val clipboardManager = LocalClipboardManager.current
  var copied by remember { mutableStateOf(false) }

  LaunchedEffect(copied) {
    if (copied) {
      delay(2000L)
      copied = false
    }
  }

  val effectiveIp = when {
    ipDetails.ip != "—" && ipDetails.ip.isNotBlank() -> ipDetails.ip
    ipAddress != "—" && ipAddress.isNotBlank() -> ipAddress
    isConnected -> selectedServer.ipAddress
    else -> "Detecting IP…"
  }

  val locationSummary = when {
    ipDetails.city.isNotBlank() && ipDetails.country.isNotBlank() ->
      "${ipDetails.flagEmoji} ${ipDetails.city}, ${ipDetails.country}"
    ipDetails.country.isNotBlank() ->
      "${ipDetails.flagEmoji} ${ipDetails.country}"
    isConnected ->
      "${selectedServer.flagEmoji} ${selectedServer.city}, ${selectedServer.country}"
    else ->
      "Determining Location…"
  }

  val ispSummary = when {
    ipDetails.isp.isNotBlank() && ipDetails.isp != "Scanning ISP details…" ->
      ipDetails.isp
    isConnected ->
      "${selectedServer.protocolSupport} Encrypted Node"
    else ->
      "Direct Network ISP"
  }

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(DarkSurfaceCard)
      .border(
        width = 1.dp,
        color = if (isConnected) OkEmerald.copy(alpha = 0.5f) else CyanAccent.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp)
      )
      .padding(12.dp)
      .testTag("home_screen_ip_summary_card")
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      // Header: Public IP Label, Live Status Pill, Copy & Refresh
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.Public,
            contentDescription = null,
            tint = if (isConnected) OkEmerald else CyanAccent,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "CURRENT PUBLIC IP",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp,
            color = TextPrimary
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Status Pill: Protected vs Exposed
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(16.dp))
              .background(
                if (isConnected) OkEmerald.copy(alpha = 0.16f) else WarningAmber.copy(alpha = 0.16f)
              )
              .border(
                1.dp,
                if (isConnected) OkEmerald.copy(alpha = 0.4f) else WarningAmber.copy(alpha = 0.4f),
                RoundedCornerShape(16.dp)
              )
              .padding(horizontal = 7.dp, vertical = 2.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(5.dp)
                  .clip(CircleShape)
                  .background(if (isConnected) OkEmerald else WarningAmber)
              )
              Text(
                text = if (isConnected) "TUNNEL PROTECTED" else "UNENCRYPTED / EXPOSED",
                fontSize = 8.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.4.sp,
                color = if (isConnected) OkEmerald else WarningAmber
              )
            }
          }

          // Quick Refresh Button
          IconButton(
            onClick = onRefreshIp,
            modifier = Modifier.size(26.dp).testTag("home_summary_refresh_ip_btn")
          ) {
            if (ipDetails.isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(13.dp),
                color = CyanAccent,
                strokeWidth = 1.8.dp
              )
            } else {
              Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "Refresh Public IP Details",
                tint = CyanAccent,
                modifier = Modifier.size(15.dp)
              )
            }
          }
        }
      }

      // IP Display with Click-to-Copy
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF091424))
          .border(1.dp, Color(0x20FFFFFF), RoundedCornerShape(8.dp))
          .clickable {
            clipboardManager.setText(AnnotatedString(effectiveIp))
            copied = true
          }
          .padding(horizontal = 10.dp, vertical = 8.dp)
          .testTag("home_summary_ip_value_row"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = effectiveIp,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 0.5.sp,
            color = if (isConnected) OkEmerald else CyanAccent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = if (isConnected) "Masked by encrypted tunnel · Zero DNS leaks" else "Direct device uplink · Unmasked identity",
            fontSize = 9.5.sp,
            color = TextMuted
          )
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (copied) {
            Text(
              text = "COPIED",
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold,
              color = OkEmerald
            )
            Icon(
              imageVector = Icons.Rounded.Check,
              contentDescription = "Copied",
              tint = OkEmerald,
              modifier = Modifier.size(14.dp)
            )
          } else {
            Icon(
              imageVector = Icons.Rounded.ContentCopy,
              contentDescription = "Copy IP",
              tint = TextMuted,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }

      // Geo Location and ISP details row
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = locationSummary,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f, fill = false)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = ispSummary,
          fontSize = 10.5.sp,
          color = TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

