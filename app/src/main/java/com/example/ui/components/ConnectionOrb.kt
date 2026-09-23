package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.model.VpnStatus
import com.example.ui.theme.DarkBgMid
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.VibrantBlue

@Composable
fun ConnectionOrb(
  vpnStatus: VpnStatus,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "orb_animations")

  // Dash rotation animation
  val rotationDuration = when (vpnStatus) {
    VpnStatus.DISCONNECTED -> 26000
    VpnStatus.CONNECTING -> 2400
    VpnStatus.CONNECTED -> 14000
  }

  val ringRotation by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = rotationDuration, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "ring_rotation"
  )

  // Halo pulse animation when connected
  val haloScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.45f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "halo_scale"
  )

  val haloAlpha by infiniteTransition.animateFloat(
    initialValue = 0.65f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "halo_alpha"
  )

  // Breathing animation when connecting
  val breatheScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathe_scale"
  )

  val breatheAlpha by infiniteTransition.animateFloat(
    initialValue = 0.55f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathe_alpha"
  )

  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val pressScale = if (isPressed) 0.95f else 1.0f

  val ringColor by animateColorAsState(
    targetValue = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> Color(0x1AFFFFFF)
      VpnStatus.CONNECTING -> VibrantBlue.copy(alpha = 0.6f)
      VpnStatus.CONNECTED -> OkEmerald.copy(alpha = 0.5f)
    },
    label = "ring_color"
  )

  val orbBorderColor by animateColorAsState(
    targetValue = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> Color(0x22FFFFFF)
      VpnStatus.CONNECTING -> VibrantBlue.copy(alpha = 0.8f)
      VpnStatus.CONNECTED -> OkEmerald.copy(alpha = 0.9f)
    },
    label = "orb_border_color"
  )

  val iconColor by animateColorAsState(
    targetValue = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> TextMuted
      VpnStatus.CONNECTING -> Color(0xFF7FB4FF)
      VpnStatus.CONNECTED -> OkEmerald
    },
    label = "icon_color"
  )

  val accessibilityDesc = when (vpnStatus) {
    VpnStatus.DISCONNECTED -> "Connect VPN"
    VpnStatus.CONNECTING -> "Connecting to VPN"
    VpnStatus.CONNECTED -> "Disconnect VPN"
  }

  Box(
    modifier = modifier
      .size(240.dp)
      .semantics { contentDescription = accessibilityDesc },
    contentAlignment = Alignment.Center
  ) {
    // Halo wave when connected
    if (vpnStatus == VpnStatus.CONNECTED) {
      Box(
        modifier = Modifier
          .size(180.dp)
          .scale(haloScale)
          .border(1.5.dp, OkEmerald.copy(alpha = haloAlpha), CircleShape)
      )
    }

    // Outer Canvas for rotating rings
    Canvas(modifier = Modifier.size(230.dp)) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = (size.width / 2f) - 12.dp.toPx()

      // Inner static ring
      drawCircle(
        color = Color(0x14FFFFFF),
        radius = radius - 10.dp.toPx(),
        style = Stroke(width = 1.dp.toPx())
      )

      // Outer dashed spinning ring
      rotate(ringRotation, pivot = center) {
        drawCircle(
          color = ringColor,
          radius = radius,
          style = Stroke(
            width = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 18f), 0f)
          )
        )
      }
    }

    // Central Glowing Power Orb Button
    val orbBrush = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> Brush.radialGradient(
        colors = listOf(Color(0xFF1E2F4C), Color(0xFF111B2C), Color(0xFF090F1A))
      )
      VpnStatus.CONNECTING -> Brush.radialGradient(
        colors = listOf(Color(0xFF1B3D74), Color(0xFF10264B), Color(0xFF0A1424))
      )
      VpnStatus.CONNECTED -> Brush.radialGradient(
        colors = listOf(Color(0xFF103A30), Color(0xFF0D2520), Color(0xFF071513))
      )
    }

    // Glow shadow backplate
    Box(
      modifier = Modifier
        .size(182.dp)
        .scale(pressScale)
        .clip(CircleShape)
        .background(
          when (vpnStatus) {
            VpnStatus.DISCONNECTED -> Color.Transparent
            VpnStatus.CONNECTING -> VibrantBlue.copy(alpha = 0.25f)
            VpnStatus.CONNECTED -> OkEmerald.copy(alpha = 0.35f)
          }
        )
    )

    Box(
      modifier = Modifier
        .size(174.dp)
        .scale(pressScale)
        .clip(CircleShape)
        .background(orbBrush)
        .border(1.5.dp, orbBorderColor, CircleShape)
        .clickable(
          interactionSource = interactionSource,
          indication = null,
          onClick = onToggle
        )
        .testTag("vpn_connect_button"),
      contentAlignment = Alignment.Center
    ) {
      // Inner subtle radial highlight
      Box(
        modifier = Modifier
          .size(140.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(
                when (vpnStatus) {
                  VpnStatus.DISCONNECTED -> Color(0x18FFFFFF)
                  VpnStatus.CONNECTING -> Color(0x333B82F6)
                  VpnStatus.CONNECTED -> Color(0x332EE6A8)
                },
                Color.Transparent
              )
            )
          )
      )

      val iconModifier = if (vpnStatus == VpnStatus.CONNECTING) {
        Modifier
          .size(54.dp)
          .scale(breatheScale)
      } else {
        Modifier.size(54.dp)
      }

      Icon(
        imageVector = Icons.Default.PowerSettingsNew,
        contentDescription = null,
        tint = iconColor.copy(
          alpha = if (vpnStatus == VpnStatus.CONNECTING) breatheAlpha else 1f
        ),
        modifier = iconModifier
      )
    }
  }
}
