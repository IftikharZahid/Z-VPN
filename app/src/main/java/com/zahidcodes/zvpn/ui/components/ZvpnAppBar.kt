package com.zahidcodes.zvpn.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextPrimary

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.rounded.Dns
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceElevated
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.TextMuted

@Composable
fun ZvpnAppBar(
  onImportClick: () -> Unit,
  onAddManualServerClick: () -> Unit = onImportClick,
  onTestPingClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var showPlusMenu by remember { mutableStateOf(false) }

  Row(
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp)
      .padding(horizontal = 20.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    // Brand with ZvpnLogoMark
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      ZvpnLogoMark(size = 38.dp)

      Text(
        text = "ZVPN",
        color = TextPrimary,
        fontSize = 19.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.3).sp
      )
    }

    // Actions - Ping Test and Plus Import/Manual buttons
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Professional Ping Test Button with "Ping" text
      Box(
        modifier = Modifier
          .height(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(OkEmerald.copy(alpha = 0.15f))
          .border(1.dp, OkEmerald.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
          .clickable(onClick = onTestPingClick)
          .padding(horizontal = 12.dp)
          .testTag("app_bar_test_ping_button"),
        contentAlignment = Alignment.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Rounded.NetworkPing,
            contentDescription = "Test Ping",
            tint = OkEmerald,
            modifier = Modifier.size(16.dp)
          )
          Text(
            text = "Ping",
            color = OkEmerald,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.3.sp
          )
        }
      }

      // Plus Add/Import Button with Dropdown Menu
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(CyanAccent.copy(alpha = 0.15f))
          .border(1.dp, CyanAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
          .clickable { showPlusMenu = true }
          .testTag("app_bar_import_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Rounded.Add,
          contentDescription = "Add or Import Server",
          tint = CyanAccent,
          modifier = Modifier.size(20.dp)
        )

        DropdownMenu(
          expanded = showPlusMenu,
          onDismissRequest = { showPlusMenu = false },
          modifier = Modifier
            .background(DarkSurfaceElevated)
            .border(1.dp, DarkSurfaceStroke, RoundedCornerShape(8.dp))
            .testTag("app_bar_plus_dropdown_menu")
        ) {
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
              onImportClick()
            },
            colors = MenuDefaults.itemColors()
          )

          DropdownMenuItem(
            text = {
              Column {
                Text("Add Server Manually", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("Custom host, port, credentials & connect", color = TextMuted, fontSize = 10.5.sp)
              }
            },
            leadingIcon = {
              Icon(Icons.Rounded.Dns, contentDescription = null, tint = OkEmerald, modifier = Modifier.size(18.dp))
            },
            onClick = {
              showPlusMenu = false
              onAddManualServerClick()
            },
            colors = MenuDefaults.itemColors()
          )
        }
      }
    }
  }
}

