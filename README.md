<img width="230" height="540" alt="image" src="https://github.com/user-attachments/assets/b14cd3e1-54c9-4e8c-a379-9ccfdde7a552" />
<img width="230" height="540" alt="image" src="https://github.com/user-attachments/assets/2cb8f9e4-4554-4ca9-894d-7a9600f05bac" />
<img width="230" height="540" alt="image" src="https://github.com/user-attachments/assets/c5664870-fc48-4297-881e-b9a55da3e533" />
<img width="230" height="540" alt="image" src="https://github.com/user-attachments/assets/a7cbc4f5-4b38-4bb5-8a0b-85955a95bb6b" />
<img width="230" height="540" alt="image" src="https://github.com/user-attachments/assets/edf8efd6-90d6-4f6d-911d-7876405396fe" />
<img width="230" height="540" alt="image" src="https://github.com/user-attachments/assets/3a7b66af-41ef-4590-bf25-22ee034a150e" />
<img width="230" height="540" alt="image" src="https://github.com/user-attachments/assets/5a98d589-fc10-4e9b-9182-2a699803798e" />
<img width="230" height="540" alt="image" src="https://github.com/user-attachments/assets/70ee45d2-3a06-49a2-a7f2-b46e4891a03b" />
<img width="230" height="540" alt="bdf90251-cb02-4827-a3fc-66455ee54684" src="https://github.com/user-attachments/assets/303e350a-6fbf-4e47-ac87-26c91afc8e10" />
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
