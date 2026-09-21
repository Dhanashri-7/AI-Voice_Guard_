package com.voiceguard.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================================
// PROFESSIONAL PUBLIC-SERVICE & DIGITAL SECURITY COLOR SYSTEM
// ========================================================

// Primary Brand Colors (Deep Navy & Government-Style Blue)
val GovNavyPrimary = Color(0xFF0F2546)        // Deep Navy Blue (Primary Branding & Headers)
val GovNavyDark = Color(0xFF0A192F)           // Dark Navy (Key accents)
val GovBlueAccent = Color(0xFF1E3A8A)         // Professional Government Blue
val GovBlueLight = Color(0xFFEFF6FF)          // Very light subtle blue tint

// Surface & Background Colors (Clean Light Theme)
val AppBackground = Color(0xFFF8FAFC)         // Very Light Grey Paper Background
val CardBackground = Color(0xFFFFFFFF)        // Pure White Card Container
val CardBorder = Color(0xFFE2E8F0)            // Subtle Slate Border
val CardBorderHover = Color(0xFFCBD5E1)       // Focused / Active Border
val DividerColor = Color(0xFFF1F5F9)          // Subtle Horizontal Divider

// Status Colors (Professional, Calm, Non-Neon)
val StatusSafe = Color(0xFF16A34A)            // Professional Green (Safe)
val StatusSafeBg = Color(0xFFF0FDF4)          // Soft Green Pill Background
val StatusSafeBorder = Color(0xFFBBF7D0)      // Subtle Green Border

val StatusWarning = Color(0xFFD97706)         // Professional Amber/Orange (Suspicious)
val StatusWarningBg = Color(0xFFFFFBEB)       // Soft Amber Pill Background
val StatusWarningBorder = Color(0xFFFDE68A)   // Subtle Amber Border

val StatusThreat = Color(0xFFDC2626)          // Professional Red (High Risk / Fraud)
val StatusThreatBg = Color(0xFFFEF2F2)        // Soft Red Pill Background
val StatusThreatBorder = Color(0xFFFECACA)    // Subtle Red Border

// Typography Colors
val TextCharcoal = Color(0xFF0F172A)          // Dark Navy / Charcoal (Headings & Primary Text)
val TextSlate = Color(0xFF334155)             // Slate (Body & Secondary Headings)
val TextMutedGrey = Color(0xFF64748B)         // Medium Grey (Captions & Subtitles)
val TextSubtle = Color(0xFF94A3B8)            // Light Subtle Grey (Placeholders & Footers)

// ========================================================
// COMPATIBILITY ALIASES (Ensures zero compilation breaks across legacy files)
// ========================================================
val CyberDarkBackground = AppBackground
val CyberDarkSurface = CardBackground
val CyberCardBorder = CardBorder

val SafeEmerald = StatusSafe
val SafeEmeraldDark = Color(0xFF15803D)

val CautionAmber = StatusWarning
val CautionAmberDark = Color(0xFFB45309)

val ThreatCrimson = StatusThreat
val ThreatCrimsonDark = Color(0xFFB91C1C)

val NeuralCyan = GovBlueAccent
val NeuralPurple = GovNavyPrimary

val TextPrimary = TextCharcoal
val TextSecondary = TextMutedGrey
val TextMuted = TextSubtle

val GovTextPrimary = TextCharcoal
val GovTextSecondary = TextSlate
val GovTextMuted = TextMutedGrey

val GlassAccent = GovBlueLight
val GlassCrimsonAccent = StatusThreatBg
