package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VpnSettings
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextMuted2
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VibrantBlue

@Composable
fun SettingsScreen(
  settings: VpnSettings,
  onUpdateSettings: ((VpnSettings) -> VpnSettings) -> Unit,
  modifier: Modifier = Modifier
) {
  var showProtocolDialog by remember { mutableStateOf(false) }
  var showAccountDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp)
  ) {
    // Header
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Security & Settings",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Fine-tune your privacy shields and tunneling protocols",
          fontSize = 13.sp,
          color = TextMuted
        )
      }
    }

    // Pro Membership Card
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .background(
            Brush.linearGradient(
              colors = listOf(Color(0xFF132A4B), Color(0xFF0D1C33))
            )
          )
          .border(1.dp, CyanAccent.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
          .clickable { showAccountDialog = true }
          .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Box(
            modifier = Modifier
              .size(44.dp)
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
              modifier = Modifier.size(24.dp)
            )
          }

          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "Z-VPN PRO",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
              )
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(OkEmerald.copy(alpha = 0.2f))
                  .padding(horizontal = 6.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "ACTIVE",
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  color = OkEmerald
                )
              }
            }
            Text(
              text = "Unlimited high-speed bandwidth · 10 devices",
              fontSize = 12.sp,
              color = TextMuted
            )
          }
        }

        Icon(
          imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
          contentDescription = null,
          tint = TextMuted2
        )
      }
    }

    // Tunneling & Connection Group
    item {
      SettingsGroup(title = "CONNECTION & TUNNELING") {
        // Protocol
        SettingsClickableRow(
          icon = Icons.Rounded.NetworkCheck,
          title = "Tunnel Protocol",
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

    // About & Support Group
    item {
      SettingsGroup(title = "APP INFORMATION") {
        SettingsClickableRow(
          icon = Icons.Rounded.Devices,
          title = "Linked Devices",
          subtitle = "Manage authorized phone, tablet, and desktop slots",
          onClick = { showAccountDialog = true }
        )

        SettingsClickableRow(
          icon = Icons.Rounded.Info,
          title = "About Z-VPN",
          subtitle = "Version 1.0.0 (Build 2026.09) · Zero Logs",
          onClick = { showAboutDialog = true }
        )
      }
    }
  }

  // Protocol Selection Dialog
  if (showProtocolDialog) {
    val protocols = listOf(
      "WireGuard" to "Fastest, battery friendly, state-of-the-art cryptography",
      "OpenVPN (UDP)" to "High speed with optimal packet efficiency",
      "OpenVPN (TCP)" to "Stealth mode to bypass restrictive network firewalls",
      "IKEv2" to "Resilient reconnection during network transitions"
    )

    AlertDialog(
      onDismissRequest = { showProtocolDialog = false },
      containerColor = DarkSurfaceElevated,
      title = {
        Text("Select Tunnel Protocol", color = TextPrimary, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          protocols.forEach { (proto, desc) ->
            val isSelected = settings.protocol == proto
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                  onUpdateSettings { it.copy(protocol = proto) }
                  showProtocolDialog = false
                }
                .padding(vertical = 8.dp, horizontal = 4.dp),
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
              Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(proto, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(desc, color = TextMuted, fontSize = 11.5.sp)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showProtocolDialog = false }) {
          Text("Done", color = CyanAccent)
        }
      }
    )
  }

  // Account Dialog
  if (showAccountDialog) {
    AlertDialog(
      onDismissRequest = { showAccountDialog = false },
      containerColor = DarkSurfaceElevated,
      title = {
        Text("Account & Subscription", color = TextPrimary, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("User: xahidcodes@gmail.com", color = TextPrimary, fontSize = 14.sp)
          Text("Plan: Z-VPN Unlimited Pro (Annual)", color = CyanAccent, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          Text("Active Devices: 1 of 10 slots registered", color = TextMuted, fontSize = 12.sp)
          Text("Next Billing Date: September 23, 2027", color = TextMuted, fontSize = 12.sp)
        }
      },
      confirmButton = {
        TextButton(onClick = { showAccountDialog = false }) {
          Text("Close", color = CyanAccent)
        }
      }
    )
  }

  // About Dialog
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      containerColor = DarkSurfaceElevated,
      title = {
        Text("About Z-VPN", color = TextPrimary, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Z-VPN Client v1.0.0", color = TextPrimary, fontWeight = FontWeight.SemiBold)
          Text("Built with modern Jetpack Compose for uncompromising speed, privacy, and aesthetic clarity.", color = TextMuted, fontSize = 13.sp)
          Text("Strict No-Logs Guarantee: Audited independent RAM-only infrastructure.", color = OkEmerald, fontSize = 12.sp)
        }
      },
      confirmButton = {
        TextButton(onClick = { showAboutDialog = false }) {
          Text("OK", color = CyanAccent)
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
  val shape = RoundedCornerShape(20.dp)
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text(
      text = title,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.8.sp,
      color = TextMuted2,
      modifier = Modifier.padding(start = 4.dp)
    )
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(shape)
        .background(DarkSurfaceCard)
        .border(1.dp, DarkSurfaceStroke, shape)
        .padding(horizontal = 16.dp, vertical = 6.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
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
      .padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0x12FFFFFF)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
      }

      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text(text = subtitle, fontSize = 11.5.sp, color = TextMuted)
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
      .padding(vertical = 10.dp)
      .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(
      modifier = Modifier.weight(1f),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .size(36.dp)
          .clip(RoundedCornerShape(10.dp))
          .background(Color(0x12FFFFFF)),
        contentAlignment = Alignment.Center
      ) {
        Icon(imageVector = icon, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
      }

      Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Text(text = subtitle, fontSize = 11.5.sp, color = TextMuted)
      }
    }

    Icon(
      imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
      contentDescription = null,
      tint = TextMuted2,
      modifier = Modifier.size(20.dp)
    )
  }
}
