package com.inspiredandroid.red.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsTab(
    viewModel: SettingsViewModel = koinViewModel(),
    sandboxViewModel: SandboxViewModel = koinViewModel(),
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val sandboxState by sandboxViewModel.state.collectAsStateWithLifecycle()

    SettingsScreenContent(
        uiState = uiState,
        actions = viewModel.actions,
        sandboxState = sandboxState,
        onToggleSandbox = sandboxViewModel::onToggleSandbox,
        onSetupSandbox = sandboxViewModel::onSetupSandbox,
        onCancelSandbox = sandboxViewModel::onCancelSandbox,
        onResetSandbox = sandboxViewModel::onResetSandbox,
        onInstallPackages = sandboxViewModel::onInstallPackages,
        onNavigateBack = {},
    )
}
