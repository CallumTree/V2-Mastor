package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

// ========================================================================
// MASTOR DESIGN SYSTEM v3 TYPOGRAPHY
// ========================================================================

// Typefaces: Inter for primary & JetBrains Mono for figures & technical codes.
// Real weight files (Latin-subset) — previously a single Regular file each, so every
// Medium/SemiBold/Bold in the app was Android faking bold by smearing regular glyphs.
val InterFontFamily = FontFamily(
    Font(R.font.inter_light, FontWeight.Light),
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semi_bold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
    Font(R.font.inter_extra_bold, FontWeight.ExtraBold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono_light, FontWeight.Light),
    Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
    Font(R.font.jetbrains_mono_medium, FontWeight.Medium),
    Font(R.font.jetbrains_mono_semi_bold, FontWeight.SemiBold),
    Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
)

// ------------------------------------------------------------------------
// Primary Typography Tokens
// ------------------------------------------------------------------------

// Inter 48sp ExtraBold, tracking -2sp — hero project name
val MastorDisplayLarge = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.ExtraBold,
    fontSize = 48.sp,
    lineHeight = 54.sp,
    letterSpacing = (-2).sp,
    color = MastorInk
)

// Inter 34sp Bold, tracking -1sp — screen titles
val MastorDisplayMedium = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 34.sp,
    lineHeight = 40.sp,
    letterSpacing = (-1).sp,
    color = MastorInk
)

// Inter 22sp Bold, tracking 0 — section headers
val MastorHeadline = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
    color = MastorInk
)

// Inter 17sp SemiBold, tracking 0 — card titles
val MastorTitle = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    lineHeight = 22.sp,
    letterSpacing = 0.sp,
    color = MastorInk
)

// Inter 16sp Regular — primary body text
val MastorBodyLarge = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.sp,
    color = MastorInk
)

// Inter 14sp Regular — standard body
val MastorBody = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.sp,
    color = MastorInk
)

// Inter 11sp SemiBold, tracking 1.5sp, UPPERCASE — section labels
val MastorLabel = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 1.5.sp,
    color = MastorInkMuted
)

// Inter 11sp Medium, tracking 2sp, UPPERCASE — used as [ LABEL ] style
val MastorBracketLabel = TextStyle(
    fontFamily = InterFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 15.sp,
    letterSpacing = 2.sp,
    color = MastorInkMuted
)

// JetBrains Mono 32sp Bold — primary money figures
val MastorFinancialLarge = TextStyle(
    fontFamily = JetBrainsMonoFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 38.sp,
    letterSpacing = (-0.5).sp,
    color = MastorCopper
)

// JetBrains Mono 22sp SemiBold — secondary money figures
val MastorFinancialMed = TextStyle(
    fontFamily = JetBrainsMonoFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = (-0.2).sp,
    color = MastorCopper
)

// JetBrains Mono 14sp Medium — table amounts, rates
val MastorFinancialSmall = TextStyle(
    fontFamily = JetBrainsMonoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 18.sp,
    letterSpacing = 0.sp,
    color = MastorInk
)

// JetBrains Mono 12sp Regular — BoQ codes, refs
val MastorCode = TextStyle(
    fontFamily = JetBrainsMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
    color = MastorInkMuted
)

// ------------------------------------------------------------------------
// Material 3 Typography Mapping
// ------------------------------------------------------------------------
val MastorTypography = Typography(
    displayLarge = MastorDisplayLarge,
    displayMedium = MastorDisplayMedium,
    headlineLarge = MastorDisplayMedium,
    headlineMedium = MastorHeadline,
    headlineSmall = MastorHeadline.copy(fontSize = 19.sp, lineHeight = 24.sp),
    titleLarge = MastorHeadline,
    titleMedium = MastorTitle,
    titleSmall = MastorTitle.copy(fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = MastorBodyLarge,
    bodyMedium = MastorBody,
    bodySmall = MastorBody.copy(fontSize = 12.sp, lineHeight = 16.sp, color = MastorInkMuted),
    labelLarge = MastorBody.copy(fontWeight = FontWeight.SemiBold),
    labelMedium = MastorLabel,
    labelSmall = MastorBracketLabel
)

// Backward compatibility aliases
val FinancialLargeNumeralStyle = MastorFinancialLarge
val FinancialMediumNumeralStyle = MastorFinancialMed
