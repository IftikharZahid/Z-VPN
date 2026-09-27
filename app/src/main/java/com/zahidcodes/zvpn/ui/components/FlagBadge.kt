package com.zahidcodes.zvpn.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FlagBadge(
  countryCode: String,
  modifier: Modifier = Modifier,
  badgeSize: Dp = 40.dp
) {
  val codeUpper = countryCode.uppercase().trim()
  val shape = RoundedCornerShape(8.dp)

  Box(
    modifier = modifier
      .size(badgeSize)
      .clip(shape)
      .border(1.dp, Color(0x33FFFFFF), shape)
      .background(Color(0xFF0F1A2A)),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height

      when (codeUpper) {
        "PK" -> { // Pakistan: Green with white bar and crescent
          drawRect(Color(0xFF004120), Offset(0f, 0f), Size(w, h))
          drawRect(Color.White, Offset(0f, 0f), Size(w * 0.25f, h))
          // Crescent & Star in center
          val cx = w * 0.62f
          val cy = h * 0.5f
          drawCircle(Color.White, radius = h * 0.26f, center = Offset(cx, cy))
          drawCircle(Color(0xFF004120), radius = h * 0.22f, center = Offset(cx + w * 0.06f, cy - h * 0.04f))
          drawCircle(Color.White, radius = h * 0.06f, center = Offset(cx + w * 0.12f, cy - h * 0.12f))
        }
        "IN" -> { // India: Saffron, White, Green
          drawRect(Color(0xFFFF9933), Offset(0f, 0f), Size(w, h / 3f))
          drawRect(Color.White, Offset(0f, h / 3f), Size(w, h / 3f))
          drawRect(Color(0xFF138808), Offset(0f, h * 2f / 3f), Size(w, h / 3f))
          drawCircle(Color(0xFF000080), radius = h * 0.12f, center = Offset(w / 2f, h / 2f), style = Stroke(width = 2f))
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
        "GB", "UK" -> { // United Kingdom
          drawRect(Color(0xFF012169), Offset(0f, 0f), Size(w, h))
          drawLine(Color.White, Offset(0f, 0f), Offset(w, h), strokeWidth = 4f)
          drawLine(Color.White, Offset(0f, h), Offset(w, 0f), strokeWidth = 4f)
          drawLine(Color(0xFFC8102E), Offset(0f, 0f), Offset(w, h), strokeWidth = 2f)
          drawLine(Color(0xFFC8102E), Offset(0f, h), Offset(w, 0f), strokeWidth = 2f)
          drawRect(Color.White, Offset(w * 0.38f, 0f), Size(w * 0.24f, h))
          drawRect(Color.White, Offset(0f, h * 0.38f), Size(w, h * 0.24f))
          drawRect(Color(0xFFC8102E), Offset(w * 0.42f, 0f), Size(w * 0.16f, h))
          drawRect(Color(0xFFC8102E), Offset(0f, h * 0.42f), Size(w, h * 0.16f))
        }
        "DE" -> { // Germany: Black, Red, Gold
          drawRect(Color(0xFF000000), Offset(0f, 0f), Size(w, h / 3f))
          drawRect(Color(0xFFFF0000), Offset(0f, h / 3f), Size(w, h / 3f))
          drawRect(Color(0xFFFFCC00), Offset(0f, h * 2f / 3f), Size(w, h / 3f))
        }
        "NL" -> { // Netherlands: Red, White, Blue
          drawRect(Color(0xFFAE1C28), Offset(0f, 0f), Size(w, h / 3f))
          drawRect(Color(0xFFFFFFFF), Offset(0f, h / 3f), Size(w, h / 3f))
          drawRect(Color(0xFF21468B), Offset(0f, h * 2f / 3f), Size(w, h / 3f))
        }
        "FR" -> { // France: Blue, White, Red vertical
          drawRect(Color(0xFF002395), Offset(0f, 0f), Size(w / 3f, h))
          drawRect(Color(0xFFFFFFFF), Offset(w / 3f, 0f), Size(w / 3f, h))
          drawRect(Color(0xFFED2939), Offset(w * 2f / 3f, 0f), Size(w / 3f, h))
        }
        "FI" -> { // Finland: Nordic Blue Cross
          drawRect(Color.White, Offset(0f, 0f), Size(w, h))
          drawRect(Color(0xFF002F6C), Offset(w * 0.3f, 0f), Size(w * 0.2f, h))
          drawRect(Color(0xFF002F6C), Offset(0f, h * 0.4f), Size(w, h * 0.22f))
        }
        "RO" -> { // Romania: Blue, Yellow, Red vertical
          drawRect(Color(0xFF002B7F), Offset(0f, 0f), Size(w / 3f, h))
          drawRect(Color(0xFFFCD116), Offset(w / 3f, 0f), Size(w / 3f, h))
          drawRect(Color(0xFFCE1126), Offset(w * 2f / 3f, 0f), Size(w / 3f, h))
        }
        "TR" -> { // Turkey: Red with White Crescent and Star
          drawRect(Color(0xFFE30A17), Offset(0f, 0f), Size(w, h))
          val cx = w * 0.45f
          val cy = h * 0.5f
          drawCircle(Color.White, radius = h * 0.26f, center = Offset(cx, cy))
          drawCircle(Color(0xFFE30A17), radius = h * 0.21f, center = Offset(cx + w * 0.06f, cy))
          drawCircle(Color.White, radius = h * 0.06f, center = Offset(cx + w * 0.18f, cy))
        }
        "ES" -> { // Spain: Red, Yellow, Red
          drawRect(Color(0xFFAA1523), Offset(0f, 0f), Size(w, h * 0.25f))
          drawRect(Color(0xFFF1BF00), Offset(0f, h * 0.25f), Size(w, h * 0.5f))
          drawRect(Color(0xFFAA1523), Offset(0f, h * 0.75f), Size(w, h * 0.25f))
        }
        "IT" -> { // Italy: Green, White, Red vertical
          drawRect(Color(0xFF009246), Offset(0f, 0f), Size(w / 3f, h))
          drawRect(Color(0xFFFFFFFF), Offset(w / 3f, 0f), Size(w / 3f, h))
          drawRect(Color(0xFFCE2B37), Offset(w * 2f / 3f, 0f), Size(w / 3f, h))
        }
        "CH" -> { // Switzerland
          drawRect(Color(0xFFD52B1E), Offset(0f, 0f), Size(w, h))
          drawRect(Color.White, Offset(w * 0.42f, h * 0.2f), Size(w * 0.16f, h * 0.6f))
          drawRect(Color.White, Offset(w * 0.2f, h * 0.42f), Size(w * 0.6f, h * 0.16f))
        }
        "JP" -> { // Japan
          drawRect(Color(0xFFFFFFFF), Offset(0f, 0f), Size(w, h))
          drawCircle(Color(0xFFBC002D), radius = h * 0.28f, center = Offset(w / 2f, h / 2f))
        }
        "SG" -> { // Singapore
          drawRect(Color(0xFFED2939), Offset(0f, 0f), Size(w, h / 2f))
          drawRect(Color(0xFFFFFFFF), Offset(0f, h / 2f), Size(w, h / 2f))
          drawCircle(Color.White, radius = h * 0.14f, center = Offset(w * 0.26f, h * 0.25f))
        }
        "CA" -> { // Canada
          drawRect(Color(0xFFFF0000), Offset(0f, 0f), Size(w * 0.28f, h))
          drawRect(Color(0xFFFFFFFF), Offset(w * 0.28f, 0f), Size(w * 0.44f, h))
          drawRect(Color(0xFFFF0000), Offset(w * 0.72f, 0f), Size(w * 0.28f, h))
          drawCircle(Color(0xFFFF0000), radius = h * 0.15f, center = Offset(w / 2f, h / 2f))
        }
        "AU" -> { // Australia
          drawRect(Color(0xFF00008B), Offset(0f, 0f), Size(w, h))
          drawRect(Color(0xFF012169), Offset(0f, 0f), Size(w * 0.45f, h * 0.5f))
          drawCircle(Color.White, radius = 2.5f, center = Offset(w * 0.75f, h * 0.3f))
          drawCircle(Color.White, radius = 2.5f, center = Offset(w * 0.65f, h * 0.6f))
          drawCircle(Color.White, radius = 3.5f, center = Offset(w * 0.22f, h * 0.75f))
        }
        "SE" -> { // Sweden
          drawRect(Color(0xFF006AA7), Offset(0f, 0f), Size(w, h))
          drawRect(Color(0xFFFECC00), Offset(w * 0.32f, 0f), Size(w * 0.16f, h))
          drawRect(Color(0xFFFECC00), Offset(0f, h * 0.42f), Size(w, h * 0.2f))
        }
        "KR" -> { // South Korea
          drawRect(Color(0xFFFFFFFF), Offset(0f, 0f), Size(w, h))
          drawCircle(Color(0xFFCD2E3A), radius = h * 0.24f, center = Offset(w / 2f, h * 0.42f))
          drawCircle(Color(0xFF0047A0), radius = h * 0.24f, center = Offset(w / 2f, h * 0.58f))
        }
        "BR" -> { // Brazil
          drawRect(Color(0xFF009B3A), Offset(0f, 0f), Size(w, h))
          drawCircle(Color(0xFFFEDF00), radius = h * 0.35f, center = Offset(w / 2f, h / 2f))
          drawCircle(Color(0xFF002776), radius = h * 0.2f, center = Offset(w / 2f, h / 2f))
        }
        else -> {
          // Professional Universal Cyber Globe & Node Badge for Custom / Imported Configs
          drawRect(Color(0xFF0B192C), Offset(0f, 0f), Size(w, h))
          drawCircle(Color(0xFF00E5FF).copy(alpha = 0.2f), radius = h * 0.42f, center = Offset(w / 2f, h / 2f))
          drawCircle(Color(0xFF00E5FF).copy(alpha = 0.6f), radius = h * 0.38f, center = Offset(w / 2f, h / 2f), style = Stroke(width = 1.5f))
          drawLine(Color(0xFF00E5FF).copy(alpha = 0.5f), Offset(w * 0.12f, h / 2f), Offset(w * 0.88f, h / 2f), strokeWidth = 1.5f)
          drawLine(Color(0xFF00E5FF).copy(alpha = 0.5f), Offset(w / 2f, h * 0.12f), Offset(w / 2f, h * 0.88f), strokeWidth = 1.5f)
          drawCircle(Color(0xFF00E5FF), radius = h * 0.12f, center = Offset(w / 2f, h / 2f))
          drawCircle(Color.White, radius = h * 0.05f, center = Offset(w / 2f, h / 2f))
        }
      }
    }

    // Overlay code text if code is custom 2 characters and not global
    if (codeUpper.length == 2 && codeUpper !in listOf("US", "GB", "DE", "NL", "PK", "IN", "FI", "RO", "TR", "ES", "IT", "FR", "CH", "JP", "SG", "CA", "AU", "SE", "KR", "BR")) {
      Text(
        text = codeUpper,
        fontSize = (badgeSize.value * 0.35f).sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }
  }
}
