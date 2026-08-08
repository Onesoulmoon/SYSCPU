package com.necrosed.asciisysinfo.data

data class SystemInfo(
    val cpu:      CpuInfo      = CpuInfo(),
    val gpu:      GpuInfo      = GpuInfo(),
    val ram:      RamInfo      = RamInfo(),
    val battery:  BatteryInfo  = BatteryInfo(),
    val display:  DisplayInfo  = DisplayInfo(),
    val sensors:  SensorsInfo  = SensorsInfo(),
    val network:  NetworkInfo  = NetworkInfo(),
    val storage:  StorageInfo  = StorageInfo(),
    val soc:      SocInfo      = SocInfo(),
    val device:   DeviceInfo   = DeviceInfo(),
    val system:   SystemDetailInfo = SystemDetailInfo(),
    val thermal:  ThermalInfo  = ThermalInfo(),
    val root:     RootInfo     = RootInfo()
)

data class CpuInfo(
    val model: String = "—", val abi: String = "—",
    val coreCount: Int = 0, val frequencies: List<Long> = emptyList(),
    val usagePercent: Float = 0f, val temperature: Float = Float.NaN
)

data class GpuInfo(
    val renderer: String = "Fetching…", val vendor: String = "Fetching…",
    val glVersion: String = "—", val glslVersion: String = "—",
    val loadPercent: Float = Float.NaN, val loadSource: String = "—"
)

data class RamInfo(
    val totalBytes: Long = 0, val availableBytes: Long = 0,
    val usedBytes: Long = 0, val usagePercent: Float = 0f,
    val lowMemory: Boolean = false, val threshold: Long = 0,
    val swapTotalBytes: Long = 0, val swapUsedBytes: Long = 0
)

data class BatteryInfo(
    val level: Int = 0, val isCharging: Boolean = false,
    val chargePlug: String = "—", val voltageMv: Int = 0,
    val temperatureCelsius: Float = 0f, val health: String = "—",
    val technology: String = "—", val status: String = "—",
    val present: Boolean = false
)

data class DisplayInfo(
    val widthPx: Int = 0, val heightPx: Int = 0,
    val widthDp: Float = 0f, val heightDp: Float = 0f,
    val densityDpi: Int = 0, val densityFactor: Float = 0f,
    val refreshRateHz: Float = 0f, val hdrTypes: List<String> = emptyList(),
    val orientation: String = "—", val diagonalInch: String = "—"
)

data class SensorsInfo(
    val ambientTempC: Float = Float.NaN, val pressureHpa: Float = Float.NaN,
    val humidity: Float = Float.NaN, val lightLux: Float = Float.NaN,
    val accelX: Float = Float.NaN, val accelY: Float = Float.NaN, val accelZ: Float = Float.NaN,
    val gyroX: Float = Float.NaN, val gyroY: Float = Float.NaN, val gyroZ: Float = Float.NaN,
    val magnetX: Float = Float.NaN, val magnetY: Float = Float.NaN, val magnetZ: Float = Float.NaN,
    val proximity: Float = Float.NaN, val sensorList: List<SensorEntry> = emptyList()
)
data class SensorEntry(val name: String, val type: String, val vendor: String)

data class NetworkInfo(
    val isConnected: Boolean = false, val type: String = "None",
    val capabilities: List<String> = emptyList(),
    val ipv4: String = "—", val ipv6: String = "—",
    val downloadBps: Long = 0, val uploadBps: Long = 0,
    val totalRxBytes: Long = 0, val totalTxBytes: Long = 0,
    val wifiRssi: Int = 0, val wifiLinkSpeedMbps: Int = 0,
    val wifiSsid: String = "—"
)

data class StorageInfo(
    val internalTotalBytes: Long = 0, val internalUsedBytes: Long = 0,
    val internalFreeBytes: Long = 0, val externalTotalBytes: Long = 0,
    val externalUsedBytes: Long = 0, val externalFreeBytes: Long = 0,
    val hasExternal: Boolean = false
)

// ═══ NEW: SoC — raw, no chipset-name enrichment (per decision: accuracy over prettiness) ═══
data class SocInfo(
    val socModel:       String      = "—",   // Build.SOC_MODEL (API 31+) or /proc/cpuinfo Hardware
    val coreCount:      Int         = 0,
    val is64Bit:        Boolean     = false,
    val frequencies:    List<Long>  = emptyList(),  // MHz per core
    val armPart:        String      = "—",   // "CPU part" from /proc/cpuinfo, e.g. 0xd47
    val armImplementer: String      = "—",   // "CPU implementer" hex
    val armVariant:     String      = "—"
)

// ═══ NEW: Device — model/manufacturer/board/display/ram/storage/cameras/connectivity ═══
data class DeviceInfo(
    val model:              String  = "—",
    val manufacturer:       String  = "—",
    val board:               String  = "—",
    val hardware:            String  = "—",
    val cameras:             List<CameraEntry> = emptyList(),
    val connectivityFeatures: List<ConnectivityFeature> = emptyList()
)
data class CameraEntry(val id: String, val facing: String, val megapixels: String)
data class ConnectivityFeature(val name: String, val supported: Boolean)

// ═══ NEW: System — Android/build/kernel/runtime info ═══
data class SystemDetailInfo(
    val androidVersion:   String = "—",
    val apiLevel:         Int    = 0,
    val securityPatch:    String = "—",
    val bootloader:       String = "—",
    val buildId:          String = "—",
    val javaVm:           String = "—",
    val openGlEs:         String = "—",
    val kernelArch:       String = "—",
    val kernelVersion:    String = "—",
    val playServicesVer:  String = "—",
    val uptimeMillis:     Long   = 0L
)

// ═══ NEW: Thermal — categorized summary + raw zone dump ═══
data class ThermalZone(val name: String, val tempCelsius: Float, val category: ThermalCategory)
enum class ThermalCategory { CPU, GPU, BATTERY, SKIN, OTHER }
data class ThermalInfo(
    val zones:       List<ThermalZone> = emptyList(),
    val cpuAvg:       Float = Float.NaN,
    val gpuAvg:       Float = Float.NaN,
    val batteryAvg:   Float = Float.NaN,
    val skinAvg:      Float = Float.NaN
)

// ═══ NEW: Root — multi-indicator confidence, not a single boolean ═══
enum class RootConfidence { NOT_DETECTED, POSSIBLE, DETECTED }
data class RootInfo(
    val confidence:   RootConfidence = RootConfidence.NOT_DETECTED,
    val suBinary:     Boolean = false,
    val magisk:       Boolean = false,
    val testKeys:     Boolean = false
)
