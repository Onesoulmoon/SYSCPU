package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.data.RootConfidence
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun SystemScreen(state: UiState) {
    val colors = LocalAsciiColors.current
    val sys    = state.info.system
    val root   = state.info.root

    val uptimeStr = remember(state.timestamp) {
        val totalSec = sys.uptimeMillis / 1000
        val d = totalSec / 86400
        val h = (totalSec % 86400) / 3600
        val m = (totalSec % 3600) / 60
        "${d}d ${h}h ${m}m"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "ANDROID") {
                DataRow("VERSION",       sys.androidVersion)
                DataRow("API LEVEL",     "${sys.apiLevel}")
                DataRow("SECURITY PATCH", sys.securityPatch)
                DataRow("BOOTLOADER",    sys.bootloader)
                DataRow("BUILD ID",      sys.buildId)
            }
        }

        item {
            AsciiPanel(title = "RUNTIME") {
                DataRow("JAVA VM",   sys.javaVm)
                DataRow("OPENGL ES", sys.openGlEs)
                DataRow("KERNEL ARCH", sys.kernelArch)
            }
        }

        item {
            AsciiPanel(title = "KERNEL") {
                Text(sys.kernelVersion, style = AsciiType.small, color = colors.text)
            }
        }

        item {
            AsciiPanel(title = "SERVICES") {
                DataRow("GOOGLE PLAY SVC", sys.playServicesVer)
                DataRow("UPTIME",          uptimeStr)
            }
        }

        item {
            val rootColor = when (root.confidence) {
                RootConfidence.DETECTED     -> colors.red
                RootConfidence.POSSIBLE     -> colors.yellow
                RootConfidence.NOT_DETECTED -> colors.green
            }
            AsciiPanel(title = "ROOT") {
                DataRow("STATUS", when (root.confidence) {
                    RootConfidence.DETECTED     -> "DETECTED"
                    RootConfidence.POSSIBLE     -> "POSSIBLE"
                    RootConfidence.NOT_DETECTED -> "NOT DETECTED"
                }, rootColor)
                DataRow("SU BINARY",  if (root.suBinary)  "✓" else "—")
                DataRow("MAGISK",     if (root.magisk)    "✓" else "—")
                DataRow("TEST-KEYS",  if (root.testKeys)  "✓" else "—")
            }
        }
    }
}
