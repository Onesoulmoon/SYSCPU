package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.formatBytes
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun DeviceScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val dev    = state.info.device
    val disp   = state.info.display
    val ram    = state.info.ram
    val stor   = state.info.storage

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "MODEL") {
                DataRow("MODEL",        dev.model)
                DataRow("MANUFACTURER", dev.manufacturer)
                DataRow("BOARD",        dev.board)
                DataRow("HARDWARE",     dev.hardware)
            }
        }

        item {
            AsciiPanel(title = "DISPLAY") {
                DataRow("SIZE",       disp.diagonalInch)
                DataRow("RESOLUTION", "${disp.widthPx} × ${disp.heightPx} px")
                DataRow("DENSITY",    "${disp.densityDpi} dpi")
                DataRow("REFRESH",    "%.0f Hz".format(disp.refreshRateHz))
            }
        }

        item {
            AsciiPanel(title = "RAM") {
                DataRow("TOTAL",     formatBytes(ram.totalBytes))
                val availPct = if (ram.totalBytes > 0) (ram.availableBytes.toFloat() / ram.totalBytes * 100) else 0f
                DataRow("AVAILABLE", "${formatBytes(ram.availableBytes)}  (${availPct.toInt()}%)")
            }
        }

        item {
            AsciiPanel(title = "STORAGE") {
                DataRow("INTERNAL",  formatBytes(stor.internalTotalBytes))
                val availPct = if (stor.internalTotalBytes > 0)
                    (stor.internalFreeBytes.toFloat() / stor.internalTotalBytes * 100) else 0f
                DataRow("AVAILABLE", "${formatBytes(stor.internalFreeBytes)}  (${availPct.toInt()}%)")
                if (stor.hasExternal) {
                    DataRow("EXTERNAL", formatBytes(stor.externalTotalBytes))
                }
            }
        }

        item {
            AsciiPanel(title = "CAMERAS") {
                if (dev.cameras.isEmpty()) {
                    DataRow("STATUS", "None detected")
                } else {
                    dev.cameras.forEach { cam ->
                        DataRow("${cam.facing} (${cam.id})", cam.megapixels)
                    }
                }
            }
        }

        item {
            AsciiPanel(title = "CONNECTIVITY") {
                dev.connectivityFeatures.forEach { feat ->
                    DataRow(feat.name, if (feat.supported) "✓" else "—",
                        if (feat.supported) colors.green else colors.textDim)
                }
            }
        }
    }
}
