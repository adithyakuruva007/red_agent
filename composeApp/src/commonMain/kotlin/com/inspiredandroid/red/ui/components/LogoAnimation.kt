package com.inspiredandroid.red.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.imageResource
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.logo

@Composable
fun LogoAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 144.dp,
) {
    val imageBitmap = imageResource(Res.drawable.logo)
    
    val infiniteTransition = rememberInfiniteTransition(label = "logoRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAngle"
    )

    Image(
        bitmap = imageBitmap,
        contentDescription = "Red Logo",
        modifier = modifier
            .size(size)
            .rotate(rotationAngle),
        filterQuality = FilterQuality.High,
    )
}

