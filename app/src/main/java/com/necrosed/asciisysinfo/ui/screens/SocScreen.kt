package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun SocScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val soc    = state.info.soc

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "CHIP") {
                DataRow("MODEL",     soc.socModel)
                DataRow("CORES",     "${soc.coreCount}")
                DataRow("ARCH",      if (soc.is64Bit) "ARM 64-bit" else "ARM 32-bit")
                Text(
                    "No brand/marketing name lookup — this is exactly what the\n" +
                    "OS reports, nothing guessed or inferred.",
                    style = AsciiType.small, color = colors.textDim,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            AsciiPanel(title = "PER-CORE CLOCK") {
                if (soc.frequencies.isEmpty() || soc.frequencies.all { it == 0L }) {
                    DataRow("STATUS", "Not accessible on this device")
                } else {
                    soc.frequencies.forEachIndexed { i, freq ->
                        if (freq > 0L) DataRow("C$i", "$freq MHz")
                    }
                }
            }
        }

        item {
            AsciiPanel(title = "ARM IDENTITY") {
                DataRow("IMPLEMENTER", soc.armImplementer)
                DataRow("PART",        soc.armPart)
                DataRow("VARIANT",     soc.armVariant)
                Text(
                    "Raw values from /proc/cpuinfo. Cross-reference the ARM\n" +
                    "implementer/part registry to identify the exact core.",
                    style = AsciiType.small, color = colors.textDim,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
