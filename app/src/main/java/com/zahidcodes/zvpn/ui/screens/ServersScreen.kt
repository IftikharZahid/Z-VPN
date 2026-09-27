package com.zahidcodes.zvpn.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.model.Server
import com.zahidcodes.zvpn.model.ServerCategory
import com.zahidcodes.zvpn.model.ServerSortOption
import com.zahidcodes.zvpn.model.VpnStatus
import com.zahidcodes.zvpn.ui.components.FlagBadge
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceCard
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceElevated
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextMuted
import com.zahidcodes.zvpn.ui.theme.TextMuted2
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import com.zahidcodes.zvpn.ui.theme.VibrantBlue
import com.zahidcodes.zvpn.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
  servers: List<Server>,
  selectedServer: Server,
  vpnStatus: VpnStatus = VpnStatus.DISCONNECTED,
  searchQuery: String,
  selectedCategory: ServerCategory,
  selectedSortOption: ServerSortOption = ServerSortOption.RECOMMENDED,
  pingingServerIds: Set<String> = emptySet(),
  onSearchChange: (String) -> Unit,
  onCategorySelect: (ServerCategory) -> Unit,
  onSortSelect: (ServerSortOption) -> Unit = {},
  onServerSelect: (Server, Boolean) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onPingServer: (String) -> Unit = {},
  onPingAll: () -> Unit = {},
  onDeleteServer: (String) -> Unit = {},
  onClearAllImported: () -> Unit = {},
  onDeleteAll: () -> Unit = {},
  onImportClick: (() -> Unit)? = null,
  onAddManualClick: (() -> Unit)? = null,
  onUploadFirestore: () -> Unit = {},
  schemaJson: String = "",
  onBackClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var serverDetailToInspect by remember { mutableStateOf<Server?>(null) }
  var showSortDialog by remember { mutableStateOf(false) }
  var showFirestoreFormatDialog by remember { mutableStateOf(false) }
  var serverToDelete by remember { mutableStateOf<Server?>(null) }
  var showDeleteAllDialog by remember { mutableStateOf(false) }
  var showPlusMenu by remember { mutableStateOf(false) }

  // Filter list
  val filteredServers = servers.filter { server ->
    val matchesSearch = server.country.contains(searchQuery, ignoreCase = true) ||
      server.city.contains(searchQuery, ignoreCase = true) ||
      server.region.contains(searchQuery, ignoreCase = true) ||
      server.ipAddress.contains(searchQuery, ignoreCase = true) ||
      server.protocolSupport.contains(searchQuery, ignoreCase = true)

    val matchesCategory = when (selectedCategory) {
      ServerCategory.ALL -> true
      ServerCategory.FASTEST -> server.category == ServerCategory.FASTEST || server.pingMs <= 35
      else -> server.category == selectedCategory
    }
    matchesSearch && matchesCategory
  }.let { list ->
    when (selectedSortOption) {
      ServerSortOption.RECOMMENDED -> list
      ServerSortOption.PING -> list.sortedBy { if (it.isOnline) it.pingMs else 9999 }
      ServerSortOption.LOAD -> list.sortedBy { it.loadPercent }
      ServerSortOption.NAME -> list.sortedBy { it.country }
    }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 10.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
    contentPadding = PaddingValues(top = 6.dp, bottom = 20.dp)
  ) {
    // Ultra Professional Header Bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          if (onBackClick != null) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF1B2E4A), Color(0xFF101E33))
                  )
                )
                .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .clickable(onClick = onBackClick)
                .testTag("server_screen_back_button"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
              text = "Global Server Nodes",
              fontSize = 19.sp,
              fontWeight = FontWeight.ExtraBold,
              color = TextPrimary
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(6.dp)
                  .clip(CircleShape)
                  .background(OkEmerald)
              )
              Text(
                text = "${servers.count { it.isOnline }} Active Nodes · Live Latency",
                fontSize = 11.sp,
                color = TextMuted,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        // Single Top-Right Plus Button with Comprehensive Dropdown Menu
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF132845), Color(0xFF0D1B2D))
              )
            )
            .border(1.dp, CyanAccent.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .clickable { showPlusMenu = true }
            .testTag("servers_screen_plus_menu_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = "Server Actions & Management Menu",
            tint = CyanAccent,
            modifier = Modifier.size(20.dp)
          )

          DropdownMenu(
            expanded = showPlusMenu,
            onDismissRequest = { showPlusMenu = false },
            modifier = Modifier
              .background(Color(0xFF0F2035))
              .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(8.dp))
              .testTag("servers_plus_dropdown_menu")
          ) {
            // Option 1: Import Config Link / URI
            DropdownMenuItem(
              text = {
                Column {
                  Text("Import Config Link / URI", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                  Text("VLESS, Trojan, VMess, WireGuard link", color = TextMuted, fontSize = 10.5.sp)
                }
              },
              leadingIcon = {
                Icon(Icons.Rounded.Link, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
              },
              onClick = {
                showPlusMenu = false
                onImportClick?.invoke()
              }
            )

            // Option 2: Add Server Manually
            DropdownMenuItem(
              text = {
                Column {
                  Text("Add Server Manually", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                  Text("Host, port, protocol & connect", color = TextMuted, fontSize = 10.5.sp)
                }
              },
              leadingIcon = {
                Icon(Icons.Rounded.Dns, contentDescription = null, tint = OkEmerald, modifier = Modifier.size(18.dp))
              },
              onClick = {
                showPlusMenu = false
                onAddManualClick?.invoke()
              }
            )

            // Option 3: Filter & Sort Servers
            DropdownMenuItem(
              text = {
                Column {
                  Text("Filter & Sort Servers", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                  Text("Sort by ping, load, country or name", color = TextMuted, fontSize = 10.5.sp)
                }
              },
              leadingIcon = {
                Icon(Icons.Rounded.Tune, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(18.dp))
              },
              onClick = {
                showPlusMenu = false
                showSortDialog = true
              }
            )

            // Option 4: Delete / Clear Configurations
            if (servers.isNotEmpty()) {
              DropdownMenuItem(
                text = {
                  Column {
                    Text("Delete / Clear Configurations", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("Remove imported or all server nodes", color = TextMuted, fontSize = 10.5.sp)
                  }
                },
                leadingIcon = {
                  Icon(Icons.Rounded.DeleteSweep, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                },
                onClick = {
                  showPlusMenu = false
                  showDeleteAllDialog = true
                }
              )
            }

            // Option 5: Firestore Cloud Sync & Formats
            DropdownMenuItem(
              text = {
                Column {
                  Text("Sync Firestore Database", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                  Text("View & upload cloud server schema", color = TextMuted, fontSize = 10.5.sp)
                }
              },
              leadingIcon = {
                Icon(Icons.Rounded.CloudSync, contentDescription = null, tint = OkEmerald, modifier = Modifier.size(18.dp))
              },
              onClick = {
                showPlusMenu = false
                showFirestoreFormatDialog = true
              }
            )
          }
        }
      }
    }

    // High-Tech Compact Search Bar Component
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = {
          Text(
            text = "Search location, city, IP or protocol...",
            color = TextMuted,
            fontSize = 11.5.sp
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Search",
            tint = CyanAccent,
            modifier = Modifier.size(15.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { onSearchChange("") },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear",
                tint = TextMuted,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF0D1B2D),
          unfocusedContainerColor = Color(0xFF0D1B2D),
          focusedBorderColor = CyanAccent,
          unfocusedBorderColor = DarkSurfaceStroke,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
          .testTag("server_search_input")
      )
    }

    // Category Filter Chips
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(ServerCategory.entries) { category ->
          val isSelected = category == selectedCategory
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(
                if (isSelected) {
                  Brush.horizontalGradient(listOf(VibrantBlue, Color(0xFF1E4273)))
                } else {
                  Brush.horizontalGradient(listOf(DarkSurfaceCard, DarkSurfaceCard))
                }
              )
              .border(
                1.dp,
                if (isSelected) CyanAccent else DarkSurfaceStroke,
                RoundedCornerShape(10.dp)
              )
              .clickable { onCategorySelect(category) }
              .padding(horizontal = 12.dp, vertical = 6.dp)
              .testTag("filter_chip_${category.name.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(CyanAccent)
                )
              }
              Text(
                text = category.label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else TextMuted
              )
            }
          }
        }
      }
    }



    // Section Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp, bottom = 1.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ALL LOCATIONS (${filteredServers.size})",
          fontSize = 10.5.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp,
          color = TextMuted2
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          if (servers.any { it.isImported }) {
            TextButton(
              onClick = { showDeleteAllDialog = true },
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
              modifier = Modifier.testTag("clear_imported_configs_button")
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteSweep,
                contentDescription = "Delete Configs",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Delete All Imported",
                color = Color(0xFFEF4444),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          } else if (servers.isNotEmpty()) {
            TextButton(
              onClick = { showDeleteAllDialog = true },
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 1.dp),
              modifier = Modifier.testTag("clear_all_configs_button")
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteSweep,
                contentDescription = "Clear All",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Clear All",
                color = Color(0xFFEF4444),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }

          Text(
            text = "· Live Delay",
            fontSize = 10.sp,
            color = TextMuted
          )
        }
      }
    }

    // Server List Items
    items(filteredServers, key = { it.id }) { server ->
      val isSelected = server.id == selectedServer.id
      val isPinging = pingingServerIds.contains(server.id)

      ProfessionalServerListItem(
        server = server,
        isSelected = isSelected,
        isPinging = isPinging,
        onSelect = { onServerSelect(server, true) },
        onConnect = { onServerSelect(server, true) },
        onPingClick = { onPingServer(server.id) },
        onInfoClick = { serverDetailToInspect = server },
        onToggleFavorite = { onToggleFavorite(server.id) },
        onDelete = { serverToDelete = server }
      )
    }
  }

  // Delete Single Server Confirmation Dialog
  serverToDelete?.let { server ->
    AlertDialog(
      onDismissRequest = { serverToDelete = null },
      containerColor = DarkSurfaceElevated,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(22.dp)
          )
          Text("Delete Configuration", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Are you sure you want to delete this configuration?",
            color = TextMuted,
            fontSize = 12.sp
          )
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF0C1929))
              .padding(8.dp)
          ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Text(
                text = "${server.city}, ${server.country}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = "${server.protocolSupport} · ${server.ipAddress}:${server.port}",
                color = CyanAccent,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteServer(server.id)
            serverToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.testTag("confirm_delete_single_btn")
        ) {
          Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
      },
      dismissButton = {
        TextButton(onClick = { serverToDelete = null }) {
          Text("Cancel", color = TextMuted, fontSize = 12.sp)
        }
      }
    )
  }

  // Delete All / Bulk Configurations Dialog
  if (showDeleteAllDialog) {
    val importedCount = servers.count { it.isImported }
    AlertDialog(
      onDismissRequest = { showDeleteAllDialog = false },
      containerColor = DarkSurfaceElevated,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.DeleteSweep,
            contentDescription = null,
            tint = Color(0xFFEF4444),
            modifier = Modifier.size(24.dp)
          )
          Text("Delete Configurations", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Select deletion scope:",
            color = TextMuted,
            fontSize = 12.sp
          )

          if (importedCount > 0) {
            Button(
              onClick = {
                onClearAllImported()
                showDeleteAllDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A151C)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .testTag("delete_all_imported_option_btn")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
              ) {
                Icon(
                  imageVector = Icons.Rounded.DeleteSweep,
                  contentDescription = null,
                  tint = Color(0xFFEF4444),
                  modifier = Modifier.size(18.dp)
                )
                Column {
                  Text(
                    text = "Delete All Imported ($importedCount)",
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                  )
                  Text(
                    text = "Keeps default standard servers",
                    color = TextMuted,
                    fontSize = 10.sp
                  )
                }
              }
            }
          }

          Button(
            onClick = {
              onDeleteAll()
              showDeleteAllDialog = false
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF381418)),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
              .testTag("delete_everything_option_btn")
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteForever,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(18.dp)
              )
              Column {
                Text(
                  text = "Delete All Servers (${servers.size})",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.5.sp
                )
                Text(
                  text = "Clears all configuration nodes from the list",
                  color = Color(0xFFEF4444).copy(alpha = 0.8f),
                  fontSize = 10.sp
                )
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showDeleteAllDialog = false }) {
          Text("Cancel", color = TextMuted, fontSize = 12.sp)
        }
      }
    )
  }

  // Sort Dialog
  if (showSortDialog) {
    AlertDialog(
      onDismissRequest = { showSortDialog = false },
      containerColor = DarkSurfaceElevated,
      title = {
        Text("Sort Servers", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
          ServerSortOption.entries.forEach { option ->
            val isOptSelected = option == selectedSortOption
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  onSortSelect(option)
                  showSortDialog = false
                }
                .padding(vertical = 8.dp, horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = option.label,
                fontSize = 13.sp,
                fontWeight = if (isOptSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isOptSelected) CyanAccent else TextPrimary
              )
              if (isOptSelected) {
                Icon(
                  imageVector = Icons.Rounded.Check,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showSortDialog = false }) {
          Text("Done", color = CyanAccent, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Firestore Database Formats Dialog
  if (showFirestoreFormatDialog) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showFirestoreFormatDialog = false },
      containerColor = DarkSurfaceElevated,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.CloudSync,
            contentDescription = null,
            tint = OkEmerald,
            modifier = Modifier.size(22.dp)
          )
          Column {
            Text("Firestore Database Formats", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("Collection: servers · Sample Configurations", color = TextMuted, fontSize = 11.sp)
          }
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "These sample server configs (VLESS, Trojan, VMess, WireGuard, Shadowsocks, Hysteria2) create the standard Firestore database schema for ZVPN:",
            color = TextMuted,
            fontSize = 11.5.sp,
            lineHeight = 16.sp
          )

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(240.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF060D17))
              .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            LazyColumn {
              item {
                Text(
                  text = schemaJson,
                  color = OkEmerald,
                  fontSize = 10.sp,
                  lineHeight = 14.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                onUploadFirestore()
                showFirestoreFormatDialog = false
              },
              colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(1.1f)
            ) {
              Icon(Icons.Rounded.CloudSync, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Upload to Firestore", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            Button(
              onClick = {
                clipboardManager.setText(AnnotatedString(schemaJson))
                copied = true
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (copied) OkEmerald else Color(0xFF1E293B)
              ),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.weight(0.9f)
            ) {
              Icon(
                if (copied) Icons.Rounded.Check else Icons.Rounded.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(15.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(if (copied) "Copied!" else "Copy JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showFirestoreFormatDialog = false }) {
          Text("Close", color = CyanAccent, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }
      }
    )
  }

  // Server Details Modal
  serverDetailToInspect?.let { server ->
    val isInspectedPinging = pingingServerIds.contains(server.id)
    AlertDialog(
      onDismissRequest = { serverDetailToInspect = null },
      containerColor = DarkSurfaceElevated,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FlagBadge(countryCode = server.countryCode, badgeSize = 28.dp)
          Text("${server.city}, ${server.country}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ServerSpecRow(label = "Host / IP Address", value = server.ipAddress)
          ServerSpecRow(label = "Verified Latency", value = if (server.isOnline) "${server.pingMs} ms" else "Offline")
          ServerSpecRow(
            label = "Quality Index",
            value = when {
              !server.isOnline -> "Unreachable"
              server.pingMs < 40 -> "Ultra Low Latency (Gaming/VoIP)"
              server.pingMs < 90 -> "Optimal (4K Streaming)"
              else -> "Standard Speed"
            }
          )
          ServerSpecRow(label = "Server Load", value = "${server.loadPercent}% Capacity")
          ServerSpecRow(label = "Protocol Core", value = server.protocolSupport)
          ServerSpecRow(label = "Region", value = server.region)

          // Diagnostic Ping Button inside Details
          Button(
            onClick = { onPingServer(server.id) },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF132A4B)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 6.dp)
              .border(1.dp, CyanAccent.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              if (isInspectedPinging) {
                CircularProgressIndicator(
                  modifier = Modifier.size(15.dp),
                  color = CyanAccent,
                  strokeWidth = 2.dp
                )
                Text("Testing Packet Route…", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              } else {
                Icon(
                  imageVector = Icons.Rounded.Speed,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(16.dp)
                )
                Text("Run Real-Time Ping Test", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onServerSelect(server, true)
            serverDetailToInspect = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Connect to this Server", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
        }
      },
      dismissButton = {
        TextButton(onClick = { serverDetailToInspect = null }) {
          Text("Close", color = TextMuted, fontSize = 12.sp)
        }
      }
    )
  }
}

@Composable
private fun ServerSpecRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(label, color = TextMuted, fontSize = 12.sp)
    Text(value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun ProfessionalServerListItem(
  server: Server,
  isSelected: Boolean,
  isPinging: Boolean,
  onSelect: () -> Unit,
  onConnect: () -> Unit,
  onPingClick: () -> Unit,
  onInfoClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onDelete: () -> Unit
) {
  val shape = RoundedCornerShape(10.dp)
  val infiniteTransition = rememberInfiniteTransition(label = "pingRotation")
  val rotationAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "rotation"
  )

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(
        if (isSelected) {
          Brush.linearGradient(colors = listOf(Color(0xFF0C241B), Color(0xFF081812)))
        } else {
          Brush.linearGradient(colors = listOf(DarkSurfaceCard, DarkSurfaceCard))
        }
      )
      .border(
        1.dp,
        if (isSelected) OkEmerald.copy(alpha = 0.7f) else DarkSurfaceStroke,
        shape
      )
      .clickable(onClick = onSelect)
      .padding(horizontal = 8.dp, vertical = 5.dp)
      .testTag("server_item_${server.id}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // Fixed Width Left Active Status Bar (Zero Layout Shift)
    Box(
      modifier = Modifier
        .size(width = 4.dp, height = 24.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(if (isSelected) OkEmerald else Color.Transparent)
    )

    // Flag Badge
    FlagBadge(
      countryCode = server.countryCode,
      badgeSize = 25.dp
    )

    // Name & Ping info
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Text(
          text = "${server.city}, ${server.country}",
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
          color = if (isSelected) Color.White else TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f, fill = false)
        )

        if (server.isFavorite) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Favorite",
            tint = WarningAmber,
            modifier = Modifier.size(11.dp)
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        if (isPinging) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Icon(
              imageVector = Icons.Rounded.NetworkCheck,
              contentDescription = null,
              tint = CyanAccent,
              modifier = Modifier
                .size(11.dp)
                .rotate(rotationAngle)
            )
            Text(
              text = "Pinging…",
              fontSize = 9.5.sp,
              color = CyanAccent,
              fontWeight = FontWeight.Bold
            )
          }
        } else if (server.isExpired || !server.isOnline) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(Color(0xFFEF4444).copy(alpha = 0.18f))
              .padding(horizontal = 4.dp, vertical = 0.5.dp)
          ) {
            Text(
              text = if (server.isExpired) "EXPIRED" else "OFFLINE",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFFEF4444)
            )
          }
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
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
              fontSize = 10.sp,
              color = TextMuted,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        Text(text = "·", color = TextMuted2, fontSize = 9.5.sp)

        Text(
          text = server.protocolSupport,
          fontSize = 10.sp,
          color = TextMuted2,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }

    // Action buttons & Active Indicator
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
      // Ping Test Button
      IconButton(
        onClick = onPingClick,
        modifier = Modifier
          .size(24.dp)
          .testTag("ping_server_${server.id}")
      ) {
        if (isPinging) {
          CircularProgressIndicator(
            modifier = Modifier.size(11.dp),
            color = CyanAccent,
            strokeWidth = 1.5.dp
          )
        } else {
          Icon(
            imageVector = Icons.Rounded.NetworkCheck,
            contentDescription = "Test Ping",
            tint = CyanAccent,
            modifier = Modifier.size(14.dp)
          )
        }
      }

      // Delete Button for One-by-One Deletion
      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(26.dp)
          .testTag("delete_server_${server.id}")
      ) {
        Icon(
          imageVector = Icons.Rounded.Delete,
          contentDescription = "Delete Configuration",
          tint = if (server.isImported) Color(0xFFEF4444) else Color(0xFFEF4444).copy(alpha = 0.65f),
          modifier = Modifier.size(15.dp)
        )
      }

      IconButton(
        onClick = onInfoClick,
        modifier = Modifier.size(24.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = "Details",
          tint = TextMuted2,
          modifier = Modifier.size(13.dp)
        )
      }

      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier
          .size(24.dp)
          .testTag("server_favorite_${server.id}")
      ) {
        Icon(
          imageVector = if (server.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
          contentDescription = "Favorite",
          tint = if (server.isFavorite) WarningAmber else TextMuted2,
          modifier = Modifier.size(14.dp)
        )
      }

      // Singular Clear Active Status Indicator vs Select Action
      if (isSelected) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(OkEmerald.copy(alpha = 0.2f))
            .border(1.dp, OkEmerald.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("server_connect_btn_${server.id}"),
          contentAlignment = Alignment.Center
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Box(
              modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(OkEmerald)
            )
            Text(
              text = "ACTIVE",
              fontSize = 9.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = OkEmerald
            )
          }
        }
      } else {
        Button(
          onClick = onConnect,
          shape = RoundedCornerShape(6.dp),
          colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue),
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 1.dp),
          modifier = Modifier
            .height(24.dp)
            .testTag("server_connect_btn_${server.id}")
        ) {
          Text(
            text = "Select",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}
