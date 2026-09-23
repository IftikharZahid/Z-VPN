package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Server
import com.example.model.ServerCategory
import com.example.model.ServerSortOption
import com.example.model.VpnStatus
import com.example.ui.components.FlagBadge
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextMuted2
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VibrantBlue
import com.example.ui.theme.WarningAmber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersScreen(
  servers: List<Server>,
  selectedServer: Server,
  vpnStatus: VpnStatus = VpnStatus.DISCONNECTED,
  searchQuery: String,
  selectedCategory: ServerCategory,
  selectedSortOption: ServerSortOption = ServerSortOption.RECOMMENDED,
  onSearchChange: (String) -> Unit,
  onCategorySelect: (ServerCategory) -> Unit,
  onSortSelect: (ServerSortOption) -> Unit = {},
  onServerSelect: (Server, Boolean) -> Unit,
  onToggleFavorite: (String) -> Unit,
  onBackClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  var serverDetailToInspect by remember { mutableStateOf<Server?>(null) }
  var showSortDialog by remember { mutableStateOf(false) }

  // Filter list
  val filteredServers = servers.filter { server ->
    val matchesSearch = server.country.contains(searchQuery, ignoreCase = true) ||
      server.city.contains(searchQuery, ignoreCase = true) ||
      server.region.contains(searchQuery, ignoreCase = true) ||
      server.ipAddress.contains(searchQuery, ignoreCase = true)

    val matchesCategory = when (selectedCategory) {
      ServerCategory.ALL -> true
      ServerCategory.FASTEST -> server.category == ServerCategory.FASTEST || server.pingMs <= 35
      else -> server.category == selectedCategory
    }
    matchesSearch && matchesCategory
  }.let { list ->
    when (selectedSortOption) {
      ServerSortOption.RECOMMENDED -> list
      ServerSortOption.PING -> list.sortedBy { it.pingMs }
      ServerSortOption.LOAD -> list.sortedBy { it.loadPercent }
      ServerSortOption.NAME -> list.sortedBy { it.country }
    }
  }

  val fastestServer = servers.minByOrNull { it.pingMs }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
  ) {
    // Header Bar
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
            IconButton(
              onClick = onBackClick,
              modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(12.dp))
                .testTag("server_screen_back_button")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = TextPrimary,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = "VPN Servers",
              fontSize = 24.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "${servers.size} high-speed servers available",
              fontSize = 12.5.sp,
              color = TextMuted
            )
          }
        }

        // Sort button
        IconButton(
          onClick = { showSortDialog = true },
          modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(12.dp))
            .testTag("sort_servers_button")
        ) {
          Icon(
            imageVector = Icons.Rounded.Tune,
            contentDescription = "Sort Options",
            tint = CyanAccent,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }

    // Search Bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = {
          Text(text = "Search by city, country, or IP...", color = TextMuted, fontSize = 13.5.sp)
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { onSearchChange("") }) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear",
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        },
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = DarkSurfaceCard,
          unfocusedContainerColor = DarkSurfaceCard,
          focusedBorderColor = VibrantBlue,
          unfocusedBorderColor = DarkSurfaceStroke,
          focusedTextColor = TextPrimary,
          unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
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
              .clip(RoundedCornerShape(12.dp))
              .background(
                if (isSelected) VibrantBlue.copy(alpha = 0.22f) else DarkSurfaceCard
              )
              .border(
                1.dp,
                if (isSelected) VibrantBlue else DarkSurfaceStroke,
                RoundedCornerShape(12.dp)
              )
              .clickable { onCategorySelect(category) }
              .padding(horizontal = 14.dp, vertical = 8.dp)
              .testTag("filter_chip_${category.name.lowercase()}"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = category.label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
              color = if (isSelected) Color(0xFF93C5FD) else TextMuted
            )
          }
        }
      }
    }

    // Currently Selected Server Spotlight (if searching or just on the list)
    item {
      val isConnected = vpnStatus == VpnStatus.CONNECTED
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(18.dp))
          .background(DarkSurfaceCard)
          .border(1.dp, if (isConnected) OkEmerald.copy(alpha = 0.4f) else VibrantBlue.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
          .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isConnected) OkEmerald else VibrantBlue)
            )
            Text(
              text = if (isConnected) "ACTIVE CONNECTION" else "CURRENTLY SELECTED",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp,
              color = if (isConnected) OkEmerald else CyanAccent
            )
          }

          Text(
            text = "${selectedServer.pingMs} ms",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = OkEmerald
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          FlagBadge(countryCode = selectedServer.countryCode, badgeSize = 36.dp)
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${selectedServer.city}, ${selectedServer.country}",
              fontSize = 14.5.sp,
              fontWeight = FontWeight.Bold,
              color = TextPrimary
            )
            Text(
              text = "${selectedServer.ipAddress} · ${selectedServer.region}",
              fontSize = 11.5.sp,
              color = TextMuted
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(if (isConnected) OkEmerald.copy(alpha = 0.18f) else Color(0x18FFFFFF))
              .padding(horizontal = 8.dp, vertical = 4.dp)
          ) {
            Text(
              text = if (isConnected) "CONNECTED" else "READY",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = if (isConnected) OkEmerald else TextPrimary
            )
          }
        }
      }
    }

    // Recommended Fastest Server Banner (if available and no strict search query)
    if (searchQuery.isEmpty() && selectedCategory == ServerCategory.ALL && fastestServer != null) {
      item {
        val isFastestSelected = selectedServer.id == fastestServer.id
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
              Brush.linearGradient(
                colors = listOf(Color(0xFF132742), Color(0xFF0F1B2F))
              )
            )
            .border(1.dp, VibrantBlue.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .clickable { onServerSelect(fastestServer, false) }
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
                .background(VibrantBlue.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Rounded.Bolt,
                contentDescription = null,
                tint = CyanAccent,
                modifier = Modifier.size(22.dp)
              )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = "OPTIMAL SERVER",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.8.sp,
                  color = CyanAccent
                )
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(OkEmerald.copy(alpha = 0.2f))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                  Text(
                    text = "${fastestServer.pingMs} ms",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = OkEmerald
                  )
                }
              }
              Text(
                text = "${fastestServer.city}, ${fastestServer.country}",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
              )
            }
          }

          Button(
            onClick = { onServerSelect(fastestServer, true) },
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (isFastestSelected) OkEmerald else VibrantBlue
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.testTag("quick_connect_fastest_button")
          ) {
            Text(
              text = if (isFastestSelected) "Selected" else "Connect",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
      }
    }

    // Section Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ALL LOCATIONS (${filteredServers.size})",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp,
          color = TextMuted2
        )

        Text(
          text = "Sort: ${selectedSortOption.label}",
          fontSize = 11.sp,
          color = TextMuted,
          modifier = Modifier.clickable { showSortDialog = true }
        )
      }
    }

    // Server List Items
    items(filteredServers, key = { it.id }) { server ->
      val isSelected = server.id == selectedServer.id
      ServerListItem(
        server = server,
        isSelected = isSelected,
        onSelect = { onServerSelect(server, false) },
        onConnect = { onServerSelect(server, true) },
        onInfoClick = { serverDetailToInspect = server },
        onToggleFavorite = { onToggleFavorite(server.id) }
      )
    }
  }

  // Sort Dialog
  if (showSortDialog) {
    AlertDialog(
      onDismissRequest = { showSortDialog = false },
      containerColor = DarkSurfaceElevated,
      title = {
        Text("Sort Servers", color = TextPrimary, fontWeight = FontWeight.Bold)
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          ServerSortOption.entries.forEach { option ->
            val isOptSelected = option == selectedSortOption
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                  onSortSelect(option)
                  showSortDialog = false
                }
                .padding(vertical = 10.dp, horizontal = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = option.label,
                fontSize = 14.sp,
                fontWeight = if (isOptSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isOptSelected) CyanAccent else TextPrimary
              )
              if (isOptSelected) {
                Icon(
                  imageVector = Icons.Rounded.Check,
                  contentDescription = null,
                  tint = CyanAccent,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showSortDialog = false }) {
          Text("Done", color = CyanAccent)
        }
      }
    )
  }

  // Server Details Modal
  serverDetailToInspect?.let { server ->
    AlertDialog(
      onDismissRequest = { serverDetailToInspect = null },
      containerColor = DarkSurfaceElevated,
      title = {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          FlagBadge(countryCode = server.countryCode, badgeSize = 34.dp)
          Column {
            Text("${server.city}, ${server.country}", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text(server.region, color = TextMuted, fontSize = 12.sp)
          }
        }
      },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          ServerSpecRow("IP Address", server.ipAddress)
          ServerSpecRow("Ping Latency", "${server.pingMs} ms")
          ServerSpecRow("Server Load", "${server.loadPercent}% Capacity")
          ServerSpecRow("Protocols", server.protocolSupport)
          ServerSpecRow("Encryption", "ChaCha20-Poly1305 / AES-256")
          ServerSpecRow("Hardware", "10Gbps RAM-Only (Zero-Log)")
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
          Text("Connect to this Server", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { serverDetailToInspect = null }) {
          Text("Close", color = TextMuted)
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
    Text(label, color = TextMuted, fontSize = 12.5.sp)
    Text(value, color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
  }
}

@Composable
private fun ServerListItem(
  server: Server,
  isSelected: Boolean,
  onSelect: () -> Unit,
  onConnect: () -> Unit,
  onInfoClick: () -> Unit,
  onToggleFavorite: () -> Unit
) {
  val shape = RoundedCornerShape(16.dp)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(shape)
      .background(if (isSelected) Color(0xFF13223A) else DarkSurfaceCard)
      .border(
        1.dp,
        if (isSelected) VibrantBlue.copy(alpha = 0.6f) else DarkSurfaceStroke,
        shape
      )
      .clickable(onClick = onSelect)
      .padding(horizontal = 12.dp, vertical = 12.dp)
      .testTag("server_item_${server.id}"),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    // Flag Badge
    FlagBadge(
      countryCode = server.countryCode,
      badgeSize = 38.dp
    )

    // Name & Ping info
    Column(
      modifier = Modifier.weight(1f),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Text(
          text = "${server.city}, ${server.country}",
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = TextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        if (isSelected) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(VibrantBlue.copy(alpha = 0.25f))
              .padding(horizontal = 5.dp, vertical = 1.dp)
          ) {
            Text(
              text = "ACTIVE",
              fontSize = 8.5.sp,
              fontWeight = FontWeight.Bold,
              color = CyanAccent
            )
          }
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Ping
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          val pingColor = when {
            server.pingMs < 40 -> OkEmerald
            server.pingMs < 90 -> CyanAccent
            else -> WarningAmber
          }
          Box(
            modifier = Modifier
              .size(6.dp)
              .clip(CircleShape)
              .background(pingColor)
          )
          Text(
            text = "${server.pingMs} ms",
            fontSize = 11.5.sp,
            color = TextMuted,
            fontWeight = FontWeight.Medium
          )
        }

        Text(text = "·", color = TextMuted2)

        // Load Meter
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          LinearProgressIndicator(
            progress = { server.loadPercent / 100f },
            modifier = Modifier
              .width(32.dp)
              .height(3.5.dp)
              .clip(RoundedCornerShape(2.dp)),
            color = if (server.loadPercent > 70) WarningAmber else VibrantBlue,
            trackColor = Color(0x22FFFFFF)
          )
          Text(
            text = "${server.loadPercent}%",
            fontSize = 10.5.sp,
            color = TextMuted2,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    // Action buttons: Info, Favorite, Connect
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
      IconButton(
        onClick = onInfoClick,
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Outlined.Info,
          contentDescription = "Details",
          tint = TextMuted2,
          modifier = Modifier.size(17.dp)
        )
      }

      IconButton(
        onClick = onToggleFavorite,
        modifier = Modifier
          .size(32.dp)
          .testTag("server_favorite_${server.id}")
      ) {
        Icon(
          imageVector = if (server.isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
          contentDescription = "Favorite",
          tint = if (server.isFavorite) WarningAmber else TextMuted2,
          modifier = Modifier.size(18.dp)
        )
      }

      Button(
        onClick = onConnect,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isSelected) OkEmerald.copy(alpha = 0.2f) else VibrantBlue.copy(alpha = 0.85f)
        ),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier
          .height(28.dp)
          .testTag("server_connect_btn_${server.id}")
      ) {
        Text(
          text = if (isSelected) "Active" else "Select",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          color = if (isSelected) OkEmerald else Color.White
        )
      }
    }
  }
}
