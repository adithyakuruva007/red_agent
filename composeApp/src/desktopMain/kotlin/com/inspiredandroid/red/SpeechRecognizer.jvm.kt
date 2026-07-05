package com.inspiredandroid.red

import androidx.compose.runtime.*
import kotlinx.coroutines.*

@Composable
actual fun rememberPlatformSpeechRecognizer(
    onResult: (String) -> Unit,
    onListeningChange: (Boolean) -> Unit
): PlatformSpeechRecognizer {
    val coroutineScope = rememberCoroutineScope()
    var listeningState by remember { mutableStateOf(false) }
    var listeningJob by remember { mutableStateOf<Job?>(null) }

    return remember(coroutineScope, listeningState) {
        object : PlatformSpeechRecognizer {
            override val isListening: Boolean
                get() = listeningState

            override fun startListening() {
                listeningJob?.cancel()
                listeningState = true
                onListeningChange(true)
                
                listeningJob = coroutineScope.launch {
                    delay(3000) // Simulate 3 seconds of recording
                    if (listeningState) {
                        onResult("Simulated voice input transcription from desktop.")
                        listeningState = false
                        onListeningChange(false)
                    }
                }
            }

            override fun stopListening() {
                listeningJob?.cancel()
                listeningState = false
                onListeningChange(false)
            }
        }
    }
}
