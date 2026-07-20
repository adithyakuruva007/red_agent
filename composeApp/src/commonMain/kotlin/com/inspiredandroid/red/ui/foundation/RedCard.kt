package com.inspiredandroid.red.ui.foundation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedBorderHairline

@Composable
fun RedCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = RedBgPanel),
        border = BorderStroke(1.dp, RedBorderHairline),
    ) {
        content()
    }
}

@Composable
fun RedCardContentPadding(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    RedCard(modifier = modifier) {
        Box(modifier = Modifier.padding(14.dp)) {
            content()
        }
    }
}
