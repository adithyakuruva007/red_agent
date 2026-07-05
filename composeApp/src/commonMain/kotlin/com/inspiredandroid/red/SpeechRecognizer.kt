package com.inspiredandroid.red

import androidx.compose.runtime.Composable

interface PlatformSpeechRecognizer {
    val isListening: Boolean
    fun startListening()
    fun stopListening()
}

@Composable
expect fun rememberPlatformSpeechRecognizer(
    onResult: (String) -> Unit,
    onListeningChange: (Boolean) -> Unit
): PlatformSpeechRecognizer
