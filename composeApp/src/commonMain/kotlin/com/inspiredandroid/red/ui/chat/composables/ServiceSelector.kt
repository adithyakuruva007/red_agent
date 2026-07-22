package com.inspiredandroid.red.ui.chat.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.inspiredandroid.red.data.ServiceEntry
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.handCursor
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.painterResource

@Composable
fun ServiceSelector(
    services: ImmutableList<ServiceEntry>,
    onSelectService: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (services.isEmpty()) return

    val current = services.first()
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .clickable { expanded = true }
                .handCursor(),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(current.icon),
                contentDescription = current.serviceName,
                tint = RedAccent,
                modifier = Modifier.size(16.dp),
            )
        }

        if (expanded) {
            val spacingPx = with(LocalDensity.current) { 8.dp.roundToPx() }
            Popup(
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true),
                popupPositionProvider = remember(spacingPx) { AnchorAbovePositionProvider(spacingPx) },
            ) {
                BoxWithConstraints {
                    val maxMenuHeight = maxHeight - 32.dp
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = RedBgPanel,
                        border = BorderStroke(1.dp, RedBorderHairline),
                        shadowElevation = 8.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .heightIn(max = maxMenuHeight)
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 6.dp),
                        ) {
                            services.forEach { entry ->
                                val isCurrent = entry.instanceId == current.instanceId
                                ServiceMenuItem(
                                    entry = entry,
                                    isCurrent = isCurrent,
                                    onClick = {
                                        expanded = false
                                        if (!isCurrent) {
                                            onSelectService(entry.instanceId)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ServiceMenuItem(
    entry: ServiceEntry,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    val rowBackground = if (isCurrent) RedAccent.copy(alpha = 0.15f) else Color.Transparent
    val textColor = if (isCurrent) RedAccent else RedTextPrimary
    val subTextColor = if (isCurrent) RedAccent.copy(alpha = 0.8f) else RedTextSecondary

    Row(
        modifier = Modifier
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(rowBackground)
            .clickable(onClick = onClick)
            .handCursor()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .widthIn(min = 210.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(entry.icon),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = textColor,
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = entry.serviceName,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
            )
            if (entry.modelId.isNotEmpty()) {
                Text(
                    text = entry.modelId,
                    fontSize = 11.5.sp,
                    color = subTextColor,
                )
            }
        }
    }
}

private class AnchorAbovePositionProvider(
    private val verticalSpacing: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val maxX = (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        val x = (anchorBounds.right - popupContentSize.width).coerceIn(0, maxX)
        val above = anchorBounds.top - popupContentSize.height - verticalSpacing
        val y = if (above >= 0) {
            above
        } else {
            val maxY = (windowSize.height - popupContentSize.height).coerceAtLeast(0)
            (anchorBounds.bottom + verticalSpacing).coerceAtMost(maxY)
        }
        return IntOffset(x, y)
    }
}
