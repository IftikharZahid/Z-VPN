package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ZvpnAppBar(
  onImportClick: () -> Unit,
  onTestPingClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp)
      .padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Brand
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Brand Mark Shield
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(CyanAccent.copy(alpha = 0.12f))
          .border(1.dp, CyanAccent.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.Security,
          contentDescription = null,
          tint = CyanAccent,
          modifier = Modifier.size(22.dp)
        )
      }

      Text(
        text = "ZVPN",
        color = TextPrimary,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
      )
    }

    // Actions - Ping Test and Plus Import buttons
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Ping Test Button
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(OkEmerald.copy(alpha = 0.15f))
          .border(1.dp, OkEmerald.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
          .clickable(onClick = onTestPingClick)
          .testTag("app_bar_test_ping_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.NetworkPing,
          contentDescription = "Test Ping & Real Delay",
          tint = OkEmerald,
          modifier = Modifier.size(20.dp)
        )
      }

      // Plus Import Button
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(CyanAccent.copy(alpha = 0.15f))
          .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
          .clickable(onClick = onImportClick)
          .testTag("app_bar_import_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.Add,
          contentDescription = "Import Configuration",
          tint = CyanAccent,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
