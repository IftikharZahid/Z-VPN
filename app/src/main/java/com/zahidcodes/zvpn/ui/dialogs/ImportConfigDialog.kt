package com.zahidcodes.zvpn.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.model.Server
import com.zahidcodes.zvpn.model.ServerCategory
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceCard
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceElevated
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextMuted
import com.zahidcodes.zvpn.ui.theme.TextMuted2
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import com.zahidcodes.zvpn.ui.theme.VibrantBlue
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImportConfigDialog(
  initialTab: Int = 0,
  onDismiss: () -> Unit,
  onImport: (String) -> Unit,
  onAddManualServer: (Server, Boolean) -> Unit = { _, _ -> }
) {
  var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab) }

  // Tab 0: Paste URI state
  var configText by remember { mutableStateOf("") }
  val clipboardManager = LocalClipboardManager.current

  // Tab 1: Manual Server Entry state
  var serverName by remember { mutableStateOf("") }
  var host by remember { mutableStateOf("") }
  var port by remember { mutableStateOf("443") }
  var selectedProtocol by remember { mutableStateOf("VLESS") }
  var uuidOrPassword by remember { mutableStateOf("") }
  var selectedSecurity by remember { mutableStateOf("REALITY") }
  var selectedNetwork by remember { mutableStateOf("TCP") }
  var sni by remember { mutableStateOf("") }
  var path by remember { mutableStateOf("/") }
  var encryption by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val protocols = listOf("VLESS", "Trojan", "VMess", "Shadowsocks", "WireGuard", "Hysteria2")
  val securityOptions = listOf("REALITY", "TLS", "None")
  val networkOptions = listOf("TCP", "HTTPUpgrade", "WebSocket", "gRPC")

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceElevated,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Rounded.AddCircle,
          contentDescription = null,
          tint = CyanAccent,
          modifier = Modifier.size(24.dp)
        )
        Text("Add VPN Configuration", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 480.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Tab Row Switcher: Paste URI vs Manual Entry
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = Color(0xFF091422),
          contentColor = CyanAccent,
          indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
              color = CyanAccent,
              height = 2.5.dp
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
        ) {
          Tab(
            selected = selectedTab == 0,
            onClick = {
              selectedTab = 0
              errorMessage = null
            },
            text = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(14.dp))
                Text("Paste URI", fontSize = 11.5.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
              }
            },
            selectedContentColor = CyanAccent,
            unselectedContentColor = TextMuted
          )

          Tab(
            selected = selectedTab == 1,
            onClick = {
              selectedTab = 1
              errorMessage = null
            },
            text = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(Icons.Rounded.Dns, contentDescription = null, modifier = Modifier.size(14.dp))
                Text("Manual Server", fontSize = 11.5.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
              }
            },
            selectedContentColor = CyanAccent,
            unselectedContentColor = TextMuted
          )
        }

        // Tab Content
        if (selectedTab == 0) {
          // --- TAB 0: PASTE URI ---
          Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Paste VLESS, Trojan, VMess, WireGuard, or Shadowsocks URIs (single or batch):",
              color = TextMuted,
              fontSize = 11.5.sp,
              lineHeight = 16.sp
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End
            ) {
              OutlinedButton(
                onClick = {
                  val clip = clipboardManager.getText()?.text
                  if (!clip.isNullOrBlank()) {
                    configText = if (configText.isBlank()) clip else "$configText\n$clip"
                  }
                },
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                modifier = Modifier.height(28.dp).testTag("paste_clipboard_button")
              ) {
                Icon(
                  imageVector = Icons.Rounded.ContentPaste,
                  contentDescription = "Paste from Clipboard",
                  tint = CyanAccent,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Paste from Clipboard", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.SemiBold)
              }

              if (configText.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                TextButton(
                  onClick = { configText = "" },
                  contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                  modifier = Modifier.height(28.dp)
                ) {
                  Text("Clear", fontSize = 10.sp, color = Color(0xFFEF4444))
                }
              }
            }

            OutlinedTextField(
              value = configText,
              onValueChange = { configText = it },
              placeholder = {
                Text(
                  "vless://uuid@host:port?path=/&security=none&type=httpupgrade#filembad-84\ntrojan://password@host:port...#Node-2",
                  color = TextMuted.copy(alpha = 0.5f),
                  fontSize = 11.sp
                )
              },
              modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 140.dp)
                .testTag("import_config_input"),
              textStyle = androidx.compose.ui.text.TextStyle(
                color = TextPrimary,
                fontSize = 11.5.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
              ),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = DarkSurfaceStroke,
                focusedContainerColor = Color(0x10FFFFFF),
                unfocusedContainerColor = Color(0x08FFFFFF)
              ),
              shape = RoundedCornerShape(10.dp)
            )
          }
        } else {
          // --- TAB 1: MANUAL SERVER / HOST DETAILS ---
          val scrollState = rememberScrollState()
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Protocol Selector Chips
            Text("Protocol", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              protocols.forEach { proto ->
                val isSelected = selectedProtocol == proto
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else Color(0xFF0F2035))
                    .border(1.dp, if (isSelected) CyanAccent else DarkSurfaceStroke, RoundedCornerShape(6.dp))
                    .clickable { selectedProtocol = proto }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = proto,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) CyanAccent else TextPrimary
                  )
                }
              }
            }

            // Server Name / Remark
            OutlinedTextField(
              value = serverName,
              onValueChange = { serverName = it },
              label = { Text("Server Name / Remark", fontSize = 11.sp) },
              placeholder = { Text("e.g. My Fast Tunnel", fontSize = 11.sp, color = TextMuted) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("manual_server_name_input"),
              textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 12.sp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = DarkSurfaceStroke
              )
            )

            // Host / IP and Port in a single row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = host,
                onValueChange = {
                  host = it
                  errorMessage = null
                },
                label = { Text("Server Host / IP *", fontSize = 11.sp) },
                placeholder = { Text("185.241.44.12 / domain", fontSize = 10.5.sp, color = TextMuted) },
                singleLine = true,
                modifier = Modifier.weight(1.5f).testTag("manual_host_input"),
                textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CyanAccent,
                  unfocusedBorderColor = DarkSurfaceStroke
                )
              )

              OutlinedTextField(
                value = port,
                onValueChange = { port = it.filter { char -> char.isDigit() } },
                label = { Text("Port *", fontSize = 11.sp) },
                placeholder = { Text("443", fontSize = 11.sp, color = TextMuted) },
                singleLine = true,
                modifier = Modifier.weight(0.7f).testTag("manual_port_input"),
                textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 12.sp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CyanAccent,
                  unfocusedBorderColor = DarkSurfaceStroke
                )
              )
            }

            // UUID / Password / Secret Key
            val credentialLabel = when (selectedProtocol) {
              "VLESS", "VMess" -> "UUID (Client ID) *"
              "Trojan", "Shadowsocks", "Hysteria2" -> "Password / Secret Key *"
              "WireGuard" -> "Public Key / Private Key *"
              else -> "Authentication Key *"
            }

            OutlinedTextField(
              value = uuidOrPassword,
              onValueChange = { uuidOrPassword = it },
              label = { Text(credentialLabel, fontSize = 11.sp) },
              placeholder = { Text("e.g. 162ff214-21c8-4963-a80a-6200cbad787e", fontSize = 10.sp, color = TextMuted) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("manual_uuid_input"),
              textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 11.5.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = DarkSurfaceStroke
              )
            )

            // Security Options
            Text("Security / TLS", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              securityOptions.forEach { sec ->
                val isSelected = selectedSecurity == sec
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) OkEmerald.copy(alpha = 0.2f) else Color(0xFF0F2035))
                    .border(1.dp, if (isSelected) OkEmerald else DarkSurfaceStroke, RoundedCornerShape(6.dp))
                    .clickable { selectedSecurity = sec }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = sec,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) OkEmerald else TextPrimary
                  )
                }
              }
            }

            // Transport Network
            Text("Transport Network", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              networkOptions.forEach { net ->
                val isSelected = selectedNetwork == net
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) VibrantBlue.copy(alpha = 0.25f) else Color(0xFF0F2035))
                    .border(1.dp, if (isSelected) VibrantBlue else DarkSurfaceStroke, RoundedCornerShape(6.dp))
                    .clickable { selectedNetwork = net }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                  Text(
                    text = net,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) VibrantBlue else TextPrimary
                  )
                }
              }
            }

            // Optional SNI and Path
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              OutlinedTextField(
                value = sni,
                onValueChange = { sni = it },
                label = { Text("SNI / Host Header", fontSize = 11.sp) },
                placeholder = { Text("e.g. play.google.com", fontSize = 10.sp, color = TextMuted) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 11.5.sp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CyanAccent,
                  unfocusedBorderColor = DarkSurfaceStroke
                )
              )

              OutlinedTextField(
                value = path,
                onValueChange = { path = it },
                label = { Text("Path", fontSize = 11.sp) },
                placeholder = { Text("/", fontSize = 11.sp, color = TextMuted) },
                singleLine = true,
                modifier = Modifier.weight(0.7f),
                textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 11.5.sp),
                colors = OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = CyanAccent,
                  unfocusedBorderColor = DarkSurfaceStroke
                )
              )
            }

            // Encryption / Cipher field
            OutlinedTextField(
              value = encryption,
              onValueChange = { encryption = it },
              label = { Text("Encryption / Cipher (Optional)", fontSize = 11.sp) },
              placeholder = { Text("none / mlkem768x25519plus... / chacha20", fontSize = 10.sp, color = TextMuted) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("manual_encryption_input"),
              textStyle = androidx.compose.ui.text.TextStyle(color = TextPrimary, fontSize = 11.5.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = DarkSurfaceStroke
              )
            )

            if (errorMessage != null) {
              Text(
                text = errorMessage!!,
                color = Color(0xFFEF4444),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }
      }
    },
    confirmButton = {
      if (selectedTab == 0) {
        // Tab 0 action
        Button(
          onClick = {
            if (configText.isNotBlank()) {
              onImport(configText)
            }
          },
          enabled = configText.isNotBlank(),
          colors = ButtonDefaults.buttonColors(
            containerColor = CyanAccent,
            disabledContainerColor = CyanAccent.copy(alpha = 0.3f)
          ),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier.testTag("confirm_import_button")
        ) {
          Text("Import Configs", color = Color.Black, fontWeight = FontWeight.Bold)
        }
      } else {
        // Tab 1 actions: Add vs Add & Connect
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Button(
            onClick = {
              if (host.isBlank()) {
                errorMessage = "Please enter Server Host / IP"
                return@Button
              }
              val cleanHost = host.trim()
              val cleanPort = port.toIntOrNull() ?: 443
              val name = serverName.ifBlank { "$cleanHost:$cleanPort" }
              val (country, countryCode) = when {
                cleanHost.endsWith(".ir") || cleanHost.contains("ir") -> "Iran" to "IR"
                cleanHost.endsWith(".de") || cleanHost.contains("de") -> "Germany" to "DE"
                cleanHost.endsWith(".nl") || cleanHost.contains("nl") -> "Netherlands" to "NL"
                cleanHost.endsWith(".uk") || cleanHost.contains("uk") -> "United Kingdom" to "GB"
                cleanHost.endsWith(".fr") || cleanHost.contains("fr") -> "France" to "FR"
                cleanHost.endsWith(".sg") || cleanHost.contains("sg") -> "Singapore" to "SG"
                cleanHost.endsWith(".jp") || cleanHost.contains("jp") -> "Japan" to "JP"
                cleanHost.endsWith(".ca") || cleanHost.contains("ca") -> "Canada" to "CA"
                cleanHost.endsWith(".tr") || cleanHost.contains("tr") -> "Turkey" to "TR"
                else -> "Custom Node" to "US"
              }

              val encParam = if (encryption.isNotBlank()) "&encryption=" + encryption.trim() else ""
              val generatedUri = when (selectedProtocol) {
                "VLESS" -> "vless://${uuidOrPassword.trim()}@$cleanHost:$cleanPort?security=${selectedSecurity.lowercase()}&type=${selectedNetwork.lowercase()}${if (sni.isNotBlank()) "&sni=" + sni.trim() else ""}${if (path.isNotBlank()) "&path=" + path.trim() else ""}$encParam#$name"
                "Trojan" -> "trojan://${uuidOrPassword.trim()}@$cleanHost:$cleanPort?security=tls&type=${selectedNetwork.lowercase()}${if (sni.isNotBlank()) "&sni=" + sni.trim() else ""}$encParam#$name"
                "VMess" -> "vmess://${uuidOrPassword.trim()}@$cleanHost:$cleanPort?security=tls&type=${selectedNetwork.lowercase()}#$name"
                "Shadowsocks" -> "ss://${uuidOrPassword.trim()}@$cleanHost:$cleanPort#$name"
                "WireGuard" -> "wg://${uuidOrPassword.trim()}@$cleanHost:$cleanPort#$name"
                else -> "vless://${uuidOrPassword.trim()}@$cleanHost:$cleanPort#$name"
              }

              val manualServer = Server(
                id = "manual-${System.currentTimeMillis()}",
                country = country,
                city = name,
                countryCode = countryCode,
                pingMs = Random.nextInt(18, 48),
                loadPercent = Random.nextInt(15, 45),
                ipAddress = cleanHost,
                category = ServerCategory.FASTEST,
                region = "Global",
                protocolSupport = "$selectedProtocol · ${selectedNetwork.uppercase()}",
                isImported = true,
                configUri = generatedUri,
                port = cleanPort,
                uuid = uuidOrPassword.trim()
              )
              onAddManualServer(manualServer, false)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132845)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp)).testTag("manual_add_server_btn")
          ) {
            Text("Add Server", color = CyanAccent, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
          }

          Button(
            onClick = {
              if (host.isBlank()) {
                errorMessage = "Please enter Server Host / IP"
                return@Button
              }
              val cleanHost = host.trim()
              val cleanPort = port.toIntOrNull() ?: 443
              val name = serverName.ifBlank { "$cleanHost:$cleanPort" }
              val (country, countryCode) = when {
                cleanHost.endsWith(".ir") || cleanHost.contains("ir") -> "Iran" to "IR"
                cleanHost.endsWith(".de") || cleanHost.contains("de") -> "Germany" to "DE"
                cleanHost.endsWith(".nl") || cleanHost.contains("nl") -> "Netherlands" to "NL"
                cleanHost.endsWith(".uk") || cleanHost.contains("uk") -> "United Kingdom" to "GB"
                cleanHost.endsWith(".fr") || cleanHost.contains("fr") -> "France" to "FR"
                cleanHost.endsWith(".sg") || cleanHost.contains("sg") -> "Singapore" to "SG"
                cleanHost.endsWith(".jp") || cleanHost.contains("jp") -> "Japan" to "JP"
                cleanHost.endsWith(".ca") || cleanHost.contains("ca") -> "Canada" to "CA"
                cleanHost.endsWith(".tr") || cleanHost.contains("tr") -> "Turkey" to "TR"
                else -> "Custom Node" to "US"
              }

              val encParam = if (encryption.isNotBlank()) "&encryption=" + encryption.trim() else ""
              val generatedUri = when (selectedProtocol) {
                "VLESS" -> "vless://${uuidOrPassword.trim()}@$cleanHost:$cleanPort?security=${selectedSecurity.lowercase()}&type=${selectedNetwork.lowercase()}${if (sni.isNotBlank()) "&sni=" + sni.trim() else ""}${if (path.isNotBlank()) "&path=" + path.trim() else ""}$encParam#$name"
                "Trojan" -> "trojan://${uuidOrPassword.trim()}@$cleanHost:$cleanPort?security=tls&type=${selectedNetwork.lowercase()}${if (sni.isNotBlank()) "&sni=" + sni.trim() else ""}$encParam#$name"
                "VMess" -> "vmess://${uuidOrPassword.trim()}@$cleanHost:$cleanPort?security=tls&type=${selectedNetwork.lowercase()}#$name"
                "Shadowsocks" -> "ss://${uuidOrPassword.trim()}@$cleanHost:$cleanPort#$name"
                "WireGuard" -> "wg://${uuidOrPassword.trim()}@$cleanHost:$cleanPort#$name"
                else -> "vless://${uuidOrPassword.trim()}@$cleanHost:$cleanPort#$name"
              }

              val manualServer = Server(
                id = "manual-${System.currentTimeMillis()}",
                country = country,
                city = name,
                countryCode = countryCode,
                pingMs = Random.nextInt(18, 48),
                loadPercent = Random.nextInt(15, 45),
                ipAddress = cleanHost,
                category = ServerCategory.FASTEST,
                region = "Global",
                protocolSupport = "$selectedProtocol · ${selectedNetwork.uppercase()}",
                isImported = true,
                configUri = generatedUri,
                port = cleanPort,
                uuid = uuidOrPassword.trim()
              )
              onAddManualServer(manualServer, true)
            },
            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.testTag("manual_add_and_connect_btn")
          ) {
            Icon(Icons.Rounded.Bolt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Add & Connect", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
          }
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMuted)
      }
    }
  )
}
