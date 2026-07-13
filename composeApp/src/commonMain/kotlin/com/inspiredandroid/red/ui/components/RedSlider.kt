@file:OptIn(ExperimentalMaterial3Api::class)

package com.inspiredandroid.red.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.inspiredandroid.red.ui.handCursor

@Composable
fun RedSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
) {
    Slider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier.handCursor(),
        valueRange = valueRange,
        steps = steps,
        colors = redSliderColors(),
        thumb = { RedSliderThumb() },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                colors = redSliderTrackColors(),
                drawStopIndicator = null,
                drawTick = { _, _ -> },
            )
        },
    )
}

@Composable
fun RedRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
) {
    RangeSlider(
        value = value,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        modifier = modifier.handCursor(),
        valueRange = valueRange,
        steps = steps,
        startThumb = { RedSliderThumb() },
        endThumb = { RedSliderThumb() },
        track = { rangeSliderState ->
            SliderDefaults.Track(
                rangeSliderState = rangeSliderState,
                colors = redSliderTrackColors(),
                drawStopIndicator = null,
                drawTick = { _, _ -> },
            )
        },
    )
}

@Composable
private fun RedSliderThumb() {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
    )
}

@Composable
private fun redSliderColors() = SliderDefaults.colors(
    thumbColor = MaterialTheme.colorScheme.primary,
    activeTrackColor = MaterialTheme.colorScheme.primary,
    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    activeTickColor = Color.Transparent,
    inactiveTickColor = Color.Transparent,
)

@Composable
private fun redSliderTrackColors() = SliderDefaults.colors(
    activeTrackColor = MaterialTheme.colorScheme.primary,
    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
)
