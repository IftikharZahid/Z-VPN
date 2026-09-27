package com.zahidcodes.zvpn.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.ui.components.ZvpnLogoMark
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkBgEnd
import com.zahidcodes.zvpn.ui.theme.DarkBgMid
import com.zahidcodes.zvpn.ui.theme.DarkBgStart
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit
) {
  val scale = remember { Animatable(0.6f) }

  LaunchedEffect(true) {
    scale.animateTo(
      targetValue = 1.0f,
      animationSpec = spring(dampingRatio = 0.45f, stiffness = 200f)
    )
    delay(1200)
    onSplashFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(DarkBgStart, DarkBgMid, DarkBgEnd)
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.scale(scale.value)
    ) {
      ZvpnLogoMark(size = 96.dp)
      Spacer(modifier = Modifier.height(20.dp))
      Text(
        text = "ZVPN",
        color = TextPrimary,
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Secure & Lightning Fast VPN",
        color = CyanAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp
      )
    }
  }
}
