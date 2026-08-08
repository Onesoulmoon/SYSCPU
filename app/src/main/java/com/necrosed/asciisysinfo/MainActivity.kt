package com.necrosed.asciisysinfo

import android.opengl.EGL14
import android.opengl.GLES20
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necrosed.asciisysinfo.ui.components.TabBar
import com.necrosed.asciisysinfo.ui.screens.*
import com.necrosed.asciisysinfo.ui.theme.*
import com.necrosed.asciisysinfo.viewmodel.AppTab
import com.necrosed.asciisysinfo.viewmodel.SystemViewModel
import com.necrosed.asciisysinfo.viewmodel.UiState
import java.util.Calendar

class MainActivity : ComponentActivity() {
    private val vm: SystemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        initGpuInfo()
        setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            AsciiSysTheme(theme = state.theme) {
                AsciiSysApp(
                    state       = state,
                    vm          = vm,
                    onThemeCycle = {
                        vm.setTheme(when (state.theme) {
                            TerminalTheme.BTOP  -> TerminalTheme.PAPER
                            TerminalTheme.PAPER -> TerminalTheme.CRT
                            TerminalTheme.CRT   -> TerminalTheme.BTOP
                        })
                    }
                )
            }
        }
    }

    private fun initGpuInfo() {
        Thread {
            try {
                val dpy = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
                if (dpy == EGL14.EGL_NO_DISPLAY) return@Thread
                val ver = IntArray(2)
                if (!EGL14.eglInitialize(dpy, ver, 0, ver, 1)) return@Thread
                val cfgAttr = intArrayOf(
                    EGL14.EGL_RENDERABLE_TYPE, 4,
                    EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                    EGL14.EGL_RED_SIZE, 8, EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8, EGL14.EGL_NONE
                )
                val cfgs = arrayOfNulls<android.opengl.EGLConfig>(1)
                val num  = IntArray(1)
                EGL14.eglChooseConfig(dpy, cfgAttr, 0, cfgs, 0, 1, num, 0)
                if (num[0] == 0) { EGL14.eglTerminate(dpy); return@Thread }
                val pb  = intArrayOf(EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE)
                val suf = EGL14.eglCreatePbufferSurface(dpy, cfgs[0], pb, 0)
                val ca  = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE)
                val ctx = EGL14.eglCreateContext(dpy, cfgs[0], EGL14.EGL_NO_CONTEXT, ca, 0)
                EGL14.eglMakeCurrent(dpy, suf, suf, ctx)
                runOnUiThread {
                    vm.setGpuInfo(
                        GLES20.glGetString(GLES20.GL_RENDERER)                  ?: "Unknown",
                        GLES20.glGetString(GLES20.GL_VENDOR)                    ?: "Unknown",
                        GLES20.glGetString(GLES20.GL_VERSION)                   ?: "Unknown",
                        GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION)  ?: "Unknown"
                    )
                }
                EGL14.eglMakeCurrent(dpy, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
                EGL14.eglDestroyContext(dpy, ctx)
                EGL14.eglDestroySurface(dpy, suf)
                EGL14.eglTerminate(dpy)
            } catch (t: Throwable) {
                // Catch Throwable, not just Exception — some OEM GPU drivers throw
                // Error subclasses here, which would otherwise crash the app since
                // Threads have no default exception handling on Android.
                android.util.Log.e("AsciiSys", "EGL init failed: ${t.message}")
            }
        }.also { it.isDaemon = true }.start()
    }
}

@Composable
fun AsciiSysApp(state: UiState, vm: SystemViewModel, onThemeCycle: () -> Unit) {
    val colors = LocalAsciiColors.current

    val clock = remember(state.timestamp) {
        val c = Calendar.getInstance()
        "%02d:%02d:%02d".format(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .systemBarsPadding()
    ) {
        // ── TOP BAR ──────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.surface)
                .padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text("▌SYS", style = AsciiType.appTitle, color = colors.cpu)
            Text(clock, style = AsciiType.body, color = colors.text)
            val label = when (state.theme) {
                TerminalTheme.BTOP  -> "[BTOP]"
                TerminalTheme.PAPER -> "[PAPER]"
                TerminalTheme.CRT   -> "[CRT]"
            }
            Text(
                label, style = AsciiType.tab, color = colors.borderHi,
                modifier = Modifier.clickable(onClick = onThemeCycle).padding(4.dp)
            )
        }

        // ── TAB BAR ──────────────────────────────────────────────────
        TabBar(active = state.activeTab, onSelect = vm::setTab)

        // ── SEPARATOR ────────────────────────────────────────────────
        Text(
            text = "═".repeat(80), style = AsciiType.small, color = colors.border,
            maxLines = 1, softWrap = false,
            modifier = Modifier.fillMaxWidth().background(colors.surface)
        )

        // ── SCREEN CONTENT ───────────────────────────────────────────
        Box(modifier = Modifier.weight(1f).fillMaxWidth().background(colors.background)) {
            when (state.activeTab) {
                AppTab.OVERVIEW -> OverviewScreen(state)
                AppTab.SOC      -> SocScreen(state)
                AppTab.DEVICE   -> DeviceScreen(state)
                AppTab.SYSTEM   -> SystemScreen(state)
                AppTab.BATTERY  -> BatteryScreen(state)
                AppTab.THERMAL  -> ThermalScreen(state, vm)
                AppTab.SENSORS  -> SensorsScreen(state)
                AppTab.NETWORK  -> NetworkScreen(state)
                AppTab.ABOUT    -> AboutScreen()
            }
        }
    }
}
