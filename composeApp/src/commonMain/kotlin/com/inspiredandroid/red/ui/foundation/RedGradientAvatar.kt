package com.inspiredandroid.red.ui.foundation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.RedGrad1End
import com.inspiredandroid.red.ui.RedGrad1Start
import com.inspiredandroid.red.ui.RedGrad2End
import com.inspiredandroid.red.ui.RedGrad2Start
import com.inspiredandroid.red.ui.RedGrad3End
import com.inspiredandroid.red.ui.RedGrad3Start
import com.inspiredandroid.red.ui.RedGrad4End
import com.inspiredandroid.red.ui.RedGrad4Start
import com.inspiredandroid.red.ui.RedGrad5End
import com.inspiredandroid.red.ui.RedGrad5Start
import com.inspiredandroid.red.ui.RedOnline

enum class AvatarGradient {
    Grad1, Grad2, Grad3, Grad4, Grad5
}

fun avatarGradient(index: Int): AvatarGradient = when (index % 5) {
    0 -> AvatarGradient.Grad1
    1 -> AvatarGradient.Grad2
    2 -> AvatarGradient.Grad3
    3 -> AvatarGradient.Grad4
    else -> AvatarGradient.Grad5
}

internal fun gradientBrush(grad: AvatarGradient): Brush = when (grad) {
    AvatarGradient.Grad1 -> Brush.linearGradient(listOf(RedGrad1Start, RedGrad1End))
    AvatarGradient.Grad2 -> Brush.linearGradient(listOf(RedGrad2Start, RedGrad2End))
    AvatarGradient.Grad3 -> Brush.linearGradient(listOf(RedGrad3Start, RedGrad3End))
    AvatarGradient.Grad4 -> Brush.linearGradient(listOf(RedGrad4Start, RedGrad4End))
    AvatarGradient.Grad5 -> Brush.linearGradient(listOf(RedGrad5Start, RedGrad5End))
}

@Composable
fun RedGradientAvatar(
    initial: String,
    grad: AvatarGradient = AvatarGradient.Grad1,
    size: Dp = 52.dp,
    fontSize: TextUnit = 18.sp,
    roundedCorner: Dp = 16.dp,
    modifier: Modifier = Modifier,
    isActive: Boolean = false,
    statusDotColor: Color? = null,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(roundedCorner))
            .background(gradientBrush(grad)),
        contentAlignment = Alignment.Center,
    ) {
        if (isActive) {
            val transition = rememberInfiniteTransition(label = "pulse")
            val ringAlpha by transition.animateFloat(
                initialValue = 0.9f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "ringAlpha",
            )
            val ringScale by transition.animateFloat(
                initialValue = 0.94f,
                targetValue = 1.08f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Restart,
                ),
                label = "ringScale",
            )
            Box(
                modifier = Modifier
                    .size(size * (1 + (ringScale - 0.94f) / 0.94f))
                    .clip(RoundedCornerShape(roundedCorner + 3.dp))
                    .border(
                        width = 2.dp,
                        color = RedOnline.copy(alpha = ringAlpha),
                        shape = RoundedCornerShape(roundedCorner + 3.dp),
                    ),
            )
        }
        Text(
            text = initial.take(2).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize,
        )
        if (statusDotColor != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 0.dp, y = 0.dp)
                    .size(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(statusDotColor),
            )
        }
    }
}
