package com.inspiredandroid.red.data

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class ContactRecord(
    val id: String,
    val displayName: String,
    val phoneNumbers: List<String>,
    val emails: List<String>
)
