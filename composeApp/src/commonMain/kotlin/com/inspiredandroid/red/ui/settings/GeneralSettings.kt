package com.inspiredandroid.red.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.inspiredandroid.red.data.AppSettings
import com.inspiredandroid.red.data.AppColorScheme
import org.koin.compose.koinInject
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.inspiredandroid.red.ui.RedOutlinedTextField
import com.inspiredandroid.red.ui.components.RedSlider
import com.inspiredandroid.red.ui.handCursor
import red.composeapp.generated.resources.Res
import red.composeapp.generated.resources.ic_arrow_drop_down
import red.composeapp.generated.resources.settings_daemon_mode
import red.composeapp.generated.resources.settings_daemon_mode_description
import red.composeapp.generated.resources.settings_dynamic_ui
import red.composeapp.generated.resources.settings_dynamic_ui_description
import red.composeapp.generated.resources.settings_theme
import red.composeapp.generated.resources.settings_theme_dark
import red.composeapp.generated.resources.settings_theme_description
import red.composeapp.generated.resources.settings_theme_light
import red.composeapp.generated.resources.settings_theme_oled
import red.composeapp.generated.resources.settings_theme_system
import red.composeapp.generated.resources.settings_ui_scale
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.roundToInt

@Composable
internal fun GeneralContent(uiState: SettingsUiState, actions: SettingsActions) {
    val appSettings: AppSettings = koinInject()
    val colorScheme by appSettings.colorSchemeFlow.collectAsStateWithLifecycle()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val useStaggered = maxWidth >= 600.dp
        if (useStaggered) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (uiState.showDaemonToggle) {
                        SettingsCard {
                            DaemonModeToggle(
                                isDaemonEnabled = uiState.isDaemonEnabled,
                                onToggleDaemon = actions.onToggleDaemon,
                            )
                        }
                    }
                    SettingsCard {
                        DynamicUiToggle(
                            isDynamicUiEnabled = uiState.isDynamicUiEnabled,
                            onToggleDynamicUi = actions.onToggleDynamicUi,
                        )
                    }

                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (uiState.showUiScale) {
                        SettingsCard {
                            UiScaleSection(
                                uiScale = uiState.uiScale,
                                onChangeUiScale = actions.onChangeUiScale,
                            )
                        }
                    }
                    SettingsCard {
                        ExportImportSection(
                            onExportSettings = actions.onExportSettings,
                            onPrepareExport = actions.onPrepareExport,
                            onImportSettings = actions.onImportSettings,
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (uiState.showDaemonToggle) {
                    SettingsCard {
                        DaemonModeToggle(
                            isDaemonEnabled = uiState.isDaemonEnabled,
                            onToggleDaemon = actions.onToggleDaemon,
                        )
                    }
                }
                SettingsCard {
                    DynamicUiToggle(
                        isDynamicUiEnabled = uiState.isDynamicUiEnabled,
                        onToggleDynamicUi = actions.onToggleDynamicUi,
                    )
                }


                if (uiState.showUiScale) {
                    SettingsCard {
                        UiScaleSection(
                            uiScale = uiState.uiScale,
                            onChangeUiScale = actions.onChangeUiScale,
                        )
                    }
                }
                SettingsCard {
                    ExportImportSection(
                        onExportSettings = actions.onExportSettings,
                        onPrepareExport = actions.onPrepareExport,
                        onImportSettings = actions.onImportSettings,
                    )
                }
            }
        }
    }
}



@Composable
private fun DaemonModeToggle(
    isDaemonEnabled: Boolean,
    onToggleDaemon: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ToggleableHeadline(
            title = stringResource(Res.string.settings_daemon_mode),
            description = stringResource(Res.string.settings_daemon_mode_description),
            checked = isDaemonEnabled,
            onCheckedChange = onToggleDaemon,
        )
    }
}

@Composable
private fun DynamicUiToggle(
    isDynamicUiEnabled: Boolean,
    onToggleDynamicUi: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        ToggleableHeadline(
            title = stringResource(Res.string.settings_dynamic_ui),
            description = stringResource(Res.string.settings_dynamic_ui_description),
            checked = isDynamicUiEnabled,
            onCheckedChange = onToggleDynamicUi,
        )
    }
}


@Composable
private fun UiScaleSection(
    uiScale: Float,
    onChangeUiScale: (Float) -> Unit,
) {
    var sliderValue by remember(uiScale) { mutableStateOf(uiScale) }
    val steps = 14 // 16 snap points from 50% to 200% in 10% increments (14 intermediate)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.settings_ui_scale),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "${(sliderValue * 100).roundToInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        RedSlider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onChangeUiScale(sliderValue) },
            valueRange = 0.5f..2.0f,
            steps = steps,
        )
    }
}



