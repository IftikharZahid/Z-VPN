package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
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
import com.example.model.Server
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextMuted2
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VibrantBlue
import com.example.ui.theme.WarningAmber

@Composable
fun ServerCard(
  server: Server,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(18.dp)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(DarkSurfaceCard)
      .border(1.dp, DarkSurfaceStroke, shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp, vertical = 12.dp)
      .testTag("server_selection_card"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Flag
    FlagBadge(
      countryCode = server.countryCode,
      badgeSize = 40.dp
    )

    // Details
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = "SELECTED SERVER",
          fontSize = 9.5.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp,
          color = TextMuted2
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .background(VibrantBlue.copy(alpha = 0.2f))
            .padding(horizontal = 4.dp, vertical = 1.dp)
        ) {
          Text(
            text = "TAP TO CHANGE",
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent
          )
        }
      }

      Text(
        text = "${server.city}, ${server.country}",
        fontSize = 14.5.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Text(
        text = "${server.ipAddress} · ${server.region}",
        fontSize = 11.5.sp,
        color = TextMuted,
        maxLines = 1
      )
    }

    // Ping & Chevron
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      val pingColor = when {
        server.pingMs < 40 -> OkEmerald
        server.pingMs < 90 -> CyanAccent
        else -> WarningAmber
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(pingColor)
        )
        Text(
          text = "${server.pingMs} ms",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextMuted
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = "Change server",
        tint = TextMuted2,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}
