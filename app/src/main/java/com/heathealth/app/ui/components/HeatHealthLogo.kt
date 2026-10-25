package com.heathealth.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.heathealth.app.R

@Composable
fun HeatHealthLogo(
    modifier: Modifier = Modifier,
    contentDescription: String = "HeatHealth Logo"
) {
    Image(
        painter = painterResource(id = R.drawable.heathealth_logo),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
