package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FlagBadge(
  countryCode: String,
  modifier: Modifier = Modifier,
  badgeSize: Dp = 40.dp
) {
  val shape = RoundedCornerShape(12.dp)
  Box(
    modifier = modifier
      .size(badgeSize)
      .clip(shape)
      .border(1.dp, Color(0x22FFFFFF), shape)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      when (countryCode.uppercase()) {
        "NL" -> { // Netherlands: Red, White, Blue
          drawRect(Color(0xFFAE1C28), Offset(0f, 0f), Size(w, h / 3f))
          drawRect(Color(0xFFFFFFFF), Offset(0f, h / 3f), Size(w, h / 3f))
          drawRect(Color(0xFF21468B), Offset(0f, h * 2f / 3f), Size(w, h / 3f))
        }
        "DE" -> { // Germany: Black, Red, Gold
          drawRect(Color(0xFF000000), Offset(0f, 0f), Size(w, h / 3f))
          drawRect(Color(0xFFFF0000), Offset(0f, h / 3f), Size(w, h / 3f))
          drawRect(Color(0xFFFFCC00), Offset(0f, h * 2f / 3f), Size(w, h / 3f))
        }
        "US" -> { // United States
          val stripeH = h / 7f
          for (i in 0 until 7) {
            val color = if (i % 2 == 0) Color(0xFFB22234) else Color(0xFFFFFFFF)
            drawRect(color, Offset(0f, i * stripeH), Size(w, stripeH))
          }
          drawRect(Color(0xFF3C3B6E), Offset(0f, 0f), Size(w * 0.45f, stripeH * 4f))
          drawCircle(Color.White, radius = 2f, center = Offset(w * 0.22f, stripeH * 2f))
        }
        "GB" -> { // United Kingdom
          drawRect(Color(0xFF012169), Offset(0f, 0f), Size(w, h))
          // Diagonals
          drawLine(Color.White, Offset(0f, 0f), Offset(w, h), strokeWidth = 5f)
          drawLine(Color.White, Offset(0f, h), Offset(w, 0f), strokeWidth = 5f)
          drawLine(Color(0xFFC8102E), Offset(0f, 0f), Offset(w, h), strokeWidth = 2.5f)
          drawLine(Color(0xFFC8102E), Offset(0f, h), Offset(w, 0f), strokeWidth = 2.5f)
          // Cross
          drawRect(Color.White, Offset(w * 0.4f, 0f), Size(w * 0.2f, h))
          drawRect(Color.White, Offset(0f, h * 0.38f), Size(w, h * 0.24f))
          drawRect(Color(0xFFC8102E), Offset(w * 0.44f, 0f), Size(w * 0.12f, h))
          drawRect(Color(0xFFC8102E), Offset(0f, h * 0.42f), Size(w, h * 0.16f))
        }
        "CH" -> { // Switzerland: Red with White Cross
          drawRect(Color(0xFFD52B1E), Offset(0f, 0f), Size(w, h))
          drawRect(Color.White, Offset(w * 0.42f, h * 0.2f), Size(w * 0.16f, h * 0.6f))
          drawRect(Color.White, Offset(w * 0.2f, h * 0.42f), Size(w * 0.6f, h * 0.16f))
        }
        "JP" -> { // Japan: White with red circle
          drawRect(Color(0xFFFFFFFF), Offset(0f, 0f), Size(w, h))
          drawCircle(Color(0xFFBC002D), radius = h * 0.28f, center = Offset(w / 2f, h / 2f))
        }
        "SG" -> { // Singapore
          drawRect(Color(0xFFED2939), Offset(0f, 0f), Size(w, h / 2f))
          drawRect(Color(0xFFFFFFFF), Offset(0f, h / 2f), Size(w, h / 2f))
          drawCircle(Color.White, radius = h * 0.14f, center = Offset(w * 0.26f, h * 0.25f))
        }
        "CA" -> { // Canada: Red-White-Red
          drawRect(Color(0xFFFF0000), Offset(0f, 0f), Size(w * 0.28f, h))
          drawRect(Color(0xFFFFFFFF), Offset(w * 0.28f, 0f), Size(w * 0.44f, h))
          drawRect(Color(0xFFFF0000), Offset(w * 0.72f, 0f), Size(w * 0.28f, h))
          drawCircle(Color(0xFFFF0000), radius = h * 0.15f, center = Offset(w / 2f, h / 2f))
        }
        "AU" -> { // Australia
          drawRect(Color(0xFF00008B), Offset(0f, 0f), Size(w, h))
          drawRect(Color(0xFF012169), Offset(0f, 0f), Size(w * 0.45f, h * 0.5f))
          drawCircle(Color.White, radius = 3f, center = Offset(w * 0.75f, h * 0.3f))
          drawCircle(Color.White, radius = 3f, center = Offset(w * 0.65f, h * 0.6f))
          drawCircle(Color.White, radius = 4f, center = Offset(w * 0.22f, h * 0.75f))
        }
        "FR" -> { // France: Blue, White, Red vertical
          drawRect(Color(0xFF002395), Offset(0f, 0f), Size(w / 3f, h))
          drawRect(Color(0xFFFFFFFF), Offset(w / 3f, 0f), Size(w / 3f, h))
          drawRect(Color(0xFFED2939), Offset(w * 2f / 3f, 0f), Size(w / 3f, h))
        }
        "SE" -> { // Sweden: Blue with Yellow Cross
          drawRect(Color(0xFF006AA7), Offset(0f, 0f), Size(w, h))
          drawRect(Color(0xFFFECC00), Offset(w * 0.32f, 0f), Size(w * 0.16f, h))
          drawRect(Color(0xFFFECC00), Offset(0f, h * 0.42f), Size(w, h * 0.2f))
        }
        "KR" -> { // South Korea
          drawRect(Color(0xFFFFFFFF), Offset(0f, 0f), Size(w, h))
          drawCircle(Color(0xFFCD2E3A), radius = h * 0.24f, center = Offset(w / 2f, h * 0.42f))
          drawCircle(Color(0xFF0047A0), radius = h * 0.24f, center = Offset(w / 2f, h * 0.58f))
        }
        "BR" -> { // Brazil: Green with Yellow and Blue
          drawRect(Color(0xFF009B3A), Offset(0f, 0f), Size(w, h))
          drawCircle(Color(0xFFFEDF00), radius = h * 0.35f, center = Offset(w / 2f, h / 2f))
          drawCircle(Color(0xFF002776), radius = h * 0.2f, center = Offset(w / 2f, h / 2f))
        }
        else -> {
          drawRect(Color(0xFF1E293B), Offset(0f, 0f), Size(w, h))
          drawCircle(Color(0xFF38BDF8), radius = h * 0.25f, center = Offset(w / 2f, h / 2f))
        }
      }
    }
  }
}
