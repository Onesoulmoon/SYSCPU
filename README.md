<img width="842" height="1280" alt="image" src="https://github.com/user-attachments/assets/721b5f15-e8a3-4154-a485-b71c43b34366" />
<img width="875" height="1280" alt="image" src="https://github.com/user-attachments/assets/a3d49cd2-5cbc-4ec2-810a-ba6d71abd9f9" />
<img width="1009" height="1280" alt="image" src="https://github.com/user-attachments/assets/44f68b22-8e28-4901-be75-3ec496ceec3d" />


# SYS-CPU

A CPU-Z-style system information app for Android with a paper terminal /
CRT amber aesthetic. Built with Kotlin + Jetpack Compose.

## Features

| Tab   | Content                                          |
|-------|--------------------------------------------------|
| CPU   | Model, ABI, cores, usage graph, frequencies      |
| GPU   | Renderer, vendor, OpenGL ES & GLSL versions      |
| RAM   | Total/used/available, swap, live graph           |
| BATT  | Level, chemistry, voltage, temp graph            |
| DISP  | Resolution, density, refresh rate, HDR           |
| SENS  | Accel, gyro, mag, barometer, light, proximity    |
| NET   | Type, IP, live speed graphs, session totals      |
| STOR  | Internal & external usage bars                   |

Theme toggle (Paper ↔ CRT Amber) is in the top-right header.

## Requirements

- Android Studio **Hedgehog (2023.1.1)** or newer
- Android **API 24+** (Android 7.0)
- Kotlin **1.9.20**
- Compose BOM **2024.04.01**

## Setup

1. Unzip this archive
2. Open the **AsciiSysInfo** folder in Android Studio
3. Let Gradle sync (it will download dependencies automatically)
4. Run on a physical device for accurate sensor + hardware data
5. Emulators will show limited sensor/GPU/frequency data

## Customisation hints

| File                  | What to change                              |
|-----------------------|---------------------------------------------|
| `Color.kt`            | PaperTheme / CrtTheme colour palettes       |
| `Type.kt`             | Font sizes and letter-spacing               |
| `AsciiPanel.kt`       | Box-drawing style, padding, border char     |
| `SystemViewModel.kt`  | `TICK_MS` refresh interval, `MAX_HIST` size |
| `MainActivity.kt`     | Tab labels, status-bar fields               |

## Package name

`com.necrosed.asciisysinfo` — change in `build.gradle.kts` and
`AndroidManifest.xml` before publishing.

## Permissions used

```
ACCESS_NETWORK_STATE   — connection type, IP
ACCESS_WIFI_STATE      — Wi-Fi RSSI & link speed
INTERNET               — TrafficStats baseline
```

No root required. Some sysfs paths (`/sys/class/thermal/`, `/proc/stat`)
may return partial data on locked-down OEM builds — the app handles this
gracefully with "N/A" fallbacks.
