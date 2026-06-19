package com.example.scfs.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.scfs.R

val Harmattan = FontFamily(
    Font(R.font.harmattan_regular),
)

val Hanuman = FontFamily(
    Font(R.font.hanuman_regular),
    Font(R.font.hanuman_bold, FontWeight.Bold)
)

val Typography = Typography(

    headlineLarge = TextStyle(
        fontFamily = Hanuman,
        fontWeight = FontWeight.Bold,
        fontSize = 48.sp
    ),

    bodyLarge = TextStyle(
        fontFamily = Harmattan,
        fontSize = 20.sp
    )

)