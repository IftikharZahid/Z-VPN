package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Server
import com.example.model.VpnStatus
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.OkEmerald
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextMuted2
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.VibrantBlue
import java.util.Locale

@Composable
fun StatsScreen(
  vpnStatus: VpnStatus,
  selectedServer: Server,
  sessionSeconds: Long,
  downloadSpeed: String,
  uploadSpeed: String,
  totalDownloadedMb: Double,
  totalUploadedMb: Double,
  trafficHistory: List<Float>,
  modifier: Modifier = Modifier
) {
  val hours = sessionSeconds / 3600
  val minutes = (sessionSeconds % 3600) / 60
  val seconds = sessionSeconds % 60
  val durationFormatted = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 20.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
  ) {
    // Title
    item {
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
          text = "Network Analytics",
          fontSize = 24.sp,
          fontWeight = FontWeight.Bold,
          color = TextPrimary
        )
        Text(
          text = "Real-time bandwidth throughput & security telemetry",
          fontSize = 13.sp,
          color = TextMuted
        )
      }
    }

    // Bandwidth Chart Card
    item {
      val shape = RoundedCornerShape(20.dp)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(shape)
          .background(DarkSurfaceCard)
          .border(1.dp, DarkSurfaceStroke, shape)
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(if (vpnStatus == VpnStatus.CONNECTED) OkEmerald else VibrantBlue)
            )
            Text(
              text = "LIVE BANDWIDTH",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.8.sp,
              color = TextMuted
            )
          }

          Text(
            text = if (vpnStatus == VpnStatus.CONNECTED) downloadSpeed else "IDLE",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (vpnStatus == VpnStatus.CONNECTED) CyanAccent else TextMuted2
          )
        }

        // Custom Canvas Chart
        BandwidthChart(
          dataPoints = if (vpnStatus == VpnStatus.CONNECTED) trafficHistory else listOf(0f, 0f, 0f, 0f),
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
        )
      }
    }

    // Duration & Transferred Data Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Session Duration
        MetricCard(
          title = "SESSION TIME",
          value = if (vpnStatus == VpnStatus.CONNECTED) durationFormatted else "00:00:00",
          icon = Icons.Rounded.Timer,
          accentColor = CyanAccent,
          modifier = Modifier.weight(1f)
        )

        // Ping Latency
        MetricCard(
          title = "CURRENT PING",
          value = if (vpnStatus == VpnStatus.CONNECTED) "${selectedServer.pingMs} ms" else "—",
          icon = Icons.Rounded.Speed,
          accentColor = OkEmerald,
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Download & Upload Breakdown
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        val dlMb = if (totalDownloadedMb > 1024) String.format(Locale.US, "%.2f GB", totalDownloadedMb / 1024.0)
                   else String.format(Locale.US, "%.1f MB", totalDownloadedMb)

        val upMb = if (totalUploadedMb > 1024) String.format(Locale.US, "%.2f GB", totalUploadedMb / 1024.0)
                   else String.format(Locale.US, "%.1f MB", totalUploadedMb)

        MetricCard(
          title = "DATA DOWNLOADED",
          value = if (vpnStatus == VpnStatus.CONNECTED) dlMb else "0.0 MB",
          icon = Icons.Rounded.ArrowDownward,
          accentColor = VibrantBlue,
          modifier = Modifier.weight(1f)
        )

        MetricCard(
          title = "DATA UPLOADED",
          value = if (vpnStatus == VpnStatus.CONNECTED) upMb else "0.0 MB",
          icon = Icons.Rounded.ArrowUpward,
          accentColor = Color(0xFFA855F7),
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Security Diagnostics Card
    item {
      val shape = RoundedCornerShape(20.dp)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(shape)
          .background(DarkSurfaceCard)
          .border(1.dp, DarkSurfaceStroke, shape)
          .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Text(
          text = "SECURITY INTEGRITY AUDIT",
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp,
          color = TextMuted2
        )

        SecurityAuditRow("DNS Leak Protection", "Active & Encrypted", true)
        SecurityAuditRow("IPv6 Tunneling", "Masked & Shielded", true)
        SecurityAuditRow("Kill Switch Daemon", "Armed (Auto-block)", true)
        SecurityAuditRow("Cipher Suite", "ChaCha20-Poly1305 256-bit", true)
        SecurityAuditRow("Selected Gateway", "${selectedServer.city} (${selectedServer.ipAddress})", true)
      }
    }
  }
}

@Composable
private fun SecurityAuditRow(
  title: String,
  status: String,
  isSecure: Boolean
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      fontSize = 13.sp,
      color = TextPrimary,
      fontWeight = FontWeight.Medium
    )
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Text(
        text = status,
        fontSize = 12.sp,
        color = if (isSecure) OkEmerald else TextMuted
      )
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = null,
        tint = OkEmerald,
        modifier = Modifier.size(15.dp)
      )
    }
  }
}

@Composable
private fun MetricCard(
  title: String,
  value: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier
) {
  val shape = RoundedCornerShape(18.dp)
  Column(
    modifier = modifier
      .clip(shape)
      .background(DarkSurfaceCard)
      .border(1.dp, DarkSurfaceStroke, shape)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = accentColor,
        modifier = Modifier.size(16.dp)
      )
      Text(
        text = title,
        fontSize = 9.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = TextMuted2
      )
    }
    Text(
      text = value,
      fontSize = 16.sp,
      fontWeight = FontWeight.Bold,
      color = TextPrimary,
      fontFamily = FontFamily.Default
    )
  }
}

@Composable
private fun BandwidthChart(
  dataPoints: List<Float>,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    if (dataPoints.isEmpty()) return@Canvas

    val width = size.width
    val height = size.height
    val maxVal = (dataPoints.maxOrNull() ?: 100f).coerceAtLeast(10f)

    // Background horizontal grid lines
    val gridLines = 3
    for (i in 0..gridLines) {
      val y = height * (i.toFloat() / gridLines)
      drawLine(
        color = Color(0x0CFFFFFF),
        start = Offset(0f, y),
        end = Offset(width, y),
        strokeWidth = 1.dp.toPx()
      )
    }

    val stepX = width / (dataPoints.size - 1).coerceAtLeast(1)
    val points = dataPoints.mapIndexed { index, value ->
      val x = index * stepX
      val normalizedY = 1f - (value / (maxVal * 1.25f))
      val y = (normalizedY * height).coerceIn(4f, height - 4f)
      Offset(x, y)
    }

    val path = Path()
    val fillPath = Path()

    points.forEachIndexed { i, pt ->
      if (i == 0) {
        path.moveTo(pt.x, pt.y)
        fillPath.moveTo(pt.x, height)
        fillPath.lineTo(pt.x, pt.y)
      } else {
        val prev = points[i - 1]
        val cX1 = (prev.x + pt.x) / 2f
        val cY1 = prev.y
        val cX2 = (prev.x + pt.x) / 2f
        val cY2 = pt.y
        path.cubicTo(cX1, cY1, cX2, cY2, pt.x, pt.y)
        fillPath.cubicTo(cX1, cY1, cX2, cY2, pt.x, pt.y)
      }
    }

    fillPath.lineTo(width, height)
    fillPath.close()

    // Draw gradient area beneath line
    drawPath(
      path = fillPath,
      brush = Brush.verticalGradient(
        colors = listOf(CyanAccent.copy(alpha = 0.35f), Color.Transparent)
      )
    )

    // Draw stroked glowing curve
    drawPath(
      path = path,
      color = CyanAccent,
      style = Stroke(width = 2.5.dp.toPx())
    )

    // Draw active dot at latest point
    points.lastOrNull()?.let { last ->
      drawCircle(Color(0xFF22D3EE), radius = 5.dp.toPx(), center = last)
      drawCircle(Color.White, radius = 2.5.dp.toPx(), center = last)
    }
  }
}
