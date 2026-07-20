package com.inspiredandroid.red.ui.foundation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.inspiredandroid.red.ui.RedOnline

@Composable
fun RedStatusDot(
    modifier: Modifier = Modifier,
    size: Dp = 8.dp,
    color: androidx.compose.ui.graphics.Color = RedOnline,
    pulsing: Boolean = false,
) {
    if (pulsing) {
        val transition = rememberInfiniteTransition(label = "statusPulse")
        val pulseAlpha by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.35f,
            animationSpec = infiniteRepeatable(
                animation = tween(800),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulseAlpha",
        )
        Box(
            modifier = modifier
                .size(size)
                .alpha(pulseAlpha)
                .background(color, CircleShape),
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .background(color, CircleShape),
        )
    }
}
