package com.necrosed.asciisysinfo.ui.screens

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun CpuScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val cpu    = state.info.cpu

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(
                title = "CPU",
                extra = cpu.model.take(24)
            ) {
                // Identity: The Braille History Graph (btop style)
                BrailleGraph(
                    history = state.cpuHistory,
                    min = 0f, max = 100f,
                    rows = 4,
                    color = colors.cpu
                )
                
                Spacer(Modifier.height(10.dp))
                
                // Per-Core Grid (2 columns)
                val cores = cpu.coreCount
                val half  = (cores + 1) / 2
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        (0 until half).forEach { i ->
                            val maxFreq = cpu.frequencies.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
                            val freq = cpu.frequencies.getOrNull(i) ?: 0L
                            val pct = (freq.toFloat() / maxFreq * 100f).coerceIn(0f, 100f)
                            CoreRow(index = i, percent = pct, freqMhz = freq)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        (half until cores).forEach { i ->
                            val maxFreq = cpu.frequencies.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f
                            val freq = cpu.frequencies.getOrNull(i) ?: 0L
                            val pct = (freq.toFloat() / maxFreq * 100f).coerceIn(0f, 100f)
                            CoreRow(index = i, percent = pct, freqMhz = freq)
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    DataRow("USAGE", "${cpu.usagePercent.toInt()}%", modifier = Modifier.weight(1f), valueColor = colors.cpu)
                    if (!cpu.temperature.isNaN()) {
                        val tc = when {
                            cpu.temperature > 75 -> colors.red
                            cpu.temperature > 55 -> colors.orange
                            else -> colors.green
                        }
                        DataRow("TEMP", "%.1f°C".format(cpu.temperature), modifier = Modifier.weight(1f), valueColor = tc)
                    }
                }
            }
        }

        item {
            AsciiPanel(title = "HARDWARE") {
                DataRow("MODEL",    Build.MODEL)
                DataRow("BOARD",    Build.BOARD)
                DataRow("ABI",      cpu.abi)
                DataRow("SDK",      Build.VERSION.SDK_INT.toString())
            }
        }
    }
}
