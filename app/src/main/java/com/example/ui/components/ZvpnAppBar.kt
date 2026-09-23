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
import androidx.compose.material.icons.outlined.Notifications
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ZvpnAppBar(
  onNotificationsClick: () -> Unit,
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
        text = "Z-VPN",
        color = TextPrimary,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
      )
    }

    // Actions - Notification button only
    Box(
      modifier = Modifier
        .size(38.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(Color(0x0EFFFFFF))
        .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(12.dp))
        .clickable(onClick = onNotificationsClick)
        .testTag("app_bar_notifications_button"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Outlined.Notifications,
        contentDescription = "Notifications",
        tint = TextMuted,
        modifier = Modifier.size(19.dp)
      )

      // Unread badge dot
      Box(
        modifier = Modifier
          .size(7.dp)
          .align(Alignment.TopEnd)
          .padding(end = 4.dp, top = 4.dp)
          .clip(CircleShape)
          .background(CyanAccent)
      )
    }
  }
}
