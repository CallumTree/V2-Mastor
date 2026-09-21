package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ========================================================================
// MASTOR DESIGN SYSTEM v3 FOUNDATION TOKENS
// ========================================================================

// ------------------------------------------------------------------------
// Primary Palette
// ------------------------------------------------------------------------
val MastorCopper = Color(0xFFC97B3F)        // Primary accent — CTAs, active states, money figures
val MastorCopperLight = Color(0xFFE8A868)   // Hover/pressed states on copper
val MastorCopperDark = Color(0xFFA85E28)    // Deep copper for text on light backgrounds
val MastorCopperSubtle = Color(0xFFF5E6D3)  // Copper tint background for highlighted cards

// ------------------------------------------------------------------------
// Structural Surfaces
// ------------------------------------------------------------------------
val MastorCharcoal = Color(0xFF1A1A2E)      // Primary dark surface — nav bar, headers, dark cards
val MastorCharcoalMid = Color(0xFF252540)   // Secondary dark surface — elevated dark cards
val MastorCharcoalLight = Color(0xFF2E2E4A) // Tertiary — borders on dark surfaces

// ------------------------------------------------------------------------
// Content Backgrounds
// ------------------------------------------------------------------------
val MastorCream = Color(0xFFF5F0E8)         // Primary content background — replaces white
val MastorCreamDark = Color(0xFFEDE6D8)     // Cards on cream background
val MastorCreamBorder = Color(0xFFD4C9B5)   // Borders on cream surfaces

// ------------------------------------------------------------------------
// Text Tokens
// ------------------------------------------------------------------------
val MastorInk = Color(0xFF1A1A2E)           // Primary text on light backgrounds
val MastorInkMuted = Color(0xFF5A5A7A)      // Secondary/label text
val MastorInkSubtle = Color(0xFF8A8AAA)     // Placeholder, disabled text
val MastorCreamText = Color(0xFFF5F0E8)     // Primary text on dark surfaces
val MastorCreamMuted = Color(0xFFB0A898)    // Secondary text on dark surfaces

// ------------------------------------------------------------------------
// Teal — strictly for AI/live features only
// ------------------------------------------------------------------------
val MastorTeal = Color(0xFF2DD4BF)          // AI transcription active, AI match results
val MastorTealSubtle = Color(0xFFE0FAF7)    // Teal tint background

// ------------------------------------------------------------------------
// Status Colours
// ------------------------------------------------------------------------
val StatusGreen = Color(0xFF22C55E)         // Claimed, complete, paid
val StatusGreenBg = Color(0xFFF0FDF4)
val StatusAmber = Color(0xFFF59E0B)         // In progress, pending, draft
val StatusAmberBg = Color(0xFFFFFBEB)
val StatusRed = Color(0xFFEF4444)           // Rejected, error, overdue
val StatusRedBg = Color(0xFFFEF2F2)
val StatusSlate = Color(0xFF64748B)         // Not started, inactive
val StatusSlateBg = Color(0xFFF8FAFC)

// ------------------------------------------------------------------------
// Compatibility & Semantic Aliases (Preserves existing codebase components)
// ------------------------------------------------------------------------
val MastorSlateDark = MastorCharcoal
val MastorSlateText = MastorInk
val MastorSlateMuted = MastorInkMuted
val MastorSlateBorder = MastorCreamBorder

val MastorAccentBlue = MastorCopper
val MastorAccentBlueLight = MastorCopperSubtle

val MastorBackgroundLight = MastorCream
val MastorSurfaceLight = MastorCreamDark
val MastorSurfaceVariant = MastorCream

val StatusClaimedGreen = StatusGreen
val StatusClaimedBg = StatusGreenBg

val StatusPendingAmber = StatusAmber
val StatusPendingBg = StatusAmberBg

val StatusFlaggedRed = StatusRed
val StatusFlaggedBg = StatusRedBg

val StatusIdentifiedSky = MastorTeal
val StatusIdentifiedBg = MastorTealSubtle

val MastorGold = MastorCopper
val MastorGoldBg = MastorCopperSubtle
