package com.joao.gastos.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.joao.gastos.R

val Sora = FontFamily(
    Font(R.font.sora_regular, FontWeight.Normal),
    Font(R.font.sora_semibold, FontWeight.SemiBold),
)

private fun sora(size: Int, weight: FontWeight, lineHeight: Int, tabular: Boolean = false) = TextStyle(
    fontFamily = Sora,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontFeatureSettings = if (tabular) "tnum" else null,
)

val Typography = Typography(
    displayLarge = sora(32, FontWeight.Normal, 38, tabular = true),
    displayMedium = sora(32, FontWeight.Normal, 38, tabular = true),
    displaySmall = sora(32, FontWeight.Normal, 38, tabular = true),
    headlineLarge = sora(32, FontWeight.SemiBold, 38),
    headlineMedium = sora(28, FontWeight.SemiBold, 34),
    headlineSmall = sora(28, FontWeight.SemiBold, 34),
    titleLarge = sora(28, FontWeight.SemiBold, 34),
    titleMedium = sora(16, FontWeight.SemiBold, 22),
    titleSmall = sora(14, FontWeight.SemiBold, 18),
    bodyLarge = sora(16, FontWeight.Normal, 24),
    bodyMedium = sora(16, FontWeight.Normal, 24),
    bodySmall = sora(14, FontWeight.Normal, 21),
    labelLarge = sora(16, FontWeight.SemiBold, 21),
    labelMedium = sora(14, FontWeight.SemiBold, 18),
    labelSmall = sora(14, FontWeight.SemiBold, 18),
)
