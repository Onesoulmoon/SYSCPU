package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.formatBytes
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun StorageScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val stor   = state.info.storage

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "INTERNAL STORAGE") {
                DataRow("TOTAL",  formatBytes(stor.internalTotalBytes))
                DataRow("USED",   formatBytes(stor.internalUsedBytes), valueColor = colors.accent)
                DataRow("FREE",   formatBytes(stor.internalFreeBytes), valueColor = colors.green)
                Spacer(Modifier.height(4.dp))
                AsciiBar(
                    value = stor.internalUsedBytes.toFloat(),
                    max   = stor.internalTotalBytes.toFloat().coerceAtLeast(1f)
                )
            }
        }

        item {
            AsciiPanel(title = "EXTERNAL STORAGE") {
                if (!stor.hasExternal) {
                    DataRow("STATUS", "Not mounted", valueColor = colors.textDim)
                } else {
                    DataRow("TOTAL", formatBytes(stor.externalTotalBytes))
                    DataRow("USED",  formatBytes(stor.externalUsedBytes), valueColor = colors.accent)
                    DataRow("FREE",  formatBytes(stor.externalFreeBytes), valueColor = colors.green)
                    Spacer(Modifier.height(4.dp))
                    AsciiBar(
                        value = stor.externalUsedBytes.toFloat(),
                        max   = stor.externalTotalBytes.toFloat().coerceAtLeast(1f)
                    )
                }
            }
        }
    }
}
