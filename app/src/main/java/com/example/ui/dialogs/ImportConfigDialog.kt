package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ImportConfigDialog(
  onDismiss: () -> Unit,
  onImport: (String) -> Unit
) {
  var configText by remember { mutableStateOf("") }

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
          modifier = Modifier.size(22.dp)
        )
        Text("Import VPN Configuration", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    },
    text = {
      Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Paste VLESS, VMess, or Trojan configuration URIs below (one or multiple per line):",
          color = TextMuted,
          fontSize = 12.sp
        )

        OutlinedTextField(
          value = configText,
          onValueChange = { configText = it },
          placeholder = {
            Text(
              "vless://uuid@ip:port?security=reality...#server-name",
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
            fontSize = 12.sp
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyanAccent,
            unfocusedBorderColor = DarkSurfaceStroke,
            focusedContainerColor = Color(0x10FFFFFF),
            unfocusedContainerColor = Color(0x08FFFFFF)
          ),
          shape = RoundedCornerShape(12.dp)
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (configText.isNotBlank()) {
            onImport(configText)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("confirm_import_button")
      ) {
        Text("Import Configs", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMuted)
      }
    }
  )
}
