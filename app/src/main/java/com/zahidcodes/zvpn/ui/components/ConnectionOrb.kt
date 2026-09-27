package com.zahidcodes.zvpn.ui.components

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
import com.zahidcodes.zvpn.model.VpnStatus
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextMuted
import com.zahidcodes.zvpn.ui.theme.VibrantBlue
import com.zahidcodes.zvpn.ui.theme.WarningAmber

@Composable
fun ConnectionOrb(
  vpnStatus: VpnStatus,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "orb_animations")

  // Dash rotation animation
  val rotationDuration = when (vpnStatus) {
    VpnStatus.DISCONNECTED -> 24000
    VpnStatus.CONNECTING, VpnStatus.RECONNECTING -> 2200
    VpnStatus.CONNECTED -> 12000
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
    targetValue = 1.38f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "halo_scale"
  )

  val haloAlpha by infiniteTransition.animateFloat(
    initialValue = 0.7f,
    targetValue = 0f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "halo_alpha"
  )

  // Breathing animation when connecting
  val breatheScale by infiniteTransition.animateFloat(
    initialValue = 0.94f,
    targetValue = 1.06f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathe_scale"
  )

  val breatheAlpha by infiniteTransition.animateFloat(
    initialValue = 0.6f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "breathe_alpha"
  )

  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val pressScale = if (isPressed) 0.94f else 1.0f

  val ringColor by animateColorAsState(
    targetValue = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> Color(0x22FFFFFF)
      VpnStatus.CONNECTING -> VibrantBlue.copy(alpha = 0.75f)
      VpnStatus.CONNECTED -> OkEmerald.copy(alpha = 0.65f)
      VpnStatus.RECONNECTING -> WarningAmber.copy(alpha = 0.8f)
    },
    label = "ring_color"
  )

  val orbBorderColor by animateColorAsState(
    targetValue = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> Color(0x33FFFFFF)
      VpnStatus.CONNECTING -> VibrantBlue.copy(alpha = 0.9f)
      VpnStatus.CONNECTED -> OkEmerald.copy(alpha = 0.95f)
      VpnStatus.RECONNECTING -> WarningAmber.copy(alpha = 0.9f)
    },
    label = "orb_border_color"
  )

  val iconColor by animateColorAsState(
    targetValue = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> TextMuted
      VpnStatus.CONNECTING -> Color(0xFF93C5FD)
      VpnStatus.CONNECTED -> OkEmerald
      VpnStatus.RECONNECTING -> WarningAmber
    },
    label = "icon_color"
  )

  val accessibilityDesc = when (vpnStatus) {
    VpnStatus.DISCONNECTED -> "Connect VPN"
    VpnStatus.CONNECTING -> "Connecting to VPN"
    VpnStatus.CONNECTED -> "Disconnect VPN"
    VpnStatus.RECONNECTING -> "Reconnecting to VPN"
  }

  // Compact, professional Connect Button (176dp container with 130dp central orb)
  Box(
    modifier = modifier
      .size(176.dp)
      .semantics { contentDescription = accessibilityDesc }
      .clip(CircleShape)
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onToggle
      )
      .testTag("vpn_connect_button"),
    contentAlignment = Alignment.Center
  ) {
    // Halo wave when connected
    if (vpnStatus == VpnStatus.CONNECTED) {
      Box(
        modifier = Modifier
          .size(136.dp)
          .scale(haloScale)
          .border(2.dp, OkEmerald.copy(alpha = haloAlpha), CircleShape)
      )
    }

    // Outer Canvas for rotating rings
    Canvas(modifier = Modifier.size(170.dp)) {
      val center = Offset(size.width / 2f, size.height / 2f)
      val radius = (size.width / 2f) - 8.dp.toPx()

      // Inner static ring
      drawCircle(
        color = Color(0x18FFFFFF),
        radius = radius - 10.dp.toPx(),
        style = Stroke(width = 1.2.dp.toPx())
      )

      // Outer dashed spinning ring
      rotate(ringRotation, pivot = center) {
        drawCircle(
          color = ringColor,
          radius = radius,
          style = Stroke(
            width = 1.8.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
          )
        )
      }
    }

    // Central Glowing Power Orb Button
    val orbBrush = when (vpnStatus) {
      VpnStatus.DISCONNECTED -> Brush.radialGradient(
        colors = listOf(Color(0xFF233658), Color(0xFF142036), Color(0xFF0B1220))
      )
      VpnStatus.CONNECTING -> Brush.radialGradient(
        colors = listOf(Color(0xFF1E468A), Color(0xFF122C5C), Color(0xFF0A1833))
      )
      VpnStatus.CONNECTED -> Brush.radialGradient(
        colors = listOf(Color(0xFF124438), Color(0xFF0E2E27), Color(0xFF071915))
      )
      VpnStatus.RECONNECTING -> Brush.radialGradient(
        colors = listOf(Color(0xFF422C0E), Color(0xFF2E1E09), Color(0xFF1A1105))
      )
    }

    // Glow shadow backplate
    Box(
      modifier = Modifier
        .size(138.dp)
        .scale(pressScale)
        .clip(CircleShape)
        .background(
          when (vpnStatus) {
            VpnStatus.DISCONNECTED -> Color.Transparent
            VpnStatus.CONNECTING -> VibrantBlue.copy(alpha = 0.3f)
            VpnStatus.CONNECTED -> OkEmerald.copy(alpha = 0.4f)
            VpnStatus.RECONNECTING -> WarningAmber.copy(alpha = 0.35f)
          }
        )
    )

    Box(
      modifier = Modifier
        .size(130.dp)
        .scale(pressScale)
        .clip(CircleShape)
        .background(orbBrush)
        .border(1.8.dp, orbBorderColor, CircleShape)
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
          .size(100.dp)
          .clip(CircleShape)
          .background(
            Brush.radialGradient(
              colors = listOf(
                when (vpnStatus) {
                  VpnStatus.DISCONNECTED -> Color(0x22FFFFFF)
                  VpnStatus.CONNECTING -> Color(0x403B82F6)
                  VpnStatus.CONNECTED -> Color(0x402EE6A8)
                  VpnStatus.RECONNECTING -> Color(0x40F59E0B)
                },
                Color.Transparent
              )
            )
          )
      )

      val iconModifier = if (vpnStatus == VpnStatus.CONNECTING || vpnStatus == VpnStatus.RECONNECTING) {
        Modifier
          .size(42.dp)
          .scale(breatheScale)
      } else {
        Modifier.size(42.dp)
      }

      Icon(
        imageVector = Icons.Default.PowerSettingsNew,
        contentDescription = null,
        tint = iconColor.copy(
          alpha = if (vpnStatus == VpnStatus.CONNECTING || vpnStatus == VpnStatus.RECONNECTING) breatheAlpha else 1f
        ),
        modifier = iconModifier
      )
    }
  }
}
