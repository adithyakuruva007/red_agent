package com.inspiredandroid.red.ui.foundation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedTextTertiary

@Composable
fun ThinkingDots(
    modifier: Modifier = Modifier,
    dotSize: Dp = 4.dp,
    color: androidx.compose.ui.graphics.Color = RedAccent,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(3) { index ->
            val transition = rememberInfiniteTransition(label = "thinkDot$index")
            val dotAlpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dotAlpha$index",
            )
            val dotScale by transition.animateFloat(
                initialValue = 1f,
                targetValue = 0.6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, delayMillis = index * 150),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dotScale$index",
            )
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .scale(dotScale)
                    .alpha(dotAlpha)
                    .background(color, CircleShape),
            )
        }
    }
}
