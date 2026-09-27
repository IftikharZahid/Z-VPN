package com.zahidcodes.zvpn.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.model.Server
import com.zahidcodes.zvpn.model.VpnStatus
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceCard
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextMuted
import com.zahidcodes.zvpn.ui.theme.TextMuted2
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import com.zahidcodes.zvpn.ui.theme.VibrantBlue

@Composable
fun ConnectionTelemetryCard(
  vpnStatus: VpnStatus,
  selectedServer: Server,
  ipAddress: String,
  downloadSpeed: String,
  uploadSpeed: String,
  totalDownloadedMb: Double,
  totalUploadedMb: Double,
  activeNetworkType: String = "Wi-Fi / Cellular",
  modifier: Modifier = Modifier
) {
  val isConnected = vpnStatus == VpnStatus.CONNECTED
  val isConnecting = vpnStatus == VpnStatus.CONNECTING || vpnStatus == VpnStatus.RECONNECTING

  val borderColor = when {
    isConnected -> OkEmerald.copy(alpha = 0.45f)
    isConnecting -> VibrantBlue.copy(alpha = 0.45f)
    else -> DarkSurfaceStroke
  }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(DarkSurfaceCard)
      .border(1.dp, borderColor, RoundedCornerShape(12.dp))
      .padding(10.dp)
      .testTag("connection_telemetry_card"),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Card Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Icon(
          imageVector = Icons.Rounded.Sensors,
          contentDescription = null,
          tint = if (isConnected) OkEmerald else CyanAccent,
          modifier = Modifier.size(15.dp)
        )
        Text(
          text = "REAL-TIME TELEMETRY",
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp,
          color = TextPrimary
        )
      }

      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(
            if (isConnected) OkEmerald.copy(alpha = 0.16f)
            else if (isConnecting) VibrantBlue.copy(alpha = 0.16f)
            else Color(0x1AFFFFFF)
          )
          .border(
            1.dp,
            if (isConnected) OkEmerald.copy(alpha = 0.4f)
            else if (isConnecting) VibrantBlue.copy(alpha = 0.4f)
            else DarkSurfaceStroke,
            RoundedCornerShape(20.dp)
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
              .background(
                if (isConnected) OkEmerald
                else if (isConnecting) VibrantBlue
                else TextMuted2
              )
          )
          Text(
            text = if (isConnected) "ACTIVE TUNNEL" else if (isConnecting) "SYNCING" else "IDLE",
            fontSize = 8.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isConnected) OkEmerald else if (isConnecting) CyanAccent else TextMuted
          )
        }
      }
    }

    HorizontalDivider(
      modifier = Modifier.fillMaxWidth(),
      thickness = 1.dp,
      color = DarkSurfaceStroke.copy(alpha = 0.6f)
    )

    // Data Transfer Grid (Download vs Upload)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Received Telemetry Block
      TelemetryMetricTile(
        title = "DOWNLOAD",
        rate = if (isConnected) downloadSpeed else "—",
        totalFormatted = formatDataAmount(if (isConnected) totalDownloadedMb else 0.0),
        icon = Icons.Rounded.ArrowDownward,
        accentColor = OkEmerald,
        modifier = Modifier.weight(1f)
      )

      // Sent Telemetry Block
      TelemetryMetricTile(
        title = "UPLOAD",
        rate = if (isConnected) uploadSpeed else "—",
        totalFormatted = formatDataAmount(if (isConnected) totalUploadedMb else 0.0),
        icon = Icons.Rounded.ArrowUpward,
        accentColor = CyanAccent,
        modifier = Modifier.weight(1f)
      )
    }

    HorizontalDivider(
      modifier = Modifier.fillMaxWidth(),
      thickness = 1.dp,
      color = DarkSurfaceStroke.copy(alpha = 0.6f)
    )

    // Protocol, IP & Network Technical Details
    Column(
      verticalArrangement = Arrangement.spacedBy(3.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      TelemetrySpecRow(
        icon = Icons.Rounded.Public,
        label = "Current IP Address",
        value = ipAddress,
        highlightColor = if (isConnected) OkEmerald else CyanAccent
      )

      TelemetrySpecRow(
        icon = Icons.Rounded.Wifi,
        label = "Active Transport",
        value = if (isConnected) "$activeNetworkType (Auto-Reconnect)" else activeNetworkType,
        highlightColor = if (isConnected) OkEmerald else CyanAccent
      )

      TelemetrySpecRow(
        icon = Icons.Rounded.Router,
        label = "Protocol & Core",
        value = selectedServer.protocolSupport
      )

      TelemetrySpecRow(
        icon = Icons.Rounded.Security,
        label = "Tunnel Interface",
        value = if (isConnected) "tun0 (Android VpnService)" else "Disconnected"
      )

      TelemetrySpecRow(
        icon = Icons.Rounded.Memory,
        label = "Encryption & MTU",
        value = if (isConnected) "AES-256-GCM · MTU 1500" else "Standard Socket"
      )

      TelemetrySpecRow(
        icon = Icons.Rounded.NetworkCheck,
        label = "Server Latency",
        value = if (selectedServer.isOnline) "${selectedServer.pingMs} ms (${selectedServer.city})" else "Offline"
      )
    }
  }
}

@Composable
private fun TelemetryMetricTile(
  title: String,
  rate: String,
  totalFormatted: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(Color(0xFF0C1929))
      .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(8.dp))
      .padding(8.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Box(
          modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.18f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(10.dp)
          )
        }

        Text(
          text = title,
          fontSize = 8.5.sp,
          fontWeight = FontWeight.Bold,
          color = TextMuted,
          letterSpacing = 0.4.sp
        )
      }

      Text(
        text = rate,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
      )

      Text(
        text = "TOTAL: $totalFormatted",
        fontSize = 8.5.sp,
        fontWeight = FontWeight.Medium,
        color = accentColor
      )
    }
  }
}

@Composable
private fun TelemetrySpecRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String,
  highlightColor: Color? = null
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = TextMuted2,
        modifier = Modifier.size(12.dp)
      )
      Text(
        text = label,
        fontSize = 10.5.sp,
        color = TextMuted,
        fontWeight = FontWeight.Medium
      )
    }

    Text(
      text = value,
      fontSize = 10.5.sp,
      fontWeight = FontWeight.SemiBold,
      color = highlightColor ?: TextPrimary,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}

private fun formatDataAmount(mb: Double): String {
  return when {
    mb <= 0.0 -> "0.0 MB"
    mb < 1024.0 -> String.format("%.1f MB", mb)
    else -> String.format("%.2f GB", mb / 1024.0)
  }
}
