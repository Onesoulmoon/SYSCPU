package com.necrosed.asciisysinfo.ui.screens

import android.app.ActivityManager
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.UiState

@Composable
fun GpuScreen(state: UiState) {
    val colors  = LocalAsciiColors.current
    val gpu     = state.info.gpu
    val ctx     = LocalContext.current
    val am      = ctx.getSystemService(ActivityManager::class.java)
    val devCfg  = am?.deviceConfigurationInfo
    val glesMaj = devCfg?.glEsVersion ?: "—"

    // Detect if GPU info is valid
    val hasGpuInfo = gpu.renderer != "Unknown" && gpu.renderer != "Fetching…" && gpu.renderer.isNotBlank()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "GPU") {
                if (!hasGpuInfo) {
                    Text(
                        "No information available.",
                        style = AsciiType.body,
                        color = colors.textDim,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                } else {
                    DataRow("RENDERER",  gpu.renderer)
                    DataRow("VENDOR",    gpu.vendor)
                    Spacer(Modifier.height(4.dp))
                    DataRow("VERSION",   gpu.glVersion)
                    DataRow("GLSL VER",  gpu.glslVersion)
                    DataRow("GLES FEAT", glesMaj)
                    
                    val vulkan = Build.VERSION.SDK_INT >= 24
                    DataRow("VULKAN", if (vulkan) "API 24+ available" else "Not supported",
                        valueColor = if (vulkan) colors.green else colors.textDim)
                }
            }
        }

        if (hasGpuInfo) {
            item {
                AsciiPanel(title = "NOTE") {
                    Text(
                        "GPU strings are collected once via EGL at startup.\n" +
                        "Restart the app to refresh.",
                        style = AsciiType.small, color = colors.textDim
                    )
                }
            }
        }
    }
}
