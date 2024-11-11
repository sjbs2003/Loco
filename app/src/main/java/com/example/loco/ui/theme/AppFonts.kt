package com.example.loco.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.loco.R

object AppFonts {
    private val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs
    )

    // Define your Google Fonts
    val Montserrat = FontFamily(
        Font(GoogleFont("Montserrat"), provider)
    )

    val PlayfairDisplay = FontFamily(
        Font(GoogleFont("Playfair Display"), provider)
    )

    val Roboto = FontFamily(
        Font(GoogleFont("Roboto"), provider)
    )

    val Lora = FontFamily(
        Font(GoogleFont("Lora"), provider)
    )

    val OpenSans = FontFamily(
        Font(GoogleFont("Open Sans"), provider)
    )

    val Quicksand = FontFamily(
        Font(GoogleFont("Quicksand"), provider)
    )

    val Raleway = FontFamily(
        Font(GoogleFont("Raleway"), provider)
    )

    val Poppins = FontFamily(
        Font(GoogleFont("Poppins"), provider)
    )

    val NotoSerif = FontFamily(
        Font(GoogleFont("Noto Serif"), provider)
    )

    val Nunito = FontFamily(
        Font(GoogleFont("Nunito"), provider)
    )

    // Font Collection for easy iteration
    val availableFonts = listOf(
        "Default" to FontFamily.Default,
        "Montserrat" to Montserrat,
        "Playfair Display" to PlayfairDisplay,
        "Roboto" to Roboto,
        "Lora" to Lora,
        "Open Sans" to OpenSans,
        "Quicksand" to Quicksand,
        "Raleway" to Raleway,
        "Poppins" to Poppins,
        "Noto Serif" to NotoSerif,
        "Nunito" to Nunito
    )

    // Predefined text styles that respect the current theme
    @Composable
    fun getTitleStyle(
        fontFamily: FontFamily = FontFamily.Default,
        isDark: Boolean
    ): TextStyle {
        return TextStyle(
            fontFamily = fontFamily,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onBackground
        )
    }

    @Composable
    fun getContentStyle(
        fontFamily: FontFamily = FontFamily.Default,
        isDark: Boolean
    ): TextStyle {
        return TextStyle(
            fontFamily = fontFamily,
            fontSize = 16.sp,
            color = if (isDark) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }

    // Font size options
    val fontSizes = listOf(
        "Small" to 14.sp,
        "Medium" to 16.sp,
        "Large" to 18.sp,
        "Extra Large" to 20.sp
    )
}