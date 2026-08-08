package com.necrosed.asciisysinfo.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AsciiType {
    val mono = FontFamily.Monospace
    val appTitle = TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold,   fontSize = 14.sp, letterSpacing = 2.sp)
    val body     = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal,  fontSize = 12.sp, letterSpacing = 0.3.sp)
    val label    = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal,  fontSize = 11.sp, letterSpacing = 0.2.sp)
    val value    = TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold,    fontSize = 11.sp, letterSpacing = 0.2.sp)
    val small    = TextStyle(fontFamily = mono, fontWeight = FontWeight.Normal,  fontSize = 10.sp, letterSpacing = 0.1.sp)
    val tab      = TextStyle(fontFamily = mono, fontWeight = FontWeight.Bold,    fontSize = 10.sp, letterSpacing = 1.sp)
}
