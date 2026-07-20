package com.inspiredandroid.red.ui.sandbox

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.red.ui.RedBgDeep
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.RedTextTertiary
import com.inspiredandroid.red.ui.settings.SandboxViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SandboxTab(
    modifier: Modifier = Modifier,
    viewModel: SandboxViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (!state.showSandbox) {
        // Platform doesn't support sandbox (e.g. desktop JVM)
        Box(
            modifier = modifier.fillMaxSize().background(RedBgDeep),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "🖥",
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Sandbox",
                    color = RedTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "The Linux sandbox is not available on this platform.\nIt runs on Android devices with rootfs support.",
                    color = RedTextTertiary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    } else {
        SandboxTabsContent(
            sandboxState = state,
            onSetupSandbox = viewModel::onSetupSandbox,
            onCancelSandbox = viewModel::onCancelSandbox,
            modifier = modifier.fillMaxSize(),
        )
    }
}
