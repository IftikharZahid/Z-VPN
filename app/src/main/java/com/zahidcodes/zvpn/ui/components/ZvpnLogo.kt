package com.zahidcodes.zvpn.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zahidcodes.zvpn.R

@Composable
fun ZvpnLogoMark(
  size: Dp = 38.dp,
  modifier: Modifier = Modifier
) {
  Image(
    painter = painterResource(id = R.drawable.ic_zvpn_logo),
    contentDescription = "ZVPN Logo",
    contentScale = ContentScale.Fit,
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(10.dp))
  )
}
