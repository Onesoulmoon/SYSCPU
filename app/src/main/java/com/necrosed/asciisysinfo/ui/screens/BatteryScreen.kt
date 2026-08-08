package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun BatteryScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val bat    = state.info.battery

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "STATUS") {
                DataRow("HEALTH",       bat.health, if (bat.health == "Good") colors.green else colors.yellow)
                val lc = when { bat.level <= 15 -> colors.red; bat.level <= 30 -> colors.yellow; else -> colors.green }
                DataRow("LEVEL",        "${bat.level}%", lc)
                BtopBar(bat.level.toFloat(), 100f, fillColor = lc, width = 20)
                DataRow("POWER SOURCE", bat.chargePlug)
                DataRow("STATUS",       bat.status, if (bat.isCharging) colors.green else colors.textDim)
                DataRow("TECHNOLOGY",   bat.technology)
                val tc = when { bat.temperatureCelsius > 45 -> colors.red
                    bat.temperatureCelsius > 35 -> colors.yellow else -> colors.green }
                DataRow("TEMPERATURE",  "%.1f °C".format(bat.temperatureCelsius), tc)
                DataRow("VOLTAGE",      "${bat.voltageMv} mV")
                DataRow("PRESENT",      if (bat.present) "Yes" else "No")
            }
        }

        item {
            AsciiPanel(title = "LEVEL HISTORY") {
                BrailleGraph(
                    history = state.batteryHistory, min = 0f, max = 100f,
                    color = colors.green, dimColor = colors.green.copy(alpha = 0.1f), rows = 4
                )
            }
        }

        item {
            AsciiPanel(title = "TEMPERATURE HISTORY") {
                BrailleGraph(
                    history = state.battTempHistory, min = 20f, max = 60f,
                    color = colors.orange, dimColor = colors.orange.copy(alpha = 0.1f), rows = 4
                )
            }
        }
    }
}
