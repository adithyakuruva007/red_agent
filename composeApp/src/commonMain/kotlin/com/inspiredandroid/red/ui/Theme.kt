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
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
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
import com.inspiredandroid.red.data.ThemeMode

data class RedColors(
    val bgDeep: Color,
    val bgPanel: Color,
    val bgElevated: Color,
    val bgElevated2: Color,
    val borderHairline: Color,
    val accent: Color,
    val accentSoft: Color,
    val agentViolet: Color,
    val agentVioletSoft: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val online: Color,
    val danger: Color,
)

val DarkRedColors = RedColors(
    bgDeep = Color(0xFF0E1420),
    bgPanel = Color(0xFF151C29),
    bgElevated = Color(0xFF1D2635),
    bgElevated2 = Color(0xFF242F41),
    borderHairline = Color(0x80263144),
    accent = Color(0xFF5B8DEF),
    accentSoft = Color(0x265B8DEF),
    agentViolet = Color(0xFF8B6CF2),
    agentVioletSoft = Color(0x248B6CF2),
    textPrimary = Color(0xFFEAEFF5),
    textSecondary = Color(0xFF98A4B8),
    textTertiary = Color(0xFF5E6B80),
    online = Color(0xFF3ED598),
    danger = Color(0xFFF26D6D)
)

val LightRedColors = RedColors(
    bgDeep = Color(0xFFF4F6FB),
    bgPanel = Color(0xFFFFFFFF),
    bgElevated = Color(0xFFEBF0F7),
    bgElevated2 = Color(0xFFDFE6F0),
    borderHairline = Color(0x1F1A2435),
    accent = Color(0xFF2A6AE3),
    accentSoft = Color(0x122A6AE3),
    agentViolet = Color(0xFF7551EC),
    agentVioletSoft = Color(0x107551EC),
    textPrimary = Color(0xFF1E293B),
    textSecondary = Color(0xFF64748B),
    textTertiary = Color(0xFF94A3B8),
    online = Color(0xFF1FA876),
    danger = Color(0xFFD32F2F)
)

val LocalRedColors = staticCompositionLocalOf { DarkRedColors }

val RedBgDeep: Color @Composable get() = LocalRedColors.current.bgDeep
val RedBgPanel: Color @Composable get() = LocalRedColors.current.bgPanel
val RedBgElevated: Color @Composable get() = LocalRedColors.current.bgElevated
val RedBgElevated2: Color @Composable get() = LocalRedColors.current.bgElevated2
val RedBorderHairline: Color @Composable get() = LocalRedColors.current.borderHairline
val RedAccent: Color @Composable get() = LocalRedColors.current.accent
val RedAccentSoft: Color @Composable get() = LocalRedColors.current.accentSoft
val RedAgentViolet: Color @Composable get() = LocalRedColors.current.agentViolet
val RedAgentVioletSoft: Color @Composable get() = LocalRedColors.current.agentVioletSoft
val RedTextPrimary: Color @Composable get() = LocalRedColors.current.textPrimary
val RedTextSecondary: Color @Composable get() = LocalRedColors.current.textSecondary
val RedTextTertiary: Color @Composable get() = LocalRedColors.current.textTertiary
val RedOnline: Color @Composable get() = LocalRedColors.current.online
val RedDanger: Color @Composable get() = LocalRedColors.current.danger

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

fun getRedColorScheme(colors: RedColors, isDark: Boolean): ColorScheme {
    return if (isDark) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = Color.White,
            primaryContainer = colors.accentSoft,
            onPrimaryContainer = colors.accent,
            secondary = colors.agentViolet,
            onSecondary = Color.White,
            secondaryContainer = colors.agentVioletSoft,
            onSecondaryContainer = colors.agentViolet,
            surface = colors.bgPanel,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.bgElevated,
            onSurfaceVariant = colors.textSecondary,
            background = colors.bgDeep,
            onBackground = colors.textPrimary,
            outline = colors.borderHairline,
            outlineVariant = colors.borderHairline,
            tertiary = colors.online,
            onTertiary = Color.White,
            error = colors.danger,
            onError = Color.White,
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = Color.White,
            primaryContainer = colors.accentSoft,
            onPrimaryContainer = colors.accent,
            secondary = colors.agentViolet,
            onSecondary = Color.White,
            secondaryContainer = colors.agentVioletSoft,
            onSecondaryContainer = colors.agentViolet,
            surface = colors.bgPanel,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.bgElevated,
            onSurfaceVariant = colors.textSecondary,
            background = colors.bgDeep,
            onBackground = colors.textPrimary,
            outline = colors.borderHairline,
            outlineVariant = colors.borderHairline,
            tertiary = colors.online,
            onTertiary = Color.White,
            error = colors.danger,
            onError = Color.White,
        )
    }
}

val RedColorScheme: ColorScheme @Composable get() = getRedColorScheme(LocalRedColors.current, LocalRedColors.current != LightRedColors)

val ColorScheme.isLightFlavor: Boolean @Composable get() = LocalRedColors.current == LightRedColors

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
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RedAccent,
            unfocusedBorderColor = RedBorderHairline,
            focusedLabelColor = RedAccent,
            unfocusedLabelColor = RedTextSecondary,
            focusedTextColor = RedTextPrimary,
            unfocusedTextColor = RedTextPrimary,
        ),
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
fun outlineTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RedAccent,
    unfocusedBorderColor = RedBorderHairline,
    focusedLabelColor = RedAccent,
    unfocusedLabelColor = RedTextSecondary,
    focusedTextColor = RedTextPrimary,
    unfocusedTextColor = RedTextPrimary,
)

@Composable
fun redSwitchColors() = androidx.compose.material3.SwitchDefaults.colors(
    checkedThumbColor = Color.White,
    checkedTrackColor = RedAccent,
    checkedBorderColor = RedAccent,
    uncheckedThumbColor = RedTextSecondary,
    uncheckedTrackColor = RedBorderHairline.copy(alpha = 0.15f),
    uncheckedBorderColor = RedBorderHairline,
)

@Composable
fun Theme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.OledBlack -> true
        ThemeMode.System -> androidx.compose.foundation.isSystemInDarkTheme()
    }
    
    val colors = if (isDark) {
        if (themeMode == ThemeMode.OledBlack) {
            DarkRedColors.copy(bgDeep = Color.Black, bgPanel = Color.Black)
        } else {
            DarkRedColors
        }
    } else {
        LightRedColors
    }
    
    val colorScheme = getRedColorScheme(colors, isDark)

    androidx.compose.runtime.CompositionLocalProvider(
        LocalRedColors provides colors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = RedTypography,
        ) {
            content()
        }
    }
}

val DarkAdwaitaBlackColorScheme: ColorScheme @Composable get() = RedColorScheme
val DarkAdwaitaBlackLightBlueColorScheme: ColorScheme @Composable get() = RedColorScheme
val LightAdwaitaColorScheme: ColorScheme @Composable get() = RedColorScheme
val DarkClaymorphismColorScheme: ColorScheme @Composable get() = RedColorScheme
