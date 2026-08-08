package com.necrosed.asciisysinfo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.necrosed.asciisysinfo.data.SystemDataSource
import com.necrosed.asciisysinfo.data.SystemInfo
import com.necrosed.asciisysinfo.ui.theme.TerminalTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppTab { OVERVIEW, SOC, DEVICE, SYSTEM, BATTERY, THERMAL, SENSORS, NETWORK, ABOUT }

data class UiState(
    val info:            SystemInfo    = SystemInfo(),
    val cpuHistory:      List<Float>   = emptyList(),
    val ramHistory:      List<Float>   = emptyList(),
    val batteryHistory:  List<Float>   = emptyList(),
    val battTempHistory: List<Float>   = emptyList(),
    val netDlHistory:    List<Float>   = emptyList(),
    val netUlHistory:    List<Float>   = emptyList(),
    val gpuLoadHistory:  List<Float>   = emptyList(),
    val theme:           TerminalTheme = TerminalTheme.BTOP,
    val activeTab:       AppTab        = AppTab.OVERVIEW,
    val showRawThermal:  Boolean       = false,
    val isLoading:       Boolean       = true,
    val timestamp:       Long          = 0L
)

class SystemViewModel(app: Application) : AndroidViewModel(app) {

    private val ds = SystemDataSource(app.applicationContext)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    companion object {
        private const val MAX_HIST = 60
        private const val TICK_MS  = 1_000L
    }

    init {
        ds.registerSensors()
        viewModelScope.launch {
            while (true) { tick(); delay(TICK_MS) }
        }
    }

    private fun tick() {
        val info = SystemInfo(
            cpu     = ds.getCpuInfo(),
            gpu     = ds.getGpuInfo(),
            ram     = ds.getRamInfo(),
            battery = ds.getBatteryInfo(),
            display = ds.getDisplayInfo(),
            sensors = ds.getSensorsInfo(),
            network = ds.getNetworkInfo(),
            storage = ds.getStorageInfo(),
            soc     = ds.getSocInfo(),
            device  = ds.getDeviceInfo(),
            system  = ds.getSystemDetailInfo(),
            thermal = ds.getThermalInfo(),
            root    = ds.getRootInfo()
        )
        val p = _state.value
        fun List<Float>.push(v: Float) = (this + v).takeLast(MAX_HIST)
        _state.value = p.copy(
            info            = info,
            cpuHistory      = p.cpuHistory.push(info.cpu.usagePercent),
            ramHistory      = p.ramHistory.push(info.ram.usagePercent),
            batteryHistory  = p.batteryHistory.push(info.battery.level.toFloat()),
            battTempHistory = p.battTempHistory.push(info.battery.temperatureCelsius),
            netDlHistory    = p.netDlHistory.push(info.network.downloadBps / 1024f),
            netUlHistory    = p.netUlHistory.push(info.network.uploadBps  / 1024f),
            gpuLoadHistory  = if (!info.gpu.loadPercent.isNaN())
                                  p.gpuLoadHistory.push(info.gpu.loadPercent) else p.gpuLoadHistory,
            isLoading       = false,
            timestamp       = System.currentTimeMillis()
        )
    }

    fun setTheme(t: TerminalTheme)      { _state.value = _state.value.copy(theme = t) }
    fun setTab(tab: AppTab)             { _state.value = _state.value.copy(activeTab = tab) }
    fun toggleRawThermal()              { _state.value = _state.value.copy(showRawThermal = !_state.value.showRawThermal) }
    fun setGpuInfo(renderer: String, vendor: String, gl: String, glsl: String) {
        ds.gpuRenderer    = renderer
        ds.gpuVendor      = vendor
        ds.gpuGlVersion   = gl
        ds.gpuGlslVersion = glsl
    }

    override fun onCleared() {
        super.onCleared()
        ds.unregisterSensors()
    }
}
