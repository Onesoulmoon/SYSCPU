package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.nanOr
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.SystemViewModel
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun ThermalScreen(state: UiState, vm: SystemViewModel) {
    val colors  = LocalAsciiColors.current
    val thermal = state.info.thermal

    fun tempColor(t: Float) = when {
        t.isNaN()  -> colors.textDim
        t > 55f    -> colors.red
        t > 42f    -> colors.yellow
        else       -> colors.green
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "SUMMARY") {
                DataRow("CPU",     thermal.cpuAvg.nanOr { "%.1f °C".format(this) },     tempColor(thermal.cpuAvg))
                DataRow("GPU",     thermal.gpuAvg.nanOr { "%.1f °C".format(this) },     tempColor(thermal.gpuAvg))
                DataRow("BATTERY", thermal.batteryAvg.nanOr { "%.1f °C".format(this) }, tempColor(thermal.batteryAvg))
                DataRow("SKIN",    thermal.skinAvg.nanOr { "%.1f °C".format(this) },    tempColor(thermal.skinAvg))
                Spacer(Modifier.height(4.dp))
                Text(
                    "${thermal.zones.size} zones detected",
                    style = AsciiType.small, color = colors.textDim
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = if (state.showRawThermal) "[ HIDE RAW ZONES ]" else "[ SHOW RAW ZONES ]",
                    style = AsciiType.tab,
                    color = colors.accent,
                    modifier = Modifier
                        .background(colors.surface)
                        .clickable { vm.toggleRawThermal() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                )
            }
        }

        if (state.showRawThermal) {
            item {
                AsciiPanel(title = "RAW THERMAL ZONES") {
                    if (thermal.zones.isEmpty()) {
                        DataRow("STATUS", "No zones readable on this device")
                    } else {
                        thermal.zones.forEach { zone ->
                            DataRow(
                                zone.name.take(20),
                                "%.1f °C".format(zone.tempCelsius),
                                tempColor(zone.tempCelsius)
                            )
                        }
                    }
                }
            }
        }
    }
}
