package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Shield
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
import com.example.model.Server
import com.example.model.VpnStatus
import com.example.ui.components.ConnectionOrb
import com.example.ui.components.ServerCard
import com.example.ui.components.StatsRow
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VibrantBlue

@Composable
fun HomeScreen(
  vpnStatus: VpnStatus,
  selectedServer: Server,
  ipAddress: String,
  downloadSpeed: String,
  uploadSpeed: String,
  onToggleConnection: () -> Unit,
  onSelectServerClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val titleColor by animateColorAsState(
    targetValue = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> TextPrimary
      VpnStatus.CONNECTING -> VibrantBlue
      VpnStatus.CONNECTED -> OkEmerald
    },
    label = "status_title_color"
  )

  val titleText = when (vpnStatus) {
    VpnStatus.DISCONNECTED -> "Not Connected"
    VpnStatus.CONNECTING -> "Connecting…"
    VpnStatus.CONNECTED -> "Connected"
  }

  val subText = when (vpnStatus) {
    VpnStatus.DISCONNECTED -> "Tap the button to protect your connection"
    VpnStatus.CONNECTING -> "Securing your connection"
    VpnStatus.CONNECTED -> "Your connection is encrypted"
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(horizontal = 20.dp, vertical = 10.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // Top Hero section with Orb
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp, bottom = 18.dp)
    ) {
      ConnectionOrb(
        vpnStatus = vpnStatus,
        onToggle = onToggleConnection
      )

      Text(
        text = titleText,
        color = titleColor,
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.4).sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(top = 16.dp, bottom = 6.dp)
          .testTag("connection_status_title")
      )

      Text(
        text = subText,
        color = TextMuted,
        fontSize = 13.5.sp,
        lineHeight = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(horizontal = 28.dp)
          .testTag("connection_status_subtitle")
      )
    }

    // Middle & Bottom Sections
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Selected Server Card
      ServerCard(
        server = selectedServer,
        onClick = onSelectServerClick
      )

      // Live Stats Row
      StatsRow(
        ipAddress = ipAddress,
        downloadSpeed = downloadSpeed,
        uploadSpeed = uploadSpeed,
        vpnStatus = vpnStatus
      )

      // Security / Protocol Footer Pill
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .background(DarkSurfaceCard)
          .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(14.dp))
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = if (vpnStatus == VpnStatus.CONNECTED) Icons.Rounded.Shield else Icons.Rounded.Lock,
            contentDescription = null,
            tint = if (vpnStatus == VpnStatus.CONNECTED) OkEmerald else CyanAccent,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = if (vpnStatus == VpnStatus.CONNECTED) "AES-256 GCM · WireGuard Active" else "Standard Encryption Ready",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
          )
        }

        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
              if (vpnStatus == VpnStatus.CONNECTED) OkEmerald.copy(alpha = 0.15f)
              else Color(0x14FFFFFF)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
          Text(
            text = if (vpnStatus == VpnStatus.CONNECTED) "PROTECTED" else "READY",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp,
            color = if (vpnStatus == VpnStatus.CONNECTED) OkEmerald else TextMuted
          )
        }
      }
    }
  }
}
