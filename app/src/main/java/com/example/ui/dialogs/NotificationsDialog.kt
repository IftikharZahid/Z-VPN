package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun NotificationsDialog(
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = DarkSurfaceElevated,
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Icon(
          imageVector = Icons.Rounded.Notifications,
          contentDescription = null,
          tint = CyanAccent,
          modifier = Modifier.size(20.dp)
        )
        Text("Notifications", color = TextPrimary, fontWeight = FontWeight.Bold)
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        NotificationItem(
          title = "CleanWeb CyberShield",
          body = "1,429 malicious trackers and malicious ads blocked this week.",
          time = "10m ago",
          isSuccess = true
        )
        NotificationItem(
          title = "WireGuard Protocol Update",
          body = "Latest kernel optimizations applied for 15% lower latency.",
          time = "2h ago",
          isSuccess = false
        )
        NotificationItem(
          title = "IP Masked Successfully",
          body = "Your real IP is concealed behind RAM-only zero-log server.",
          time = "Yesterday",
          isSuccess = true
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Dismiss", color = CyanAccent)
      }
    }
  )
}

@Composable
private fun NotificationItem(
  title: String,
  body: String,
  time: String,
  isSuccess: Boolean
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0x0CFFFFFF))
      .padding(10.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Box(
      modifier = Modifier
        .size(28.dp)
        .clip(CircleShape)
        .background(if (isSuccess) OkEmerald.copy(alpha = 0.2f) else CyanAccent.copy(alpha = 0.2f)),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = if (isSuccess) Icons.Rounded.Check else Icons.Rounded.Shield,
        contentDescription = null,
        tint = if (isSuccess) OkEmerald else CyanAccent,
        modifier = Modifier.size(16.dp)
      )
    }

    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(title, color = TextPrimary, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
        Text(time, color = TextMuted, fontSize = 10.sp)
      }
      Text(body, color = TextMuted, fontSize = 11.5.sp)
    }
  }
}
