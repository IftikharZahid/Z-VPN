package com.zahidcodes.zvpn.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Message
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.VpnLock
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.BuildConfig
import com.zahidcodes.zvpn.core.PlayUpdateManager
import com.zahidcodes.zvpn.core.PlayUpdateStatus
import com.zahidcodes.zvpn.model.VpnSettings

import com.zahidcodes.zvpn.ui.components.ZvpnLogoMark
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceCard
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceElevated
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextMuted
import com.zahidcodes.zvpn.ui.theme.TextMuted2
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import com.zahidcodes.zvpn.ui.theme.VibrantBlue

@Composable
fun SettingsScreen(
  settings: VpnSettings,
  onUpdateSettings: ((VpnSettings) -> VpnSettings) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val activity = context as? Activity
  val updateStatus by PlayUpdateManager.updateStatus.collectAsState()
  val isSimulated by PlayUpdateManager.isSimulatedForceUpdate.collectAsState()
  var showProtocolDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }

  fun openExternalLink(url: String) {
    try {
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
      context.startActivity(intent)
    } catch (e: Exception) {
      // Ignore if no handler available
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
  ) {
    // Header
    item {
      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
          text = "Security & Settings",
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Fine-tune your privacy shields and tunneling protocols",
          fontSize = 12.5.sp,
          color = TextMuted
        )
      }
    }

    // Pro Membership Card
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .background(
            Brush.linearGradient(
              colors = listOf(Color(0xFF132A4B), Color(0xFF0D1C33))
            )
          )
          .border(1.dp, CyanAccent.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(colors = listOf(CyanAccent, VibrantBlue))
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Rounded.Security,
              contentDescription = null,
              tint = Color(0xFF040E1B),
              modifier = Modifier.size(22.dp)
            )
          }

          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "ZVPN PRO",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(OkEmerald.copy(alpha = 0.2f))
                  .padding(horizontal = 5.dp, vertical = 1.dp)
              ) {
                Text(
                  text = "ACTIVE",
                  fontSize = 8.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = OkEmerald
                )
              }
            }
            Text(
              text = "Unlimited high-speed bandwidth · Zero Logs",
              fontSize = 11.5.sp,
              color = TextMuted
            )
          }
        }
      }
    }

    // Tunneling & Connection Group
    item {
      SettingsGroup(title = "CONNECTION & TUNNELING") {
        // Force Stealth Protocol Toggle
        SettingsToggleRow(
          icon = Icons.Rounded.Security,
          title = "Force Stealth Mode",
          subtitle = "Bypasses Deep Packet Inspection (DPI) & network censorship using VLESS/VMess/Trojan",
          checked = settings.forceStealthProtocol,
          onCheckedChange = { checked ->
            onUpdateSettings { it.copy(forceStealthProtocol = checked) }
          },
          testTag = "setting_force_stealth_toggle"
        )

        // Protocol Selector
        SettingsClickableRow(
          icon = Icons.Rounded.NetworkCheck,
          title = "Tunnel & Stealth Protocol",
          subtitle = settings.protocol,
          onClick = { showProtocolDialog = true },
          testTag = "setting_protocol_selector"
        )

        // Kill Switch
        SettingsToggleRow(
          icon = Icons.Rounded.VpnLock,
          title = "Kill Switch",
          subtitle = "Cut internet traffic immediately if VPN drops",
          checked = settings.killSwitchEnabled,
          onCheckedChange = { checked ->
            onUpdateSettings { it.copy(killSwitchEnabled = checked) }
          },
          testTag = "setting_kill_switch_toggle"
        )

        // Auto connect on public Wi-Fi
        SettingsToggleRow(
          icon = Icons.Rounded.Wifi,
          title = "Auto-Connect on Wi-Fi",
          subtitle = "Automatically encrypt untrusted networks",
          checked = settings.autoConnectWifi,
          onCheckedChange = { checked ->
            onUpdateSettings { it.copy(autoConnectWifi = checked) }
          },
          testTag = "setting_auto_connect_toggle"
        )

        // Split Tunneling
        SettingsToggleRow(
          icon = Icons.Rounded.Shield,
          title = "Split Tunneling",
          subtitle = "Allow selected apps to bypass VPN tunnel",
          checked = settings.splitTunnelingEnabled,
          onCheckedChange = { checked ->
            onUpdateSettings { it.copy(splitTunnelingEnabled = checked) }
          },
          testTag = "setting_split_tunneling_toggle"
        )
      }
    }

    // Privacy & Threat Shield Group
    item {
      SettingsGroup(title = "CYBER DEFENSE & PRIVACY") {
        // CleanWeb Ad & Malware Blocker
        SettingsToggleRow(
          icon = Icons.Rounded.Block,
          title = "CyberShield (Ad & Malware)",
          subtitle = "Blocks phishing domains, trackers, and intrusive ads",
          checked = settings.adBlockerEnabled,
          onCheckedChange = { checked ->
            onUpdateSettings { it.copy(adBlockerEnabled = checked) }
          },
          testTag = "setting_adblock_toggle"
        )

        // DNS Leak Protection
        SettingsToggleRow(
          icon = Icons.Rounded.Security,
          title = "DNS Leak Protection",
          subtitle = "Route all DNS requests through encrypted zero-log servers",
          checked = settings.dnsLeakProtection,
          onCheckedChange = { checked ->
            onUpdateSettings { it.copy(dnsLeakProtection = checked) }
          },
          testTag = "setting_dns_leak_toggle"
        )
      }
    }

    // Google Play Updates Group
    item {
      SettingsGroup(title = "UPDATES & GOOGLE PLAY CONSOLE") {
        val statusSubtitle = when (updateStatus) {
          is PlayUpdateStatus.Checking -> "Connecting to Google Play Console..."
          is PlayUpdateStatus.ForceUpdateRequired -> "⚠️ Update required! App version is older than Play Store"
          is PlayUpdateStatus.UpdateAvailable -> "New version ready on Google Play Store"
          is PlayUpdateStatus.UpToDate -> "Up to date with official Google Play Console"
          is PlayUpdateStatus.Error -> "Could not reach Play Store · Tap to retry"
          else -> "Tap to verify latest version on Play Console"
        }

        SettingsClickableRow(
          icon = Icons.Rounded.SystemUpdate,
          title = "Check for Play Console Updates",
          subtitle = "v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) · $statusSubtitle",
          onClick = {
            PlayUpdateManager.checkForUpdates(context, activity)
          },
          testTag = "setting_check_updates"
        )

        SettingsClickableRow(
          icon = Icons.Rounded.CloudDownload,
          title = "Open Google Play Store Page",
          subtitle = "View ZVPN on Google Play Store for new releases",
          onClick = {
            PlayUpdateManager.openPlayStore(context)
          },
          testTag = "setting_open_play_store"
        )

        SettingsToggleRow(
          icon = Icons.Rounded.Security,
          title = "Simulate Play Force Update",
          subtitle = "Preview mandatory update lock screen instantly (Test Mode)",
          checked = isSimulated,
          onCheckedChange = { enabled ->
            PlayUpdateManager.setSimulatedForceUpdate(enabled)
          },
          testTag = "setting_simulate_update_toggle"
        )
      }
    }

    // App & Developer Info Group
    item {
      SettingsGroup(title = "APP & DEVELOPER INFO") {
        // About Screen & Developer Option
        SettingsClickableRow(
          icon = Icons.Rounded.Info,
          title = "About ZVPN & Developer",
          subtitle = "Developer: Iftikhar Zahid · Client v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE}) · Support",
          onClick = { showAboutDialog = true }
        )
      }
    }
  }

  // Protocol Selection Dialog (Including VLESS, VMess, Trojan)
  if (showProtocolDialog) {
    val protocols = listOf(
      Triple("VLESS (REALITY)", "Lightweight zero-overhead TLS stealth protocol for high censorship", true),
      Triple("VMess (WebSocket / TLS)", "Obfuscated proxy protocol with dynamic VMess encryption", true),
      Triple("Trojan (gRPC / TLS)", "Camouflages VPN traffic as standard HTTPS web browsing", true),
      Triple("WireGuard", "Fastest, battery friendly state-of-the-art UDP tunneling", false),
      Triple("OpenVPN (UDP)", "High speed with optimal packet efficiency", false),
      Triple("OpenVPN (TCP)", "Stealth mode to bypass restrictive firewalls", false),
      Triple("ShadowSocks (AEAD)", "Classic encrypted SOCKS5 proxy protocol", false)
    )

    AlertDialog(
      onDismissRequest = { showProtocolDialog = false },
      containerColor = DarkSurfaceElevated,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.NetworkCheck,
            contentDescription = null,
            tint = CyanAccent,
            modifier = Modifier.size(20.dp)
          )
          Text("Select Protocol Core", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(
          modifier = Modifier.verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          protocols.forEach { (proto, desc, isStealth) ->
            val isSelected = settings.protocol == proto
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFF132A4B) else Color.Transparent)
                .border(
                  1.dp,
                  if (isSelected) CyanAccent.copy(alpha = 0.5f) else Color.Transparent,
                  RoundedCornerShape(10.dp)
                )
                .clickable {
                  onUpdateSettings { it.copy(protocol = proto) }
                  showProtocolDialog = false
                }
                .padding(vertical = 8.dp, horizontal = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = isSelected,
                onClick = {
                  onUpdateSettings { it.copy(protocol = proto) }
                  showProtocolDialog = false
                },
                colors = RadioButtonDefaults.colors(
                  selectedColor = CyanAccent,
                  unselectedColor = TextMuted2
                )
              )
              Column(modifier = Modifier.padding(start = 6.dp).weight(1f)) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(proto, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                  if (isStealth) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(OkEmerald.copy(alpha = 0.2f))
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                      Text("STEALTH", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = OkEmerald)
                    }
                  }
                }
                Text(desc, color = TextMuted, fontSize = 11.sp)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showProtocolDialog = false }) {
          Text("Done", color = CyanAccent, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // About Dialog with Professional WhatsApp & Facebook Social Action Buttons
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      containerColor = DarkSurfaceElevated,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          ZvpnLogoMark(size = 32.dp)
          Text("About ZVPN Client", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.padding(top = 4.dp)
        ) {
          // Developer Profile Card
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFF0F2038))
              .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
              .padding(12.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Icon(
                  imageVector = Icons.Rounded.Code,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(16.dp)
                )
                Text(
                  text = "DEVELOPER PROFILE",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = CyanAccent,
                  letterSpacing = 0.6.sp
                )
              }

              Text(
                text = "Iftikhar Zahid",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )

              Text(
                text = "Lead Systems & Mobile Software Engineer",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
              )

              Text(
                text = "Email: xahidcodes@gmail.com",
                fontSize = 11.sp,
                color = OkEmerald,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          Text(
            text = "ZVPN Client v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.5.sp
          )
          Text(
            text = "Engineered with modern Jetpack Compose & Android VpnService TUN core for ultra-low latency, zero logs, and high-speed encryption.",
            color = TextMuted,
            fontSize = 11.sp
          )

          // Professional Social Support Buttons: WhatsApp & Facebook
          Text(
            text = "CONNECT & SUPPORT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted2,
            letterSpacing = 0.6.sp,
            modifier = Modifier.padding(top = 4.dp)
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // WhatsApp Support Button
            Button(
              onClick = { openExternalLink("https://api.whatsapp.com/send?phone=923007971374&text=Hello%20Developer%20Iftikhar%20Zahid,%20I%20am%20using%20ZVPN!") },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Icon(
                  imageVector = Icons.Rounded.Message,
                  contentDescription = "WhatsApp",
                  tint = Color.White,
                  modifier = Modifier.size(15.dp)
                )
                Text("WhatsApp", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
              }
            }

            // Facebook Page Button
            Button(
              onClick = { openExternalLink("https://facebook.com/ZahidCodes") },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Icon(
                  imageVector = Icons.Rounded.Public,
                  contentDescription = "Facebook",
                  tint = Color.White,
                  modifier = Modifier.size(15.dp)
                )
                Text("Facebook", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showAboutDialog = false }) {
          Text("OK", color = CyanAccent, fontSize = 12.sp)
        }
      }
    )
  }
}

@Composable
private fun SettingsGroup(
  title: String,
  content: @Composable () -> Unit
) {
  val shape = RoundedCornerShape(14.dp)
  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = title,
      fontSize = 10.5.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.8.sp,
      color = TextMuted2,
      modifier = Modifier.padding(start = 2.dp)
    )
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(DarkSurfaceCard)
        .border(1.dp, DarkSurfaceStroke, shape)
        .padding(horizontal = 12.dp, vertical = 4.dp),
      verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      content()
    }
  }
}

@Composable
private fun SettingsToggleRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  testTag: String = ""
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0x12FFFFFF)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
      }

      Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
      }
    }

    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(
        checkedThumbColor = Color.White,
        checkedTrackColor = VibrantBlue,
        uncheckedThumbColor = TextMuted,
        uncheckedTrackColor = DarkSurfaceElevated
      ),
      modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier
    )
  }
}

@Composable
private fun SettingsClickableRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  testTag: String = ""
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 8.dp)
      .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(32.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0x12FFFFFF)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
      }

      Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text(text = subtitle, fontSize = 11.sp, color = TextMuted)
      }
    }

    Icon(
      imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
      contentDescription = null,
      tint = TextMuted2,
      modifier = Modifier.size(18.dp)
    )
  }
}
