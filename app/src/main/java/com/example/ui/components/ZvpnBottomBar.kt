package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurfaceStroke
import com.example.ui.theme.TabBarBg
import com.example.ui.theme.TextMuted2
import com.example.ui.theme.VibrantBlue

enum class VpnScreen(val title: String, val icon: ImageVector) {
  HOME("Home", Icons.Rounded.Home),
  SERVERS("Servers", Icons.Rounded.Public),
  STATS("Stats", Icons.Rounded.BarChart),
  SETTINGS("Settings", Icons.Rounded.Tune)
}

@Composable
fun ZvpnBottomBar(
  currentScreen: VpnScreen,
  onScreenSelected: (VpnScreen) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(TabBarBg)
      .navigationBarsPadding()
  ) {
    HorizontalDivider(
      thickness = 1.dp,
      color = DarkSurfaceStroke
    )

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 6.dp),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      VpnScreen.entries.forEach { screen ->
        val isSelected = screen == currentScreen
        val iconColor by animateColorAsState(
          targetValue = if (isSelected) Color(0xFF62A8FF) else TextMuted2,
          label = "tab_color"
        )

        Column(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onScreenSelected(screen) }
            .padding(vertical = 6.dp)
            .testTag("nav_tab_${screen.name.lowercase()}"),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            if (isSelected) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(VibrantBlue.copy(alpha = 0.15f))
              )
            }
            Icon(
              imageVector = screen.icon,
              contentDescription = screen.title,
              tint = iconColor,
              modifier = Modifier.size(22.dp)
            )
          }

          Text(
            text = screen.title,
            color = iconColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
          )
        }
      }
    }
  }
}
