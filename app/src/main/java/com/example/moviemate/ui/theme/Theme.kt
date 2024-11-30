package com.example.moviemate.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.moviemate.R

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

val GentlemensScript = FontFamily(
    Font(R.font.gentlemensscript, FontWeight.Normal)
)

val YesevaOne = FontFamily(
    Font(R.font.yeseva_one, FontWeight.Bold)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)


//val DarkBlue = Color(0xFF0D47A1)
val NavyBlue = Color(0xFF1A237E)
val Indigo = Color(0xFF283593)
val DeepIndigo = Color(0xFF1B1F3B)
val RichBlack = Color(0xFF1F1F1F)
val Gray = Color(0xFFB0BEC5)
val LightGray = Color(0xFFE0E0E0)
val White = Color(0xFFFFFFFF)
val TextWhiteColorPallet = Color(0xFFEDEDED)
val ButtonGray = Color(0xFF2A2A2A)
val DeepGray = Color(0xFF2F3436)
val LightBlue = Color(0xFF1FA6DE)
val DarkBlue = Color(0xFF0777C8)

@SuppressLint("ConflictingOnColor")
val DarkColorPalette = darkColors(
    primary = DarkBlue,
    primaryVariant = NavyBlue,
    secondary = Indigo,
    secondaryVariant = LightBlue,
    background = RichBlack,
    surface = DeepIndigo,
    onPrimary = White,
    onSecondary = DeepGray,
    onBackground = TextWhiteColorPallet,
    onSurface = LightGray,
    error = Color(0xFFCF6679)
)

@Composable
fun MovieMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}