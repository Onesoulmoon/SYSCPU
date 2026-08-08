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
fun RamScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val ram    = state.info.ram

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "MEMORY") {
                // RAM History Graph
                Text("usage history", style = AsciiType.label, color = colors.blue)
                BrailleGraph(
                    history = state.ramHistory,
                    min = 0f, max = 100f,
                    rows = 3,
                    color = colors.blue
                )
                
                Spacer(Modifier.height(8.dp))

                AsciiBar(
                    value = ram.usagePercent,
                    max = 100f,
                    label = "RAM"
                )
                
                Spacer(Modifier.height(8.dp))

                DataRow("USED",      formatBytes(ram.usedBytes),      valueColor = colors.accent)
                DataRow("AVAILABLE", formatBytes(ram.availableBytes),  valueColor = colors.green)
                DataRow("TOTAL",     formatBytes(ram.totalBytes))
                
                if (ram.lowMemory) {
                    Text("⚠ LOW MEMORY DETECTED", style = AsciiType.label, color = colors.red)
                }
            }
        }

        if (ram.swapTotalBytes > 0L) {
            item {
                AsciiPanel(title = "SWAP") {
                    AsciiBar(
                        value = ram.swapUsedBytes.toFloat(),
                        max   = ram.swapTotalBytes.toFloat().coerceAtLeast(1f),
                        label = "SWAP"
                    )
                    Spacer(Modifier.height(4.dp))
                    DataRow("USED",  formatBytes(ram.swapUsedBytes),  valueColor = colors.accent)
                    DataRow("FREE",  formatBytes(ram.swapTotalBytes - ram.swapUsedBytes), valueColor = colors.green)
                    DataRow("TOTAL", formatBytes(ram.swapTotalBytes))
                }
            }
        }
    }
}
