package com.inspiredandroid.red.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val RedBgDeep = Color(0xFF0E1420)
val RedBgPanel = Color(0xFF151C29)
val RedBgElevated = Color(0xFF1D2635)
val RedBgElevated2 = Color(0xFF242F41)
val RedBorderHairline = Color(0x80263144)
val RedAccent = Color(0xFF5B8DEF)
val RedAccentSoft = Color(0x265B8DEF)
val RedAgentViolet = Color(0xFF8B6CF2)
val RedAgentVioletSoft = Color(0x248B6CF2)
val RedTextPrimary = Color(0xFFEAEFF5)
val RedTextSecondary = Color(0xFF98A4B8)
val RedTextTertiary = Color(0xFF5E6B80)
val RedOnline = Color(0xFF3ED598)
val RedDanger = Color(0xFFF26D6D)

val RedGrad1Start = Color(0xFF5B8DEF)
val RedGrad1End = Color(0xFF3E5FCB)
val RedGrad2Start = Color(0xFF8B6CF2)
val RedGrad2End = Color(0xFF5E3FCB)
val RedGrad3Start = Color(0xFF3ED598)
val RedGrad3End = Color(0xFF1FA876)
val RedGrad4Start = Color(0xFFF2A65B)
val RedGrad4End = Color(0xFFCB7A3E)
val RedGrad5Start = Color(0xFFF26D9B)
val RedGrad5End = Color(0xFFCB3E6F)

val RedOnlineShadow = Color(0xFF3ED598)
val RedAccentShadow = Color(0x805B8DEF)

// TODO: Replace with custom fonts (Space Grotesk, Inter, JetBrains Mono) once
// Compose Resources font API is resolved for this project version.
internal val DisplayFont = FontFamily.SansSerif
internal val BodyFont = FontFamily.SansSerif
internal val MonoFont = FontFamily.Monospace

val RedTypography = Typography(
    displayLarge = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, letterSpacing = (-0.02).sp),
    displayMedium = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, letterSpacing = (-0.01).sp),
    displaySmall = TextStyle(fontFamily = DisplayFont, fontWeight = FontWeight.Medium, fontSize = 18.sp),
    headlineLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, letterSpacing = (-0.01).sp),
    headlineMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 15.5.sp, letterSpacing = (-0.01).sp),
    headlineSmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    titleLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, letterSpacing = (-0.01).sp),
    titleMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 14.5.sp),
    titleSmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 13.sp),
    bodyLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 15.5.sp),
    bodyMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 14.5.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Normal, fontSize = 13.sp),
    labelLarge = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.SemiBold, fontSize = 12.sp),
    labelMedium = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 11.sp),
    labelSmall = TextStyle(fontFamily = BodyFont, fontWeight = FontWeight.Medium, fontSize = 10.sp),
)

val RedColorScheme = darkColorScheme(
    primary = RedAccent,
    onPrimary = Color.White,
    primaryContainer = RedAccentSoft,
    onPrimaryContainer = RedAccent,
    secondary = RedAgentViolet,
    onSecondary = Color.White,
    secondaryContainer = RedAgentVioletSoft,
    onSecondaryContainer = RedAgentViolet,
    surface = RedBgPanel,
    onSurface = RedTextPrimary,
    surfaceVariant = RedBgElevated,
    onSurfaceVariant = RedTextSecondary,
    background = RedBgDeep,
    onBackground = RedTextPrimary,
    outline = RedBorderHairline,
    outlineVariant = RedBorderHairline,
    tertiary = RedOnline,
    onTertiary = Color.White,
    error = RedDanger,
    onError = Color.White,
)

@Composable
fun Modifier.handCursor() = pointerHoverIcon(PointerIcon.Hand, overrideDescendants = true)

fun ColorScheme.withBlackBackground(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
)

val ColorScheme.isOledFlavor: Boolean get() = background == Color.Black
val ColorScheme.isClaymorphism: Boolean get() = background == Color(0xFF1A1A24)

@Composable
fun redAdaptiveCardColors() = CardDefaults.cardColors(
    containerColor = RedBgPanel,
)

@Composable
fun redAdaptiveCardBorder() = BorderStroke(1.dp, RedBorderHairline)

@Composable
fun Modifier.redAdaptiveCardSurface(shape: Shape = CardDefaults.shape): Modifier {
    val finalShape = RoundedCornerShape(12.dp)
    return this
        .clip(finalShape)
        .background(RedBgPanel)
        .border(BorderStroke(1.dp, RedBorderHairline), finalShape)
}

@Composable
fun Modifier.redInputSurface(isFocused: Boolean, shape: Shape = RoundedCornerShape(12.dp)): Modifier {
    return this
        .clip(shape)
        .background(RedBgPanel)
        .border(BorderStroke(if (isFocused) 1.dp else 0.dp, if (isFocused) RedAccent else RedBorderHairline), shape)
}

@Composable
fun Modifier.redIconButtonSurface(shape: Shape = RoundedCornerShape(8.dp)): Modifier {
    return this
        .clip(shape)
        .background(RedBgElevated)
}

@Composable
fun RedOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        readOnly = readOnly,
        label = label,
        placeholder = placeholder,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(),
    )
}

@Composable
fun RedClearableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    RedOutlinedTextField(
        modifier = modifier.fillMaxWidth().onFocusChanged { focused = it.isFocused },
        value = value,
        onValueChange = onValueChange,
        label = label,
        singleLine = singleLine,
        trailingIcon = {
            IconButton(
                onClick = { onValueChange("") },
                modifier = Modifier.handCursor()
                    .alpha(if (focused && value.isNotEmpty()) 1f else 0f),
                enabled = value.isNotEmpty(),
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
fun rememberGradientBrush(): Brush {
    return Brush.horizontalGradient(listOf(RedAccent, RedGrad1End))
}

@Composable
fun outlineTextFieldColors() = OutlinedTextFieldDefaults.colors()

@Composable
fun Theme(
    colorScheme: ColorScheme = RedColorScheme,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = RedTypography,
    ) {
        content()
    }
}

// Backward-compat aliases for App.kt (will remove when App.kt is rewritten in Phase 2)
val DarkAdwaitaBlackColorScheme: ColorScheme get() = RedColorScheme
val DarkAdwaitaBlackLightBlueColorScheme: ColorScheme get() = RedColorScheme
val LightAdwaitaColorScheme: ColorScheme get() = RedColorScheme
val DarkClaymorphismColorScheme: ColorScheme get() = RedColorScheme
