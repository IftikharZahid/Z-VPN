package com.zahidcodes.zvpn.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.core.PlayAppUpdateDetails
import com.zahidcodes.zvpn.core.PlayUpdateManager
import com.zahidcodes.zvpn.ui.components.ZvpnLogoMark
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkBgEnd
import com.zahidcodes.zvpn.ui.theme.DarkBgMid
import com.zahidcodes.zvpn.ui.theme.DarkBgStart
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceCard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceElevated
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextMuted
import com.zahidcodes.zvpn.ui.theme.TextMuted2
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import com.zahidcodes.zvpn.ui.theme.VibrantBlue
import com.zahidcodes.zvpn.ui.theme.WarningAmber

/**
 * Force update screen displayed when the installed app version is less than
 * the version published on Google Play Console. Prevents usage until updated.
 */
@Composable
fun ForceUpdateScreen(
  details: PlayAppUpdateDetails,
  onCheckAgain: () -> Unit,
  onDismissSimulation: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity = context as? Activity
  var isChecking by remember { mutableStateOf(false) }

  // Prevent user from backing out of forced update screen
  BackHandler(enabled = true) {
    // Cannot exit forced update
  }

  // Pulsing animation for the update shield badge
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  val backgroundBrush = Brush.verticalGradient(
    colors = listOf(DarkBgStart, DarkBgMid, DarkBgEnd)
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(backgroundBrush)
      .statusBarsPadding()
      .padding(horizontal = 24.dp, vertical = 20.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      Spacer(modifier = Modifier.height(12.dp))

      // Animated Cyber Shield & Update Indicator
      Box(
        modifier = Modifier
          .size(110.dp)
          .scale(pulseScale),
        contentAlignment = Alignment.Center
      ) {
        // Outer glowing ring
        Box(
          modifier = Modifier
            .size(110.dp)
            .clip(CircleShape)
            .background(CyanAccent.copy(alpha = 0.12f))
            .border(2.dp, CyanAccent.copy(alpha = 0.4f), CircleShape)
        )
        // Inner circle
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(
              Brush.radialGradient(
                colors = listOf(VibrantBlue.copy(alpha = 0.7f), DarkSurfaceElevated)
              )
            )
            .border(1.5.dp, CyanAccent, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Rounded.SystemUpdate,
            contentDescription = "System Update Required",
            tint = CyanAccent,
            modifier = Modifier.size(42.dp)
          )
        }
      }

      // Mandatory Update Badge
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(20.dp))
          .background(WarningAmber.copy(alpha = 0.15f))
          .border(1.dp, WarningAmber.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
          .padding(horizontal = 14.dp, vertical = 5.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.Warning,
            contentDescription = null,
            tint = WarningAmber,
            modifier = Modifier.size(14.dp)
          )
          Text(
            text = "GOOGLE PLAY CONSOLE UPDATE REQUIRED",
            color = WarningAmber,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
          )
        }
      }

      // Title & Subtitle
      Text(
        text = "Update to Continue",
        color = TextPrimary,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
      )

      Text(
        text = "Your current app version is out of date. To ensure maximum VPN security, protocol compatibility, and lightning speed, please update to the latest release on Google Play.",
        color = TextMuted,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 8.dp)
      )

      // Version Comparison Card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(DarkSurfaceCard)
          .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(16.dp))
          .padding(16.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text(
            text = "VERSION COMPARISON",
            color = CyanAccent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Installed Version",
                color = TextMuted,
                fontSize = 11.5.sp
              )
              Text(
                text = "v${details.currentVersionName} (Build ${details.currentVersionCode})",
                color = Color(0xFFFF6B6B),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace
              )
            }

            Text(
              text = "➔",
              color = TextMuted2,
              fontSize = 18.sp,
              fontWeight = FontWeight.Bold
            )

            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = "Play Console Version",
                color = TextMuted,
                fontSize = 11.5.sp
              )
              Text(
                text = "v${details.latestVersionName} (Build ${details.latestVersionCode})",
                color = OkEmerald,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      }

      // Release Notes Card
      if (details.releaseNotes.isNotBlank()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(16.dp))
            .padding(16.dp)
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = "WHAT'S NEW IN PLAY STORE",
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
            }
            Text(
              text = details.releaseNotes,
              color = TextMuted,
              fontSize = 12.5.sp,
              lineHeight = 17.sp
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // Primary Action: Update from Google Play Console
      Button(
        onClick = {
          if (activity != null && details.isPlayNativeAllowed) {
            PlayUpdateManager.launchPlayImmediateUpdate(activity)
          } else {
            PlayUpdateManager.openPlayStore(context)
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("force_update_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = Color.Transparent
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(),
        shape = RoundedCornerShape(14.dp)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.horizontalGradient(
                colors = listOf(CyanAccent, VibrantBlue)
              )
            ),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Rounded.CloudDownload,
              contentDescription = null,
              tint = Color(0xFF06101E),
              modifier = Modifier.size(20.dp)
            )
            Text(
              text = "Update from Google Play",
              color = Color(0xFF06101E),
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.3.sp
            )
          }
        }
      }

      // Secondary Action: Check Again / Refresh
      OutlinedButton(
        onClick = {
          isChecking = true
          onCheckAgain()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp)
          .testTag("check_again_button"),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.5f))
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (isChecking) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = CyanAccent,
              strokeWidth = 2.dp
            )
            Text(
              text = "Checking Play Console...",
              color = CyanAccent,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          } else {
            Icon(
              imageVector = Icons.Rounded.Refresh,
              contentDescription = null,
              tint = CyanAccent,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Check Again",
              color = CyanAccent,
              fontSize = 13.5.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // If in simulation mode, provide quick dismiss so tester can return
      if (onDismissSimulation != null) {
        OutlinedButton(
          onClick = onDismissSimulation,
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("exit_simulation_button"),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceStroke)
        ) {
          Text(
            text = "Exit Test Simulation",
            color = TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }

      Text(
        text = "Updates are verified & distributed via official Google Play Console channels.",
        color = TextMuted2,
        fontSize = 10.5.sp,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
      )
    }
  }
}
