package com.inspiredandroid.red.ui.chat

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class Attachment(
    val data: String, // Base64 encoded bytes
    val mimeType: String,
    val fileName: String? = null,
)

enum class FileCategory {
    IMAGE,
    TEXT,
    PDF,
    UNSUPPORTED,
}

data class AttachmentSplit(
    val textPrefix: String,
    val binaries: List<Attachment>,
)

@OptIn(ExperimentalEncodingApi::class)
object AttachmentProcessor {

    private const val MAX_TEXT_FILE_BYTES = 100_000 // 100 KB
    private const val MAX_PDF_BYTES = 5_000_000 // 5 MB
    private const val MAX_RAW_IMAGE_BYTES = 10_000_000 // 10 MB

    fun classifyFile(mimeType: String?, fileName: String): FileCategory {
        val mt = mimeType?.lowercase() ?: ""
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return when {
            mt.startsWith("image/") || ext in setOf("png", "jpg", "jpeg", "webp", "gif") -> FileCategory.IMAGE
            mt.startsWith("text/") || ext in setOf("txt", "md", "json", "xml", "csv", "kt", "java", "py", "sh") -> FileCategory.TEXT
            mt == "application/pdf" || ext == "pdf" -> FileCategory.PDF
            else -> FileCategory.UNSUPPORTED
        }
    }

    fun isTextMimeType(mimeType: String): Boolean {
        val mt = mimeType.lowercase()
        return mt.startsWith("text/") || mt in setOf(
            "application/json",
            "application/xml",
            "application/javascript",
            "application/x-yaml",
            "application/yaml",
        )
    }

    /**
     * Splits attachments into text (appended to user prompt with filename header)
     * and binary attachments (images, PDFs) which are processed natively.
     */
    fun splitForMessage(attachments: List<Attachment>): AttachmentSplit {
        if (attachments.isEmpty()) return AttachmentSplit("", emptyList())
        val prefix = StringBuilder()
        val binaries = mutableListOf<Attachment>()
        for (att in attachments) {
            if (isTextMimeType(att.mimeType)) {
                val decoded = Base64.decode(att.data).decodeToString()
                if (att.fileName != null) {
                    prefix.append("--- ${att.fileName} ---\n")
                }
                prefix.append(decoded).append("\n\n")
            } else {
                binaries.add(att)
            }
        }
        return AttachmentSplit(prefix.toString(), binaries)
    }
}
