package com.zahidcodes.zvpn.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.model.Server
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceCard
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextMuted
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import com.zahidcodes.zvpn.ui.theme.VibrantBlue
import com.zahidcodes.zvpn.ui.theme.WarningAmber

@Composable
fun ServerCard(
  server: Server,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(12.dp)

  Row(
    modifier = modifier
      .fillMaxWidth()
      .clip(shape)
      .background(DarkSurfaceCard)
      .border(1.dp, DarkSurfaceStroke, shape)
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp, vertical = 6.dp)
      .testTag("server_selection_card"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Flag Badge
    FlagBadge(
      countryCode = server.countryCode,
      badgeSize = 30.dp
    )

    // Details Column (Left side)
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
      Text(
        text = "${server.city}, ${server.country}",
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        val pingColor = when {
          server.pingMs < 40 -> OkEmerald
          server.pingMs < 90 -> CyanAccent
          else -> WarningAmber
        }

        Box(
          modifier = Modifier
            .size(5.dp)
            .clip(CircleShape)
            .background(pingColor)
        )

        Text(
          text = "${server.pingMs} ms",
          fontSize = 10.5.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextMuted
        )

        Text(
          text = "· ${server.ipAddress}",
          fontSize = 10.5.sp,
          color = TextMuted,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // Right Side: TAP TO CHANGE Badge & Arrow
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(VibrantBlue.copy(alpha = 0.18f))
          .border(1.dp, CyanAccent.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
          .padding(horizontal = 6.dp, vertical = 2.dp)
      ) {
        Text(
          text = "TAP TO CHANGE",
          fontSize = 8.5.sp,
          fontWeight = FontWeight.Bold,
          color = CyanAccent,
          maxLines = 1
        )
      }

      Icon(
        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
        contentDescription = "Select Server",
        tint = CyanAccent,
        modifier = Modifier.size(16.dp)
      )
    }
  }
}
