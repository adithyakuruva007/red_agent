package com.inspiredandroid.red.ui.chat.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.skills.SkillManifest
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.handCursor
import kotlinx.collections.immutable.ImmutableList

/**
 * Drop-down list of skills & tools shown above the chat composer when the user types `/`.
 * Selection replaces the leading `/<query>` token with `/<id> `, leaving the
 * cursor positioned for follow-up args.
 */
@Composable
internal fun SkillAutocomplete(
    skills: ImmutableList<SkillManifest>,
    query: String,
    onSelect: (SkillManifest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filtered = remember(skills, query) {
        val q = query.lowercase()
        if (q.isEmpty()) {
            skills
        } else {
            skills.filter { it.id.lowercase().contains(q) || it.displayName.lowercase().contains(q) }
        }
    }
    if (filtered.isEmpty()) return

    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(RedBgPanel)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(RedAccent, Color(0xFF3E5FCB))),
                shape = RoundedCornerShape(14.dp),
            )
            .heightIn(max = 220.dp)
            .verticalScroll(scrollState),
    ) {
        for (skill in filtered) {
            SkillRow(
                skill = skill,
                onClick = { onSelect(skill) },
            )
        }
    }
}

@Composable
private fun SkillRow(skill: SkillManifest, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .handCursor()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(RedAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Terminal,
                contentDescription = null,
                tint = RedAccent,
                modifier = Modifier.size(16.dp),
            )
        }

        Spacer(Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "/${skill.id}",
                color = RedTextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
            )
            if (skill.description.isNotEmpty()) {
                Text(
                    text = skill.description,
                    color = RedTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
