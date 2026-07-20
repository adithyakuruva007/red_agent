package com.inspiredandroid.red.ui.foundation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgElevated2
import com.inspiredandroid.red.ui.RedDanger
import com.inspiredandroid.red.ui.RedTextTertiary

@Composable
fun RedBadge(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = RedAccent,
    muted: Boolean = false,
) {
    val bg = if (muted) RedBgElevated2 else color
    val textColor = if (muted) RedTextTertiary else Color.White
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (count > 99) "99+" else count.toString(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun RedDotBadge(
    modifier: Modifier = Modifier,
    color: Color = RedDanger,
    size: Int = 8,
) {
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(size.dp / 2))
            .size(size.dp),
    )
}
