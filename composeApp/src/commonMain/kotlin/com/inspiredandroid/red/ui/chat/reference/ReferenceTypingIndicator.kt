package com.inspiredandroid.red.ui.chat.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedTextTertiary
import com.inspiredandroid.red.ui.foundation.ThinkingDots

@Composable
fun ReferenceTypingIndicator(
    avatarLabel: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFF8B6CF2), Color(0xFF5E3FCB))),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = avatarLabel.firstOrNull()?.uppercase() ?: "A",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp))
                .background(RedBgPanel)
                .border(1.dp, RedBorderHairline, RoundedCornerShape(18.dp, 18.dp, 18.dp, 5.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            ThinkingDots(
                dotSize = 6.dp,
                color = RedTextTertiary,
            )
        }
    }
}
