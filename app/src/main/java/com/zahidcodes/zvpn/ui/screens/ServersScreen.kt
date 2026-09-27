package com.zahidcodes.zvpn.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Star
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
  onUpdateServer: (Server) -> Unit = {},
  onRestoreDefaults: () -> Unit = {},
  onBackClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var serverDetailToInspect by remember { mutableStateOf<Server?>(null) }
  var serverToDelete by remember { mutableStateOf<Server?>(null) }
  var serverToEdit by remember { mutableStateOf<Server?>(null) }
  var showDeleteAllDialogLocal by remember { mutableStateOf(false) }

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

        // Header Action: Delete All Configurations Button
        if (servers.isNotEmpty()) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFF2A151C))
              .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.55f), RoundedCornerShape(8.dp))
              .clickable { showDeleteAllDialogLocal = true }
              .padding(horizontal = 9.dp, vertical = 5.dp)
              .testTag("servers_screen_delete_all_btn"),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteSweep,
                contentDescription = "Delete All Configurations",
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = "Delete All",
                color = Color(0xFFEF4444),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // High-Tech Compact Search Bar Component (Adjusted text size & padding)
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        textStyle = androidx.compose.ui.text.TextStyle(
          fontSize = 12.sp,
          fontWeight = FontWeight.Medium,
          color = TextPrimary
        ),
        placeholder = {
          Text(
            text = "Search location, city, IP or protocol...",
            color = TextMuted,
            fontSize = 12.sp
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = "Search",
            tint = CyanAccent,
            modifier = Modifier.size(16.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { onSearchChange("") },
              modifier = Modifier.size(26.dp)
            ) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear",
                tint = TextMuted,
                modifier = Modifier.size(13.dp)
              )
            }
          }
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = Color(0xFF091424),
          unfocusedContainerColor = Color(0xFF091424),
          focusedBorderColor = CyanAccent,
          unfocusedBorderColor = DarkSurfaceStroke,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary,
          cursorColor = CyanAccent
        ),
        shape = RoundedCornerShape(10.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .height(42.dp)
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
          .padding(top = 2.dp, bottom = 2.dp),
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
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          if (servers.isNotEmpty()) {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .clickable { showDeleteAllDialogLocal = true }
                .padding(horizontal = 4.dp, vertical = 1.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
              Icon(
                imageVector = Icons.Rounded.DeleteSweep,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(12.dp)
              )
              Text(
                text = "Delete",
                fontSize = 9.5.sp,
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold
              )
            }
            Text(text = "·", fontSize = 9.sp, color = TextMuted)
          }

          Text(
            text = "Tap Edit / Hold 2-3s",
            fontSize = 9.5.sp,
            color = CyanAccent.copy(alpha = 0.9f),
            fontWeight = FontWeight.SemiBold
          )
          Text(
            text = "· Live Delay",
            fontSize = 9.5.sp,
            color = TextMuted
          )
        }
      }
    }

    // Server List Items or Compact Empty State
    if (filteredServers.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 36.dp, horizontal = 16.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F2035))
                .border(1.dp, CyanAccent.copy(alpha = 0.4f), CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Rounded.Dns,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(26.dp)
              )
            }

            Text(
              text = if (servers.isEmpty()) "No Server Configurations" else "No Matching Locations",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )

            Text(
              text = if (servers.isEmpty())
                "All server configurations have been deleted. Import a new link or restore defaults below."
              else
                "No servers found matching \"$searchQuery\". Try a different search keyword.",
              fontSize = 11.5.sp,
              color = TextMuted,
              textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              modifier = Modifier.padding(horizontal = 20.dp)
            )

            if (servers.isEmpty()) {
              Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 6.dp)
              ) {
                Button(
                  onClick = { onImportClick?.invoke() },
                  colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Import Config", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                  onClick = onRestoreDefaults,
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Rounded.CloudSync, contentDescription = null, modifier = Modifier.size(15.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Restore Defaults", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    } else {
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
          onEditClick = { serverToEdit = server },
          onLongClick = { serverToEdit = server },
          onToggleFavorite = { onToggleFavorite(server.id) },
          onDelete = { serverToDelete = server }
        )
      }
    }
  }

  // Delete All Dialog (Triggered from ServersScreen header or section button)
  if (showDeleteAllDialogLocal) {
    DeleteConfigurationsDialog(
      servers = servers,
      onClearAllImported = onClearAllImported,
      onDeleteAll = onDeleteAll,
      onDismiss = { showDeleteAllDialogLocal = false }
    )
  }

  // Edit Server Dialog (Triggered on long-click 2-3s or edit button)
  serverToEdit?.let { server ->
    EditServerDialog(
      server = server,
      onDismiss = { serverToEdit = null },
      onSave = { updated ->
        onUpdateServer(updated)
        serverToEdit = null
      },
      onDelete = { id ->
        onDeleteServer(id)
        serverToEdit = null
      }
    )
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProfessionalServerListItem(
  server: Server,
  isSelected: Boolean,
  isPinging: Boolean,
  onSelect: () -> Unit,
  onConnect: () -> Unit,
  onPingClick: () -> Unit,
  onInfoClick: () -> Unit,
  onEditClick: () -> Unit,
  onLongClick: () -> Unit,
  onToggleFavorite: () -> Unit,
  onDelete: () -> Unit
) {
  val shape = RoundedCornerShape(10.dp)
  var showMoreMenu by remember { mutableStateOf(false) }
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
      .combinedClickable(
        onClick = onSelect,
        onLongClick = onLongClick
      )
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag("server_item_${server.id}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    // Fixed Width Left Active Status Bar (Zero Layout Shift)
    Box(
      modifier = Modifier
        .size(width = 3.5.dp, height = 24.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(if (isSelected) OkEmerald else Color.Transparent)
    )

    // Flag Badge
    FlagBadge(
      countryCode = server.countryCode,
      badgeSize = 24.dp
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

        if (server.isImported || server.id.startsWith("import-") || server.id == "vless-id-pusat-91") {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(Color(0xFF7C3AED).copy(alpha = 0.22f))
              .padding(horizontal = 4.dp, vertical = 0.5.dp)
          ) {
            Text(
              text = "IMPORTED",
              fontSize = 7.5.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color(0xFFA78BFA)
            )
          }
        }

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
      horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
      // Ping Test Button
      IconButton(
        onClick = onPingClick,
        modifier = Modifier
          .size(28.dp)
          .testTag("ping_server_${server.id}")
      ) {
        if (isPinging) {
          CircularProgressIndicator(
            modifier = Modifier.size(12.dp),
            color = CyanAccent,
            strokeWidth = 1.5.dp
          )
        } else {
          Icon(
            imageVector = Icons.Rounded.NetworkCheck,
            contentDescription = "Test Ping",
            tint = CyanAccent,
            modifier = Modifier.size(15.dp)
          )
        }
      }

      // Direct Visible Edit Pill Button
      Box(
        modifier = Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(CyanAccent.copy(alpha = 0.12f))
          .border(1.dp, CyanAccent.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
          .clickable(onClick = onEditClick)
          .padding(horizontal = 7.dp, vertical = 3.dp)
          .testTag("edit_server_${server.id}"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = "Edit Configuration",
            tint = CyanAccent,
            modifier = Modifier.size(11.5.dp)
          )
          Text(
            text = "Edit",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = CyanAccent
          )
        }
      }

      // More Options Dropdown (Edit, Info, Favorite, Delete)
      Box {
        IconButton(
          onClick = { showMoreMenu = true },
          modifier = Modifier
            .size(24.dp)
            .testTag("more_options_server_${server.id}")
        ) {
          Icon(
            imageVector = Icons.Rounded.MoreVert,
            contentDescription = "More Options",
            tint = TextMuted,
            modifier = Modifier.size(16.dp)
          )
        }

        DropdownMenu(
          expanded = showMoreMenu,
          onDismissRequest = { showMoreMenu = false },
          modifier = Modifier
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(8.dp))
        ) {
          DropdownMenuItem(
            text = { Text("Edit Configuration", fontSize = 12.5.sp, color = TextPrimary, fontWeight = FontWeight.Medium) },
            leadingIcon = {
              Icon(Icons.Rounded.Edit, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
            },
            onClick = {
              showMoreMenu = false
              onEditClick()
            }
          )
          DropdownMenuItem(
            text = { Text("Server Details", fontSize = 12.5.sp, color = TextPrimary, fontWeight = FontWeight.Medium) },
            leadingIcon = {
              Icon(Icons.Outlined.Info, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
            },
            onClick = {
              showMoreMenu = false
              onInfoClick()
            }
          )
          DropdownMenuItem(
            text = { Text(if (server.isFavorite) "Remove from Favorites" else "Add to Favorites", fontSize = 12.5.sp, color = TextPrimary, fontWeight = FontWeight.Medium) },
            leadingIcon = {
              Icon(Icons.Rounded.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
            },
            onClick = {
              showMoreMenu = false
              onToggleFavorite()
            }
          )
          DropdownMenuItem(
            text = { Text("Delete Configuration", fontSize = 12.5.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold) },
            leadingIcon = {
              Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
            },
            onClick = {
              showMoreMenu = false
              onDelete()
            }
          )
        }
      }

      // Singular Clear Active Status Indicator vs Select Action
      if (isSelected) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(OkEmerald.copy(alpha = 0.2f))
            .border(1.dp, OkEmerald.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
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
          contentPadding = PaddingValues(horizontal = 9.dp, vertical = 2.dp),
          modifier = Modifier
            .height(26.dp)
            .testTag("server_connect_btn_${server.id}")
        ) {
          Text(
            text = "Select",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
        }
      }
    }
  }
}

@Composable
fun EditServerDialog(
  server: Server,
  onDismiss: () -> Unit,
  onSave: (Server) -> Unit,
  onDelete: (String) -> Unit
) {
  var name by remember { mutableStateOf(server.city) }
  var ipAddress by remember { mutableStateOf(server.ipAddress) }
  var portStr by remember { mutableStateOf(server.port.toString()) }
  var protocol by remember { mutableStateOf(server.protocolSupport) }
  var uuid by remember { mutableStateOf(server.uuid) }
  var configUri by remember { mutableStateOf(server.configUri) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceElevated,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Rounded.Edit,
          contentDescription = null,
          tint = CyanAccent,
          modifier = Modifier.size(20.dp)
        )
        Column {
          Text(
            text = "Edit Configuration",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Text(
            text = "${server.city} · ${server.country}",
            color = TextMuted,
            fontSize = 11.sp
          )
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Config / Location Name", fontSize = 11.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkSurfaceStroke,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(8.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().height(54.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text("Host / IP Address", fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanAccent,
              unfocusedBorderColor = DarkSurfaceStroke,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            modifier = Modifier.weight(1.3f).height(54.dp)
          )

          OutlinedTextField(
            value = portStr,
            onValueChange = { portStr = it },
            label = { Text("Port", fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanAccent,
              unfocusedBorderColor = DarkSurfaceStroke,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            modifier = Modifier.weight(0.7f).height(54.dp)
          )
        }

        OutlinedTextField(
          value = protocol,
          onValueChange = { protocol = it },
          label = { Text("Protocol (VLESS, Trojan, VMess, etc.)", fontSize = 11.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkSurfaceStroke,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(8.dp),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().height(54.dp)
        )

        if (uuid.isNotBlank() || server.configUri.isNotBlank()) {
          OutlinedTextField(
            value = uuid,
            onValueChange = { uuid = it },
            label = { Text("UUID / Password / Key", fontSize = 11.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CyanAccent,
              unfocusedBorderColor = DarkSurfaceStroke,
              focusedTextColor = TextPrimary,
              unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().height(54.dp)
          )
        }

        OutlinedTextField(
          value = configUri,
          onValueChange = { configUri = it },
          label = { Text("Config URI (Optional)", fontSize = 11.sp) },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkSurfaceStroke,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
          ),
          shape = RoundedCornerShape(8.dp),
          maxLines = 2,
          modifier = Modifier.fillMaxWidth().height(60.dp)
        )
      }
    },
    confirmButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = {
            onDelete(server.id)
            onDismiss()
          },
          modifier = Modifier.size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.Delete,
            contentDescription = "Delete",
            tint = Color(0xFFEF4444)
          )
        }

        Button(
          onClick = {
            val parsedPort = portStr.toIntOrNull() ?: server.port
            val updated = server.copy(
              city = name.trim().ifBlank { server.city },
              ipAddress = ipAddress.trim().ifBlank { server.ipAddress },
              port = parsedPort,
              protocolSupport = protocol.trim().ifBlank { server.protocolSupport },
              uuid = uuid.trim().ifBlank { server.uuid },
              configUri = configUri.trim().ifBlank { server.configUri }
            )
            onSave(updated)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue),
          shape = RoundedCornerShape(8.dp)
        ) {
          Icon(imageVector = Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(15.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Save Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMuted, fontSize = 12.sp)
      }
    }
  )
}

@Composable
fun ServerSortDialog(
  selectedSortOption: ServerSortOption,
  onSortSelect: (ServerSortOption) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
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
                onDismiss()
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
      TextButton(onClick = onDismiss) {
        Text("Done", color = CyanAccent, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
      }
    }
  )
}

@Composable
fun DeleteConfigurationsDialog(
  servers: List<Server>,
  onClearAllImported: () -> Unit,
  onDeleteAll: () -> Unit,
  onDismiss: () -> Unit
) {
  val importedCount = servers.count {
    it.isImported || it.id.startsWith("import-") || it.id.startsWith("manual-") || it.id == "vless-id-pusat-91" || it.id.contains("import")
  }
  AlertDialog(
    onDismissRequest = onDismiss,
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
          text = "Choose an option to delete server configurations:",
          color = TextMuted,
          fontSize = 12.sp
        )

        // Option A: Delete All Imported Configurations
        Button(
          onClick = {
            onClearAllImported()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2A151C)
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            .testTag("delete_all_imported_option_btn")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Rounded.DeleteSweep,
              contentDescription = null,
              tint = Color(0xFFEF4444),
              modifier = Modifier.size(20.dp)
            )
            Column {
              Text(
                text = "Delete All Imported ($importedCount)",
                color = Color(0xFFEF4444),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = if (importedCount > 0) "Removes all $importedCount imported configs, keeps default nodes" else "No imported configs present (tap to clear)",
                color = TextMuted,
                fontSize = 10.5.sp
              )
            }
          }
        }

        // Option B: Delete All Servers (Completely clear everything)
        Button(
          onClick = {
            onDeleteAll()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF381418)
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(8.dp))
            .testTag("delete_everything_option_btn")
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
          ) {
            Icon(
              imageVector = Icons.Rounded.DeleteForever,
              contentDescription = null,
              tint = Color(0xFFEF4444),
              modifier = Modifier.size(20.dp)
            )
            Column {
              Text(
                text = "Delete All Servers (${servers.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = "Completely clears all configuration nodes from the app",
                color = Color(0xFFEF4444).copy(alpha = 0.85f),
                fontSize = 10.5.sp
              )
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMuted, fontSize = 12.sp)
      }
    }
  )
}

@Composable
fun FirestoreSchemaDialog(
  schemaJson: String,
  onUploadFirestore: () -> Unit,
  onDismiss: () -> Unit
) {
  val clipboardManager = LocalClipboardManager.current
  var copied by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
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
              onDismiss()
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
      TextButton(onClick = onDismiss) {
        Text("Close", color = CyanAccent, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
      }
    }
  )
}

