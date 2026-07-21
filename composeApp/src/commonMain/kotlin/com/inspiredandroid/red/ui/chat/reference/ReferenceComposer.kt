package com.inspiredandroid.red.ui.chat.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.inspiredandroid.red.data.ServiceEntry
import com.inspiredandroid.red.rememberPlatformCameraLauncher
import com.inspiredandroid.red.skills.SkillManifest
import com.inspiredandroid.red.ui.RedAccent
import com.inspiredandroid.red.ui.RedBgDeep
import com.inspiredandroid.red.ui.RedBgElevated
import com.inspiredandroid.red.ui.RedBgPanel
import com.inspiredandroid.red.ui.RedBorderHairline
import com.inspiredandroid.red.ui.RedTextPrimary
import com.inspiredandroid.red.ui.RedTextSecondary
import com.inspiredandroid.red.ui.RedTextTertiary
import com.inspiredandroid.red.ui.chat.composables.ServiceSelector
import com.inspiredandroid.red.ui.chat.composables.SkillAutocomplete
import com.inspiredandroid.red.ui.chat.composables.detectSlashQuery
import com.inspiredandroid.red.ui.handCursor
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

private fun getDefaultSkillsList(): List<SkillManifest> = listOf(
    SkillManifest(id = "help", displayName = "Help", description = "Get help & list available skills and tools", body = ""),
    SkillManifest(id = "summarize", displayName = "Summarize", description = "Summarize text, links, or documents", body = ""),
    SkillManifest(id = "code", displayName = "Code Assistant", description = "Write, refactor, or debug code", body = ""),
    SkillManifest(id = "search", displayName = "Web Search", description = "Search the web for real-time information", body = ""),
    SkillManifest(id = "image", displayName = "Image Generation", description = "Generate or analyze images", body = ""),
    SkillManifest(id = "clear", displayName = "Clear Chat", description = "Clear current conversation history", body = ""),
    SkillManifest(id = "web", displayName = "Web Fetch", description = "Fetch and extract text from web URLs", body = ""),
    SkillManifest(id = "context", displayName = "Context Manager", description = "View or add context files", body = ""),
)

@Composable
fun ReferenceComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit = {},
    isLoading: Boolean,
    files: ImmutableList<PlatformFile> = persistentListOf(),
    addFile: (PlatformFile) -> Unit = {},
    removeFile: (PlatformFile) -> Unit = {},
    supportedFileExtensions: ImmutableList<String> = persistentListOf(),
    installedSkills: ImmutableList<SkillManifest> = persistentListOf(),
    availableServices: ImmutableList<ServiceEntry> = persistentListOf(),
    onSelectService: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showAttachMenu by remember { mutableStateOf(false) }

    val handleSend = {
        if ((text.trim().isNotBlank() || files.isNotEmpty()) && !isLoading) {
            onSend()
        }
    }

    val allowFileAttachment = supportedFileExtensions.isNotEmpty()
    val filePickerLauncher = rememberFilePickerLauncher(
        type = if (allowFileAttachment) FileKitType.File(extensions = supportedFileExtensions.toList()) else FileKitType.File(),
    ) { file ->
        if (file != null) addFile(file)
    }

    val photoPickerLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
    ) { file ->
        if (file != null) addFile(file)
    }

    val cameraPickerLauncher = rememberPlatformCameraLauncher { file ->
        if (file != null) addFile(file)
    }

    val slashQuery = remember(text) {
        detectSlashQuery(text, text.length)
    }

    val allSkills = remember(installedSkills) {
        val defaults = getDefaultSkillsList()
        val merged = (installedSkills + defaults).distinctBy { it.id }
        merged.toImmutableList()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(RedBgDeep)
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        // Slash Autocomplete Menu for Tools and Skills
        if (slashQuery != null) {
            SkillAutocomplete(
                skills = allSkills,
                query = slashQuery,
                onSelect = { skill ->
                    val firstSpace = text.indexOfFirst { it.isWhitespace() }
                    val rest = if (firstSpace < 0) "" else text.substring(firstSpace)
                    val newText = "/${skill.id} $rest"
                    onTextChange(newText)
                },
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        // Render Attached File Chips if files present
        if (files.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(files) { file ->
                    AttachedFileChip(
                        file = file,
                        onRemove = { removeFile(file) },
                    )
                }
            }
        }

        // Soft Rectangular Container
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(RedBgPanel)
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            RedAccent,
                            Color(0xFF3E5FCB),
                        ),
                    ),
                    shape = RoundedCornerShape(16.dp),
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Plus Button (+) with Attachment Options Menu
            Box {
                IconButton(
                    onClick = { showAttachMenu = true },
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .handCursor(),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add attachment",
                        tint = RedTextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }

                DropdownMenu(
                    expanded = showAttachMenu,
                    onDismissRequest = { showAttachMenu = false },
                    shape = RoundedCornerShape(12.dp),
                ) {
                    DropdownMenuItem(
                        text = { Text("Files", color = RedTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.InsertDriveFile,
                                contentDescription = "Files",
                                tint = RedTextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = {
                            showAttachMenu = false
                            filePickerLauncher.launch()
                        },
                        modifier = Modifier.handCursor(),
                    )
                    DropdownMenuItem(
                        text = { Text("Camera", color = RedTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Camera",
                                tint = RedTextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = {
                            showAttachMenu = false
                            cameraPickerLauncher.launch()
                        },
                        modifier = Modifier.handCursor(),
                    )
                    DropdownMenuItem(
                        text = { Text("Photos", color = RedTextPrimary) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photos",
                                tint = RedTextSecondary,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        onClick = {
                            showAttachMenu = false
                            photoPickerLauncher.launch()
                        },
                        modifier = Modifier.handCursor(),
                    )
                }
            }

            // Model / Service Selector Dropdown
            if (availableServices.isNotEmpty()) {
                ServiceSelector(
                    services = availableServices,
                    onSelectService = onSelectService,
                )
            }

            // Text Input Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 4.dp),
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = "Message Agent…",
                        color = RedTextTertiary,
                        fontSize = 14.5.sp,
                    )
                }
                BasicTextField(
                    value = text,
                    onValueChange = onTextChange,
                    textStyle = TextStyle(
                        color = RedTextPrimary,
                        fontSize = 14.5.sp,
                    ),
                    cursorBrush = SolidColor(RedAccent),
                    singleLine = false,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Send,
                    ),
                    keyboardActions = KeyboardActions(
                        onSend = { handleSend() },
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onPreviewKeyEvent { event ->
                            if (event.key.keyCode == Key.Enter.keyCode && event.type == KeyEventType.KeyDown) {
                                if (event.isShiftPressed) {
                                    false
                                } else {
                                    handleSend()
                                    true
                                }
                            } else {
                                false
                            }
                        },
                )
            }

            // Action Button (Send / Square Cancel Stop)
            val canSend = isLoading || text.trim().isNotBlank() || files.isNotEmpty()
            IconButton(
                onClick = {
                    if (isLoading) {
                        onCancel()
                    } else {
                        handleSend()
                    }
                },
                enabled = canSend,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (canSend) {
                            Brush.linearGradient(listOf(RedAccent, Color(0xFF3E5FCB)))
                        } else {
                            SolidColor(Color.White.copy(alpha = 0.08f))
                        },
                    )
                    .handCursor(),
            ) {
                Icon(
                    imageVector = if (isLoading) Icons.Default.Stop else Icons.AutoMirrored.Filled.Send,
                    contentDescription = if (isLoading) "Cancel" else "Send",
                    tint = if (canSend) Color.White else RedTextTertiary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun AttachedFileChip(
    file: PlatformFile,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(RedBgElevated)
            .border(1.dp, RedBorderHairline, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.InsertDriveFile,
            contentDescription = null,
            tint = RedAccent,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = file.name,
            color = RedTextPrimary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 120.dp),
        )
        Spacer(Modifier.width(4.dp))
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(18.dp).handCursor(),
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = RedTextTertiary,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}
