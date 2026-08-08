package com.necrosed.asciisysinfo.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.formatBytes
import com.necrosed.asciisysinfo.formatSpeed
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

/**
 * At-a-glance dashboard of the things that actually change second to second:
 * CPU load, memory pressure, network throughput, battery drain. Static specs
 * (chip identity, display, storage, camera list…) live in their own tabs so
 * this screen stays fast to scan instead of duplicating everything.
 */
@Composable
fun OverviewScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val cpu    = state.info.cpu
    val ram    = state.info.ram
    val gpu    = state.info.gpu
    val net    = state.info.network
    val bat    = state.info.battery

    val uptime = remember(state.timestamp) {
        val ms = SystemClock.elapsedRealtime()
        val h  = ms / 3_600_000L
        val m  = (ms % 3_600_000L) / 60_000L
        "up ${h}h ${m}m"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(6.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        // ═══ ¹ CPU ═══════════════════════════════════════════════
        item {
            AsciiPanel(
                number = "¹", title = "CPU",
                extra  = "${cpu.usagePercent.toInt()}%",
                borderColor = colors.borderHi
            ) {
                BrailleGraph(
                    history = state.cpuHistory, min = 0f, max = 100f,
                    color = colors.cpu, dimColor = colors.cpuDim, rows = 3
                )
                Spacer(Modifier.height(3.dp))
                val cores = cpu.coreCount
                val half  = (cores + 1) / 2
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        (0 until half).forEach { i ->
                            val pct = if (i < cpu.frequencies.size)
                                (cpu.frequencies[i].toFloat() /
                                 (cpu.frequencies.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f)) * 100f
                            else 0f
                            CoreRow(i, pct.coerceIn(0f, 100f))
                        }
                    }
                    Spacer(Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        (half until cores).forEach { i ->
                            val pct = if (i < cpu.frequencies.size)
                                (cpu.frequencies[i].toFloat() /
                                 (cpu.frequencies.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f)) * 100f
                            else 0f
                            CoreRow(i, pct.coerceIn(0f, 100f))
                        }
                    }
                }
                Spacer(Modifier.height(3.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(uptime, style = AsciiType.small, color = colors.textDim)
                    if (!cpu.temperature.isNaN()) {
                        val tc = if (cpu.temperature > 75) colors.red
                                 else if (cpu.temperature > 55) colors.yellow else colors.green
                        Text("CPU %.1f°C".format(cpu.temperature), style = AsciiType.small, color = tc)
                    }
                }
                BtopBar(value = cpu.usagePercent, max = 100f, fillColor = colors.cpu, label = "")
            }
        }

        // ═══ ² MEM + GPU ═════════════════════════════════════════
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                AsciiPanel(modifier = Modifier.weight(1f), number = "²", title = "MEM") {
                    val totalGb = ram.totalBytes / 1_073_741_824.0
                    Text("Total: %.1f GiB".format(totalGb), style = AsciiType.small, color = colors.text)
                    Spacer(Modifier.height(2.dp))
                    Text("Used:", style = AsciiType.small, color = colors.memUsed)
                    BtopBar(value = ram.usedBytes.toFloat(), max = ram.totalBytes.toFloat().coerceAtLeast(1f),
                        fillColor = colors.memUsed, width = 14)
                    Spacer(Modifier.height(2.dp))
                    Text("Avail:", style = AsciiType.small, color = colors.memFree)
                    BtopBar(value = ram.availableBytes.toFloat(), max = ram.totalBytes.toFloat().coerceAtLeast(1f),
                        fillColor = colors.memFree, width = 14)
                    Spacer(Modifier.height(3.dp))
                    AsciiSparkline(history = state.ramHistory, color = colors.memUsed)
                    if (ram.lowMemory) Text("⚠ LOW MEM", style = AsciiType.small, color = colors.red)
                }

                AsciiPanel(modifier = Modifier.weight(1f), number = "³", title = "GPU") {
                    Text(gpu.renderer.take(20), style = AsciiType.small, color = colors.text,
                        maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(3.dp))
                    if (!gpu.loadPercent.isNaN()) {
                        Text("Load:", style = AsciiType.small, color = colors.textDim)
                        BtopBar(gpu.loadPercent, 100f, fillColor = colors.accent, width = 12)
                        Text(gpu.loadSource, style = AsciiType.small, color = colors.textDim)
                    } else {
                        Text("Load: N/A", style = AsciiType.small, color = colors.textDim)
                    }
                }
            }
        }

        // ═══ ⁴ NET ═══════════════════════════════════════════════
        item {
            AsciiPanel(number = "⁴", title = "NET", extra = net.type, borderColor = colors.border) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("▼ download", style = AsciiType.small, color = colors.netDl)
                        BrailleGraph(
                            history = state.netDlHistory, min = 0f,
                            max = (state.netDlHistory.maxOrNull() ?: 100f).coerceAtLeast(100f),
                            color = colors.netDl, dimColor = colors.netDl.copy(alpha = 0.15f), rows = 3
                        )
                        Text(formatSpeed(net.downloadBps), style = AsciiType.small, color = colors.netDl)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("▲ upload", style = AsciiType.small, color = colors.netUl)
                        BrailleGraph(
                            history = state.netUlHistory, min = 0f,
                            max = (state.netUlHistory.maxOrNull() ?: 100f).coerceAtLeast(100f),
                            color = colors.netUl, dimColor = colors.netUl.copy(alpha = 0.15f), rows = 3
                        )
                        Text(formatSpeed(net.uploadBps), style = AsciiType.small, color = colors.netUl)
                    }
                }
            }
        }

        // ═══ ⁵ BATTERY ═══════════════════════════════════════════
        item {
            AsciiPanel(number = "⁵", title = "BATTERY", extra = "${bat.level}% ${bat.status}") {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        val lc = when { bat.level <= 15 -> colors.red; bat.level <= 30 -> colors.yellow; else -> colors.green }
                        BtopBar(bat.level.toFloat(), 100f, fillColor = lc, width = 16, label = "")
                        Spacer(Modifier.height(2.dp))
                        val tc = if (bat.temperatureCelsius > 40) colors.red else colors.green
                        Text("Temp: %.1f°C".format(bat.temperatureCelsius), style = AsciiType.small, color = tc)
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        AsciiSparkline(history = state.batteryHistory,
                            color = if (bat.level > 30) colors.green else colors.red)
                    }
                }
            }
        }
    }
}
