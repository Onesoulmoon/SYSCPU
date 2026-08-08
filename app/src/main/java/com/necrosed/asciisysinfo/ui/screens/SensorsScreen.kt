package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.nanOr
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun SensorsScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val s      = state.info.sensors

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "ENVIRONMENT") {
                DataRow("AMBIENT TEMP", s.ambientTempC.nanOr { "%.1f°C".format(this) })
                DataRow("PRESSURE",     s.pressureHpa.nanOr { "%.1f hPa".format(this) })
                DataRow("HUMIDITY",     s.humidity.nanOr { "%.1f%%".format(this) })
                DataRow("LIGHT",        s.lightLux.nanOr { "%.0f lux".format(this) })
                DataRow("PROXIMITY",    s.proximity.nanOr { "%.1f cm".format(this) })
            }
        }

        item {
            AsciiPanel(title = "ACCELEROMETER  m/s²") {
                DataRow("X", s.accelX.nanOr { "% .4f".format(this) })
                DataRow("Y", s.accelY.nanOr { "% .4f".format(this) })
                DataRow("Z", s.accelZ.nanOr { "% .4f".format(this) })
            }
        }

        item {
            AsciiPanel(title = "GYROSCOPE  rad/s") {
                DataRow("X", s.gyroX.nanOr { "% .5f".format(this) })
                DataRow("Y", s.gyroY.nanOr { "% .5f".format(this) })
                DataRow("Z", s.gyroZ.nanOr { "% .5f".format(this) })
            }
        }

        item {
            AsciiPanel(title = "MAGNETOMETER  µT") {
                DataRow("X", s.magnetX.nanOr { "% .2f".format(this) })
                DataRow("Y", s.magnetY.nanOr { "% .2f".format(this) })
                DataRow("Z", s.magnetZ.nanOr { "% .2f".format(this) })
            }
        }

        item {
            AsciiPanel(title = "SENSOR LIST (${s.sensorList.size})") {
                s.sensorList.take(20).forEach { sensor ->
                    DataRow(sensor.type, sensor.name)
                }
                if (s.sensorList.size > 20) {
                    Text("+ ${s.sensorList.size - 20} more", style = AsciiType.small, color = colors.textDim,
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}
