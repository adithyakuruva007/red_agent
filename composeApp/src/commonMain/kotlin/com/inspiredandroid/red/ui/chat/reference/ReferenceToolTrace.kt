package com.inspiredandroid.red.ui.chat.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedAgentViolet
import com.inspiredandroid.red.ui.RedBgElevated
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedOnline
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.RedTextTertiary

@Composable
fun ReferenceToolTrace(
    completed: List<String>,
    inProgress: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(max = 320.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(RedBgElevated)
            .border(1.dp, RedBorderHairline, RoundedCornerShape(14.dp))
            .padding(horizontal = 11.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = "Agent is working",
            color = RedAgentViolet,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp,
        )

        completed.forEach { label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = RedOnline,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = label,
                    color = RedTextTertiary,
                    fontSize = 12.5.sp,
                )
            }
        }

        inProgress.forEach { label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = RedAccent,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = label,
                    color = RedTextSecondary,
                    fontSize = 12.5.sp,
                )
            }
        }
    }
}
