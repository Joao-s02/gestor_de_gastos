package com.joao.gastos.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.joao.gastos.model.MovementType

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = Pauta,
    surfaceTint = Bosque,
    inverseSurface = Bosque,
    inverseOnSurface = Marfil,
    inversePrimary = Salvia,
    surfaceContainerLowest = Marfil,
    surfaceContainerLow = Color(0xFFEEF2E8),
    surfaceContainer = Color(0xFFE7EEE4),
    surfaceContainerHigh = Color(0xFFE1E9DC),
    surfaceContainerHighest = Color(0xFFDBE4D4),
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark,
    outlineVariant = PautaOscura,
    surfaceTint = Bosque,
    inverseSurface = Marfil,
    inverseOnSurface = Bosque,
    inversePrimary = Salvia,
    surfaceContainerLowest = Color(0xFF102E28),
    surfaceContainerLow = Color(0xFF18463E),
    surfaceContainer = SuperficieOscura,
    surfaceContainerHigh = Color(0xFF245E54),
    surfaceContainerHighest = Color(0xFF2A685C),
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(24.dp),
)

@Immutable
data class YescaColors(
    val income: Color,
    val expense: Color,
    val balanceNeutral: Color,
    val balanceCard: Color,
    val onBalanceCard: Color,
)

private val LightYesca = YescaColors(
    income = IncomeLight,
    expense = ExpenseLight,
    balanceNeutral = BalanceNeutralLight,
    balanceCard = Salvia,
    onBalanceCard = Bosque,
)

private val DarkYesca = YescaColors(
    income = IncomeDark,
    expense = ExpenseDark,
    balanceNeutral = BalanceNeutralDark,
    balanceCard = SurfaceVariantDark,
    onBalanceCard = Marfil,
)

val LocalYescaColors = staticCompositionLocalOf { LightYesca }

@Composable
fun GastosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // La paleta institucional no sigue el wallpaper. Dynamic color rompería Yesca.
    val yesca = if (darkTheme) DarkYesca else LightYesca
    CompositionLocalProvider(LocalYescaColors provides yesca) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = AppShapes,
            content = content,
        )
    }
}

@Composable
fun moneyColor(type: MovementType): Color {
    val colors = LocalYescaColors.current
    return when (type) {
        MovementType.INGRESO -> colors.income
        MovementType.GASTO -> colors.expense
    }
}

@Composable
fun balanceColor(cents: Long): Color {
    val colors = LocalYescaColors.current
    return when {
        cents > 0L -> colors.income
        cents < 0L -> colors.expense
        else -> colors.balanceNeutral
    }
}

@Composable
fun balanceCardColor(): Color = LocalYescaColors.current.balanceCard

@Composable
fun balanceCardContentColor(): Color = LocalYescaColors.current.onBalanceCard
