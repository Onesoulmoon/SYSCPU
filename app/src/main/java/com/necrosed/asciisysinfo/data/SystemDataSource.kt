package com.necrosed.asciisysinfo.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.TrafficStats
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.display.DisplayManager
import android.content.pm.PackageManager
import android.view.Display
import java.io.File
import java.net.Inet6Address
import java.net.NetworkInterface

class SystemDataSource(private val ctx: Context) {
    private var prevTotal = 0L; private var prevIdle = 0L
    private var prevRx = TrafficStats.getTotalRxBytes().coerceAtLeast(0)
    private var prevTx = TrafficStats.getTotalTxBytes().coerceAtLeast(0)
    private var prevNetMs = System.currentTimeMillis()
    private val sensorValues = mutableMapOf<Int, FloatArray>()
    private var sensorListener: SensorEventListener? = null
    var gpuRenderer = "Fetching…"; var gpuVendor = "Fetching…"
    var gpuGlVersion = "—"; var gpuGlslVersion = "—"

    fun registerSensors() {
        val sm = ctx.getSystemService(SensorManager::class.java) ?: return
        val listener = object : SensorEventListener {
            override fun onSensorChanged(e: SensorEvent) { sensorValues[e.sensor.type] = e.values.clone() }
            override fun onAccuracyChanged(s: Sensor, a: Int) {}
        }
        sensorListener = listener
        listOf(Sensor.TYPE_AMBIENT_TEMPERATURE, Sensor.TYPE_PRESSURE, Sensor.TYPE_RELATIVE_HUMIDITY,
               Sensor.TYPE_LIGHT, Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE,
               Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_PROXIMITY).forEach { t ->
            sm.getDefaultSensor(t)?.let { sm.registerListener(listener, it, SensorManager.SENSOR_DELAY_NORMAL) }
        }
    }
    fun unregisterSensors() { sensorListener?.let { ctx.getSystemService(SensorManager::class.java)?.unregisterListener(it) } }

    fun getCpuInfo(): CpuInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        return CpuInfo(
            model = try { File("/proc/cpuinfo").bufferedReader().use { r ->
                r.lineSequence().firstOrNull { it.startsWith("Hardware") || it.startsWith("model name") }
                    ?.substringAfter(":")?.trim() } ?: Build.HARDWARE } catch (_: Exception) { Build.HARDWARE },
            abi = Build.SUPPORTED_ABIS.firstOrNull() ?: Build.CPU_ABI,
            coreCount = cores,
            frequencies = (0 until cores).map { i -> try {
                File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq").readText().trim().toLong() / 1000L
            } catch (_: Exception) { 0L } },
            usagePercent = try {
                val line = File("/proc/stat").bufferedReader().use { it.readLine() } ?: "0 0 0 0 0 0 0"
                val nums = line.trim().split(" ").filter { it.isNotEmpty() }.drop(1).map { it.toLongOrNull() ?: 0L }
                val total = nums.sum(); val idle = nums.getOrElse(3){0L} + nums.getOrElse(4){0L}
                val dt = total - prevTotal; val di = idle - prevIdle
                prevTotal = total; prevIdle = idle
                if (dt > 0) ((dt - di).toFloat() / dt * 100f).coerceIn(0f, 100f) else 0f
            } catch (_: Exception) { 0f },
            temperature = listOf("/sys/class/thermal/thermal_zone0/temp",
                "/sys/class/thermal/thermal_zone1/temp").firstNotNullOfOrNull { path ->
                try { val r = File(path).readText().trim().toLongOrNull() ?: return@firstNotNullOfOrNull null
                    if (r > 1000L) r / 1000f else r.toFloat() } catch (_: Exception) { null }
            } ?: Float.NaN
        )
    }

    fun getGpuInfo(): GpuInfo {
        // GPU load: only reliably exposed on Adreno via KGSL sysfs. Never fake this
        // on unsupported GPUs — show N/A with the reason instead of a guessed number.
        val (load, source) = readAdrenoGpuLoad()
        return GpuInfo(gpuRenderer, gpuVendor, gpuGlVersion, gpuGlslVersion, load, source)
    }

    private fun readAdrenoGpuLoad(): Pair<Float, String> {
        // Preferred: some Adreno kernels expose a direct percentage file.
        try {
            val pct = File("/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage")
                .readText().trim().removeSuffix("%").toFloatOrNull()
            if (pct != null) return Pair(pct.coerceIn(0f, 100f), "KGSL")
        } catch (_: Exception) {}
        // Fallback: "busy_cycles total_cycles" pair, compute ratio.
        try {
            val raw = File("/sys/class/kgsl/kgsl-3d0/gpubusy").readText().trim()
            val parts = raw.split(" ").filter { it.isNotEmpty() }.map { it.toLongOrNull() ?: 0L }
            if (parts.size >= 2 && parts[1] > 0L) {
                return Pair((parts[0].toFloat() / parts[1] * 100f).coerceIn(0f, 100f), "KGSL")
            }
        } catch (_: Exception) {}
        return Pair(Float.NaN, "No supported utilization interface")
    }

    fun getRamInfo(): RamInfo {
        val am = ctx.getSystemService(ActivityManager::class.java) ?: return RamInfo()
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val total = mi.totalMem; val avail = mi.availMem; val used = total - avail
        var swapT = 0L; var swapF = 0L
        try { File("/proc/meminfo").forEachLine { line -> when {
            line.startsWith("SwapTotal:") -> swapT = line.trim().split(" ").filter{it.isNotEmpty()}.getOrNull(1)?.toLongOrNull()?.times(1024) ?: 0L
            line.startsWith("SwapFree:")  -> swapF = line.trim().split(" ").filter{it.isNotEmpty()}.getOrNull(1)?.toLongOrNull()?.times(1024) ?: 0L
        }}} catch (_: Exception) {}
        return RamInfo(total, avail, used, if(total>0) used.toFloat()/total*100f else 0f,
            mi.lowMemory, mi.threshold, swapT, swapT - swapF)
    }

    fun getBatteryInfo(): BatteryInfo {
        val i = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return BatteryInfo()
        val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val plug = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
        val health = i.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        return BatteryInfo(
            level = if(scale>0) level*100/scale else level, isCharging = charging,
            chargePlug = when(plug) { 
                BatteryManager.BATTERY_PLUGGED_AC->"AC"
                BatteryManager.BATTERY_PLUGGED_USB->"USB" 
                BatteryManager.BATTERY_PLUGGED_WIRELESS->"Wireless" 
                else->"Unplugged" 
            },
            voltageMv = i.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0),
            temperatureCelsius = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f,
            health = when(health) { 
                BatteryManager.BATTERY_HEALTH_GOOD->"Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT->"Overheat" 
                BatteryManager.BATTERY_HEALTH_DEAD->"Dead"
                BatteryManager.BATTERY_HEALTH_COLD->"Cold" 
                else->"Unknown" 
            },
            technology = i.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "—",
            status = when(status) { 
                BatteryManager.BATTERY_STATUS_CHARGING->"Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING->"Discharging" 
                BatteryManager.BATTERY_STATUS_FULL->"Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING->"Not Charging" 
                else->"Unknown" 
            },
            present = i.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false)
        )
    }

    fun getDisplayInfo(): DisplayInfo {
        val dm = ctx.resources.displayMetrics
        // ctx.display requires a UI-associated context (Activity, or one made via
        // createWindowContext) and throws UnsupportedOperationException on API 31+
        // when called on the Application context. DisplayManager works from any
        // context regardless of API level, so it's the safe path here since
        // SystemDataSource is constructed with applicationContext.
        val display = ctx.getSystemService(DisplayManager::class.java)
            ?.getDisplay(Display.DEFAULT_DISPLAY)
        val hz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) display?.mode?.refreshRate ?: 60f
            else @Suppress("DEPRECATION") display?.refreshRate ?: 60f
        val hdr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            display?.hdrCapabilities?.supportedHdrTypes?.map { when(it) {
                android.view.Display.HdrCapabilities.HDR_TYPE_HDR10->"HDR10"
                android.view.Display.HdrCapabilities.HDR_TYPE_HLG->"HLG"
                android.view.Display.HdrCapabilities.HDR_TYPE_HDR10_PLUS->"HDR10+"
                android.view.Display.HdrCapabilities.HDR_TYPE_DOLBY_VISION->"Dolby Vision" else->"HDR"} } ?: emptyList()
        else emptyList()
        val orient = when(ctx.resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT->"Portrait" 
            Configuration.ORIENTATION_LANDSCAPE->"Landscape" 
            else->"Unknown" 
        }
        val diag = Math.sqrt((dm.widthPixels.toDouble()/dm.densityDpi).let{it*it} + (dm.heightPixels.toDouble()/dm.densityDpi).let{it*it})
        return DisplayInfo(dm.widthPixels, dm.heightPixels, dm.widthPixels/dm.density, dm.heightPixels/dm.density,
            dm.densityDpi, dm.density, hz, hdr, orient, "%.1f".format(diag))
    }

    fun getSensorsInfo(): SensorsInfo {
        val sm = ctx.getSystemService(SensorManager::class.java) ?: return SensorsInfo()
        fun v(t: Int, i: Int = 0) = sensorValues[t]?.getOrNull(i) ?: Float.NaN
        return SensorsInfo(
            ambientTempC = v(Sensor.TYPE_AMBIENT_TEMPERATURE), pressureHpa = v(Sensor.TYPE_PRESSURE),
            humidity = v(Sensor.TYPE_RELATIVE_HUMIDITY), lightLux = v(Sensor.TYPE_LIGHT),
            accelX = v(Sensor.TYPE_ACCELEROMETER,0), accelY = v(Sensor.TYPE_ACCELEROMETER,1), accelZ = v(Sensor.TYPE_ACCELEROMETER,2),
            gyroX = v(Sensor.TYPE_GYROSCOPE,0), gyroY = v(Sensor.TYPE_GYROSCOPE,1), gyroZ = v(Sensor.TYPE_GYROSCOPE,2),
            magnetX = v(Sensor.TYPE_MAGNETIC_FIELD,0), magnetY = v(Sensor.TYPE_MAGNETIC_FIELD,1), magnetZ = v(Sensor.TYPE_MAGNETIC_FIELD,2),
            proximity = v(Sensor.TYPE_PROXIMITY),
            sensorList = sm.getSensorList(Sensor.TYPE_ALL).map { SensorEntry(it.name, "Type(${it.type})", it.vendor) }
        )
    }

    fun getNetworkInfo(): NetworkInfo {
        val cm = ctx.getSystemService(ConnectivityManager::class.java) ?: return NetworkInfo()
        val caps = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val type = when { caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)==true->"Wi-Fi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)==true->"Cellular"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)==true->"Ethernet" else->"None" }
        val now = System.currentTimeMillis()
        val rx = TrafficStats.getTotalRxBytes().coerceAtLeast(0)
        val tx = TrafficStats.getTotalTxBytes().coerceAtLeast(0)
        val dt = ((now - prevNetMs) / 1000f).coerceAtLeast(0.001f)
        val dl = ((rx - prevRx) / dt).toLong().coerceAtLeast(0)
        val ul = ((tx - prevTx) / dt).toLong().coerceAtLeast(0)
        prevRx = rx; prevTx = tx; prevNetMs = now
        val wifi = if(type=="Wi-Fi") @Suppress("DEPRECATION") ctx.applicationContext.getSystemService(WifiManager::class.java)?.connectionInfo else null
        val ipv4 = try { java.net.NetworkInterface.getNetworkInterfaces()?.toList()
            ?.flatMap{it.inetAddresses.toList()}?.firstOrNull{!it.isLoopbackAddress && it !is Inet6Address}?.hostAddress ?: "—" } catch(_:Exception){"—"}
        val ipv6 = try { java.net.NetworkInterface.getNetworkInterfaces()?.toList()
            ?.flatMap{it.inetAddresses.toList()}?.firstOrNull{!it.isLoopbackAddress && it is Inet6Address}?.hostAddress ?: "—" } catch(_:Exception){"—"}
        // SSID requires location permission on API 27+ to resolve to a real value;
        // without it Android returns "<unknown ssid>" — we surface that as-is
        // rather than pretending we have a value we don't.
        val ssid = wifi?.ssid?.removeSurrounding("\"") ?: "—"
        return NetworkInfo(caps!=null, type, emptyList(), ipv4, ipv6, dl, ul, rx, tx,
            wifi?.rssi ?: 0, wifi?.linkSpeed ?: 0, ssid)
    }

    fun getStorageInfo(): StorageInfo {
        val iSt = StatFs(Environment.getDataDirectory().path)
        val iT = iSt.blockCountLong * iSt.blockSizeLong
        val iF = iSt.availableBlocksLong * iSt.blockSizeLong
        val hasExt = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        var eT = 0L; var eF = 0L
        if(hasExt) try { val eSt = StatFs(Environment.getExternalStorageDirectory().path)
            eT = eSt.blockCountLong * eSt.blockSizeLong; eF = eSt.availableBlocksLong * eSt.blockSizeLong } catch(_:Exception){}
        return StorageInfo(iT, iT-iF, iF, eT, eT-eF, eF, hasExt)
    }

    // ══ SOC ══════════════════════════════════════════════════════════════
    // Deliberately no chipset brand/name lookup (e.g. "Snapdragon 8 Gen 1").
    // That requires a maintained model→marketing-name database this app
    // doesn't have. Showing only what's directly readable keeps this honest.

    fun getSocInfo(): SocInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            Build.SOC_MODEL else readCpuInfoField("Hardware") ?: Build.HARDWARE
        val (implementer, part, variant) = readArmIdentity()
        return SocInfo(
            socModel    = socModel,
            coreCount   = cores,
            is64Bit     = Build.SUPPORTED_64_BIT_ABIS.isNotEmpty(),
            frequencies = (0 until cores).map { i -> try {
                File("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq").readText().trim().toLong() / 1000L
            } catch (_: Exception) { 0L } },
            armImplementer = implementer, armPart = part, armVariant = variant
        )
    }

    private fun readCpuInfoField(field: String): String? = try {
        File("/proc/cpuinfo").bufferedReader().use { r ->
            r.lineSequence().firstOrNull { it.startsWith(field) }?.substringAfter(":")?.trim()
        }
    } catch (_: Exception) { null }

    /** Returns (implementer, part, variant) hex strings from /proc/cpuinfo, e.g. (0x41, 0xd47, 0x1) */
    private fun readArmIdentity(): Triple<String, String, String> {
        return try {
            var impl = "—"; var part = "—"; var variant = "—"
            File("/proc/cpuinfo").forEachLine { line ->
                when {
                    line.startsWith("CPU implementer") -> impl    = line.substringAfter(":").trim()
                    line.startsWith("CPU part")         -> part    = line.substringAfter(":").trim()
                    line.startsWith("CPU variant")      -> variant = line.substringAfter(":").trim()
                }
            }
            Triple(impl, part, variant)
        } catch (_: Exception) { Triple("—", "—", "—") }
    }

    // ══ DEVICE ═══════════════════════════════════════════════════════════

    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            model            = Build.MODEL,
            manufacturer     = Build.MANUFACTURER,
            board             = Build.BOARD,
            hardware          = Build.HARDWARE,
            cameras           = readCameras(),
            connectivityFeatures = readConnectivityFeatures()
        )
    }

    private fun readCameras(): List<CameraEntry> {
        return try {
            val cm = ctx.getSystemService(CameraManager::class.java) ?: return emptyList()
            cm.cameraIdList.map { id ->
                val chars = cm.getCameraCharacteristics(id)
                val facing = when (chars.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT    -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK     -> "Back"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                    else -> "Unknown"
                }
                val size = chars.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE)
                val mp = if (size != null)
                    "%.1f MP".format((size.width.toLong() * size.height.toLong()) / 1_000_000.0)
                else "—"
                CameraEntry(id, facing, mp)
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun readConnectivityFeatures(): List<ConnectivityFeature> {
        val pm = ctx.packageManager
        fun has(feature: String) = pm.hasSystemFeature(feature)
        return listOf(
            ConnectivityFeature("Wi-Fi",       has(PackageManager.FEATURE_WIFI)),
            ConnectivityFeature("Bluetooth",   has(PackageManager.FEATURE_BLUETOOTH)),
            ConnectivityFeature("NFC",         has(PackageManager.FEATURE_NFC)),
            ConnectivityFeature("Telephony",   has(PackageManager.FEATURE_TELEPHONY)),
            ConnectivityFeature("USB Host",    has(PackageManager.FEATURE_USB_HOST)),
            ConnectivityFeature("GPS",         has(PackageManager.FEATURE_LOCATION_GPS)),
        )
    }

    // ══ SYSTEM ═══════════════════════════════════════════════════════════

    fun getSystemDetailInfo(): SystemDetailInfo {
        val gles = try {
            val am = ctx.getSystemService(ActivityManager::class.java)
            am?.deviceConfigurationInfo?.glEsVersion ?: "—"
        } catch (_: Exception) { "—" }
        val playServices = try {
            ctx.packageManager.getPackageInfo("com.google.android.gms", 0).versionName ?: "—"
        } catch (_: Exception) { "Not installed" }
        val kernelVersion = try {
            File("/proc/version").readText().trim()
        } catch (_: Exception) { System.getProperty("os.version") ?: "—" }

        return SystemDetailInfo(
            androidVersion  = Build.VERSION.RELEASE ?: "—",
            apiLevel        = Build.VERSION.SDK_INT,
            securityPatch   = Build.VERSION.SECURITY_PATCH.ifEmpty { "—" },
            bootloader      = Build.BOOTLOADER ?: "—",
            buildId         = Build.DISPLAY ?: "—",
            javaVm          = "ART ${System.getProperty("java.vm.version") ?: "—"}",
            openGlEs        = gles,
            kernelArch      = System.getProperty("os.arch") ?: "—",
            kernelVersion   = kernelVersion,
            playServicesVer = playServices,
            uptimeMillis    = android.os.SystemClock.elapsedRealtime()
        )
    }

    // ══ THERMAL ══════════════════════════════════════════════════════════
    // Raw zone names are device/vendor-specific and cryptic (cpuss-0, pa,
    // sdr-mmw-therm...). We keep them verbatim for the raw view, and also
    // best-effort classify by keyword match for the summary cards. Zones
    // that don't match any known keyword land in OTHER and are still shown
    // in the raw list — never silently dropped.

    fun getThermalInfo(): ThermalInfo {
        val zones = mutableListOf<ThermalZone>()
        try {
            val base = File("/sys/class/thermal")
            base.listFiles { f -> f.name.startsWith("thermal_zone") }?.forEach { zoneDir ->
                try {
                    val type = File(zoneDir, "type").readText().trim()
                    val raw  = File(zoneDir, "temp").readText().trim().toLongOrNull() ?: return@forEach
                    val temp = if (raw > 1000L) raw / 1000f else raw.toFloat()
                    // Sanity filter — some zones report garbage/negative/absurd values
                    if (temp < -40f || temp > 150f) return@forEach
                    zones.add(ThermalZone(type, temp, classifyZone(type)))
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}

        fun avgOf(cat: ThermalCategory): Float {
            val matches = zones.filter { it.category == cat }
            return if (matches.isEmpty()) Float.NaN else matches.map { it.tempCelsius }.average().toFloat()
        }

        return ThermalInfo(
            zones     = zones.sortedBy { it.name },
            cpuAvg    = avgOf(ThermalCategory.CPU),
            gpuAvg    = avgOf(ThermalCategory.GPU),
            batteryAvg = avgOf(ThermalCategory.BATTERY),
            skinAvg   = avgOf(ThermalCategory.SKIN)
        )
    }

    private fun classifyZone(rawName: String): ThermalCategory {
        val n = rawName.lowercase()
        return when {
            n.contains("cpu")  || n.contains("cpuss") || n.contains("apc")   -> ThermalCategory.CPU
            n.contains("gpu")  || n.contains("gpuss")                        -> ThermalCategory.GPU
            n.contains("batt")                                               -> ThermalCategory.BATTERY
            n.contains("skin") || n.contains("quiet") || n.contains("case")  -> ThermalCategory.SKIN
            else -> ThermalCategory.OTHER
        }
    }

    // ══ ROOT ═════════════════════════════════════════════════════════════
    // Multi-indicator confidence rather than a single boolean — a single su
    // path check is trivially wrong on many stock ROMs and equally trivially
    // bypassed by anything that actually wants to hide root.

    fun getRootInfo(): RootInfo {
        val suPaths = listOf(
            "/system/bin/su", "/system/xbin/su", "/sbin/su",
            "/system/sd/xbin/su", "/data/local/xbin/su",
            "/data/local/bin/su", "/data/local/su", "/su/bin/su"
        )
        val magiskPaths = listOf(
            "/sbin/.magisk", "/data/adb/magisk", "/data/adb/modules"
        )
        val suFound     = suPaths.any { try { File(it).exists() } catch (_: Exception) { false } }
        val magiskFound = magiskPaths.any { try { File(it).exists() } catch (_: Exception) { false } }
        val testKeys    = Build.TAGS?.contains("test-keys") == true

        val indicatorCount = listOf(suFound, magiskFound, testKeys).count { it }
        val confidence = when {
            suFound && magiskFound          -> RootConfidence.DETECTED
            indicatorCount >= 1              -> RootConfidence.POSSIBLE
            else                              -> RootConfidence.NOT_DETECTED
        }
        return RootInfo(confidence, suFound, magiskFound, testKeys)
    }
}
