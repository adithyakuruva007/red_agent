# Appearance

**Last verified:** 2026-07-19

Red uses a single dark color scheme (`RedColorScheme`) that applies across all platforms. The palette is inspired by the original reference design and uses a deep navy background (`#0E1420`) with panel, elevated, and accent colors. No light theme is currently offered; theme switching is deferred until dark/light support is needed.

All new surfaces should use the color constants defined in `Theme.kt` rather than `MaterialTheme.colorScheme.*` directly, to maintain consistency if the palette is later toggled.

## Color Tokens

| Token | Value | Usage |
|-------|-------|-------|
| `RedBgDeep` | `#0E1420` | Page background |
| `RedBgPanel` | `#151C29` | Cards, panels |
| `RedBgElevated` | `#1D2635` | Elevated surfaces, active rows |
| `RedAccent` | `#5B8DEF` | Primary accent, active states |
| `RedAgentViolet` | `#8B6CF2` | Secondary accent |
| `RedOnline` | `#3ED598` | Online/status indicators |
| `RedDanger` | `#F26D6D` | Destructive actions, badges |
| `RedTextPrimary` | `#EAEFF5` | Primary text |
| `RedTextSecondary` | `#98A4B8` | Secondary/muted text |
| `RedTextTertiary` | `#5E6B80` | Tertiary/disabled text |

## Key Files

| File | Purpose |
|------|---------|
| `composeApp/.../ui/Theme.kt` | `RedColorScheme`, all color constants, `RedTypography`, backward-compat aliases for old scheme names |
| `composeApp/.../App.kt` | Applies `RedColorScheme` as the single scheme via `Theme()` |
| `composeApp/.../ui/navigation/AppShell.kt` | Mobile shell that uses `RedBgDeep` for the root background |
| `composeApp/.../ui/navigation/BottomNavBar.kt` | Bottom bar with `RedBgPanel`, `RedAccent` active indicator |
| `composeApp/.../desktopMain/.../main.kt` | Desktop entry — uses common `App` defaults; also configures HiDPI hints and an initial 1280×800 `WindowState` so the window opens at a usable size on Linux/Wayland |
