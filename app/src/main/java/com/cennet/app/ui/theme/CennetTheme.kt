package com.cennet.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class CennetColors(
    val background: Color,
    val sage: Color,
    val lightGreen: Color,
    val midGreen: Color,
    val forest: Color,
    val darkForest: Color,
    val cream: Color,
    val peach: Color,
    val pink: Color,
    val mutedText: Color,
    val text: Color,
    val border: Color
)

val GreenCennetColors = CennetColors(
    background = Color(0xFFF5F3E8), sage = Color(0xFFDDE8D2), lightGreen = Color(0xFFC6DBB7),
    midGreen = Color(0xFF8FAD79), forest = Color(0xFF355E3B), darkForest = Color(0xFF27482D),
    cream = Color(0xFFFFFBF0), peach = Color(0xFFF1D8C8), pink = Color(0xFFEFD5D1),
    mutedText = Color(0xFF65715F), text = Color(0xFF263328), border = Color(0xFFC7D1B9)
)

val CennetColorPalettes = listOf(
    GreenCennetColors,
    GreenCennetColors.copy(sage = Color(0xFFDDE9D9), lightGreen = Color(0xFFBFD9C5), midGreen = Color(0xFF82A78C)),
    GreenCennetColors.copy(sage = Color(0xFFF0DDD2), lightGreen = Color(0xFFE5BFAF), midGreen = Color(0xFFBC8E7E), forest = Color(0xFF72564B)),
    GreenCennetColors.copy(sage = Color(0xFFDCE3E0), lightGreen = Color(0xFFB8C8C2), midGreen = Color(0xFF718B82), forest = Color(0xFF38564C))
)

val LocalCennetColors = staticCompositionLocalOf { GreenCennetColors }

private val CennetTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.Cursive, fontSize = 43.sp, lineHeight = 46.sp, fontWeight = FontWeight.Normal),
    headlineLarge = TextStyle(fontFamily = FontFamily.Cursive, fontSize = 30.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Cursive, fontSize = 23.sp, lineHeight = 27.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 18.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp)
)

@Composable
fun CennetTheme(themeIndex: Int = 0, content: @Composable () -> Unit) {
    val colors = CennetColorPalettes[themeIndex.coerceIn(CennetColorPalettes.indices)]
    CompositionLocalProvider(LocalCennetColors provides colors) {
        MaterialTheme(typography = CennetTypography, content = content)
    }
}

val cennetColors: CennetColors @Composable get() = LocalCennetColors.current
