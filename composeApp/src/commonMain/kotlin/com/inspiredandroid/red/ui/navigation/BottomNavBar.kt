package com.inspiredandroid.red.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedAccentSoft
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedDanger
import com.inspiredandroid.red.ui.RedOnline
import com.inspiredandroid.red.ui.RedTextTertiary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.foundation.border

enum class NavTab(val label: String) {
    Chats("Chats"),
    Sandbox("Sandbox"),
    Alerts("Alerts"),
    Settings("Settings"),
}

@Composable
fun BottomNavBar(
    activeTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    chatBadgeCount: Int = 0,
    alertBadgeCount: Int = 0,
    sandboxOnline: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(RedBgPanel.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
            .border(1.dp, RedBorderHairline, RoundedCornerShape(24.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NavTab.entries.forEach { tab ->
            val isActive = tab == activeTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (isActive) Modifier.background(RedAccentSoft) else Modifier
                    )
                    .clickable { onTabSelected(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.TopCenter) {
                        val iconColor = if (isActive) RedAccent else RedTextTertiary
                        val icon = when (tab) {
                            NavTab.Chats -> Icons.Default.Chat
                            NavTab.Sandbox -> Icons.Default.Terminal
                            NavTab.Alerts -> Icons.Default.Notifications
                            NavTab.Settings -> Icons.Default.Settings
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = tab.label,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp),
                        )
                        when (tab) {
                            NavTab.Chats -> if (chatBadgeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset { IntOffset(8, -4) }
                                        .background(RedDanger, RoundedCornerShape(7.5.dp))
                                        .size(15.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = if (chatBadgeCount > 9) "9+" else chatBadgeCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            NavTab.Sandbox -> if (sandboxOnline) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset { IntOffset(8, -2) }
                                        .size(7.dp)
                                        .background(RedOnline, RoundedCornerShape(3.5.dp)),
                                )
                            }
                            NavTab.Alerts -> if (alertBadgeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset { IntOffset(8, -4) }
                                        .background(RedDanger, RoundedCornerShape(7.5.dp))
                                        .size(15.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = if (alertBadgeCount > 9) "9+" else alertBadgeCount.toString(),
                                        color = Color.White,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                            NavTab.Settings -> {}
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = tab.label,
                        color = if (isActive) RedAccent else RedTextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
