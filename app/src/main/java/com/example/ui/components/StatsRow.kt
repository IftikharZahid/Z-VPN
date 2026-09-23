package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnStatus
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextMuted2
import com.example.ui.theme.TextPrimary

@Composable
fun StatsRow(
  ipAddress: String,
  downloadSpeed: String,
  uploadSpeed: String,
  vpnStatus: VpnStatus,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    StatItem(
      label = "IP ADDRESS",
      value = ipAddress,
      isConnected = vpnStatus == VpnStatus.CONNECTED,
      modifier = Modifier
        .weight(1f)
        .testTag("stat_ip_address")
    )
    StatItem(
      label = "DOWNLOAD",
      value = downloadSpeed,
      isConnected = vpnStatus == VpnStatus.CONNECTED,
      modifier = Modifier
        .weight(1f)
        .testTag("stat_download_speed")
    )
    StatItem(
      label = "UPLOAD",
      value = uploadSpeed,
      isConnected = vpnStatus == VpnStatus.CONNECTED,
      modifier = Modifier
        .weight(1f)
        .testTag("stat_upload_speed")
    )
  }
}

@Composable
private fun StatItem(
  label: String,
  value: String,
  isConnected: Boolean,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(16.dp)

  Column(
    modifier = modifier
      .clip(shape)
      .background(DarkSurfaceCard)
      .border(1.dp, DarkSurfaceStroke, shape)
      .padding(vertical = 12.dp, horizontal = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(5.dp)
  ) {
    Text(
      text = label,
      fontSize = 9.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.8.sp,
      color = TextMuted2,
      textAlign = TextAlign.Center
    )
    Text(
      text = value,
      fontSize = 12.5.sp,
      fontWeight = FontWeight.SemiBold,
      letterSpacing = (-0.2).sp,
      color = if (isConnected) Color(0xFFCBEFE2) else TextPrimary,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
}
