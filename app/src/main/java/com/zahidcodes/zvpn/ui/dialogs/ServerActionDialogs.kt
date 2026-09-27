package com.zahidcodes.zvpn.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zahidcodes.zvpn.model.Server
import com.zahidcodes.zvpn.model.ServerSortOption
import com.zahidcodes.zvpn.ui.theme.CyanAccent
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceElevated
import com.zahidcodes.zvpn.ui.theme.DarkSurfaceStroke
import com.zahidcodes.zvpn.ui.theme.OkEmerald
import com.zahidcodes.zvpn.ui.theme.TextMuted
import com.zahidcodes.zvpn.ui.theme.TextPrimary
import com.zahidcodes.zvpn.ui.theme.VibrantBlue

@Composable
fun SortServersDialog(
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
fun DeleteAllConfigurationsDialog(
  servers: List<Server>,
  onClearAllImported: () -> Unit,
  onDeleteAll: () -> Unit,
  onDismiss: () -> Unit
) {
  val importedCount = servers.count { it.isImported }
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
          text = "Select deletion scope:",
          color = TextMuted,
          fontSize = 12.sp
        )

        if (importedCount > 0) {
          Button(
            onClick = {
              onClearAllImported()
              onDismiss()
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
            onDismiss()
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
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMuted, fontSize = 12.sp)
      }
    }
  )
}

@Composable
fun FirestoreFormatDialog(
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
