package com.inspiredandroid.red.ui.chat.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.inspiredandroid.red.ui.components.LogoAnimation
import com.inspiredandroid.red.ui.components.animatedGradientBorder
import com.inspiredandroid.red.ui.handCursor
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.privacy_agree_prefix
import red.composeapp.generated.resources.privacy_policy
import red.composeapp.generated.resources.start_interactive_ui
import red.composeapp.generated.resources.welcome_message
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun EmptyState(
    modifier: Modifier,
    isUsingSharedKey: Boolean,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LogoAnimation()
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(Res.string.welcome_message),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        if (isUsingSharedKey) {
            val linkColor = MaterialTheme.colorScheme.primary
            val prefixText = stringResource(Res.string.privacy_agree_prefix)
            val policyText = stringResource(Res.string.privacy_policy)
            val annotatedString = remember(prefixText, policyText, linkColor) {
                buildAnnotatedString {
                    append(prefixText)
                    withLink(LinkAnnotation.Url(url = "https://raw.githubusercontent.com/adithyakuruva007/red_agent/main/PRIVACY.md")) {
                        withStyle(style = SpanStyle(color = linkColor)) {
                            append(policyText)
                        }
                    }
                }
            }
            Text(
                annotatedString,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}
