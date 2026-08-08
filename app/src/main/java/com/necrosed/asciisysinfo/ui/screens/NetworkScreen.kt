package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.formatBytes
import com.necrosed.asciisysinfo.formatSpeed
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun NetworkScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val net    = state.info.network

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "CONNECTION") {
                DataRow("STATUS", if (net.isConnected) "Connected" else "Disconnected",
                    if (net.isConnected) colors.green else colors.red)
                DataRow("TYPE", net.type)
                if (net.type == "Wi-Fi") {
                    DataRow("SSID",       net.wifiSsid)
                    DataRow("RSSI",       "${net.wifiRssi} dBm")
                    DataRow("LINK SPEED", "${net.wifiLinkSpeedMbps} Mbps")
                }
                DataRow("IPv4", net.ipv4)
                val ip6 = net.ipv6.take(28) + if (net.ipv6.length > 28) "…" else ""
                DataRow("IPv6", ip6)
            }
        }

        item {
            AsciiPanel(title = "▼ DOWNLOAD") {
                DataRow("SPEED", formatSpeed(net.downloadBps), colors.netDl)
                DataRow("TOTAL", formatBytes(net.totalRxBytes))
                Spacer(Modifier.height(4.dp))
                BrailleGraph(
                    history = state.netDlHistory, min = 0f,
                    max = (state.netDlHistory.maxOrNull() ?: 100f).coerceAtLeast(100f),
                    color = colors.netDl, dimColor = colors.netDl.copy(alpha = 0.12f), rows = 4
                )
                Spacer(Modifier.height(3.dp))
                AsciiSparkline(history = state.netDlHistory, color = colors.netDl)
            }
        }

        item {
            AsciiPanel(title = "▲ UPLOAD") {
                DataRow("SPEED", formatSpeed(net.uploadBps), colors.netUl)
                DataRow("TOTAL", formatBytes(net.totalTxBytes))
                Spacer(Modifier.height(4.dp))
                BrailleGraph(
                    history = state.netUlHistory, min = 0f,
                    max = (state.netUlHistory.maxOrNull() ?: 100f).coerceAtLeast(100f),
                    color = colors.netUl, dimColor = colors.netUl.copy(alpha = 0.12f), rows = 4
                )
                Spacer(Modifier.height(3.dp))
                AsciiSparkline(history = state.netUlHistory, color = colors.netUl)
            }
        }
    }
}
