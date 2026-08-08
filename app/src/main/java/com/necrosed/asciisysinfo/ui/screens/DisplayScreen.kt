package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun DisplayScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val disp   = state.info.display

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "DISPLAY") {
                DataRow("RESOLUTION",  "${disp.widthPx} × ${disp.heightPx} px")
                DataRow("SIZE DP",     "${disp.widthDp.toInt()} × ${disp.heightDp.toInt()} dp")
                DataRow("DENSITY",     "${disp.densityDpi} dpi (×${disp.densityFactor})")
                DataRow("REFRESH",     "%.1f Hz".format(disp.refreshRateHz))
                DataRow("DIAGONAL",    disp.diagonalInch)
                DataRow("ORIENTATION", disp.orientation)
            }
        }

        item {
            AsciiPanel(title = "DENSITY CLASS") {
                val cls = when {
                    disp.densityDpi >= 640 -> "XXXHDPI"
                    disp.densityDpi >= 480 -> "XXHDPI"
                    disp.densityDpi >= 320 -> "XHDPI"
                    disp.densityDpi >= 240 -> "HDPI"
                    disp.densityDpi >= 160 -> "MDPI"
                    else                   -> "LDPI"
                }
                DataRow("CLASS",   cls, valueColor = colors.accent)
                DataRow("DPI",     "${disp.densityDpi}")
                DataRow("SCALE",   "×${disp.densityFactor}")
            }
        }

        item {
            AsciiPanel(title = "HDR SUPPORT") {
                if (disp.hdrTypes.isEmpty()) {
                    DataRow("HDR", "Not supported", valueColor = colors.textDim)
                } else {
                    disp.hdrTypes.forEach { hdr ->
                        DataRow(hdr, "✓ Supported", valueColor = colors.green)
                    }
                }
            }
        }
    }
}
