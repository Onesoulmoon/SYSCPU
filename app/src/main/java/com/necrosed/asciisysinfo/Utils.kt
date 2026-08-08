package com.necrosed.asciisysinfo

fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "0 B"
    val kb = bytes / 1024.0; val mb = kb / 1024.0; val gb = mb / 1024.0
    return when { gb >= 1.0 -> "%.2f GB".format(gb); mb >= 1.0 -> "%.1f MB".format(mb)
        kb >= 1.0 -> "%.0f KB".format(kb); else -> "$bytes B" }
}
fun formatSpeed(bps: Long): String {
    val kbps = bps / 1024.0; val mbps = kbps / 1024.0
    return when { mbps >= 1.0 -> "%.2f MB/s".format(mbps)
        kbps >= 1.0 -> "%.1f KB/s".format(kbps); else -> "$bps B/s" }
}
fun Float.nanOr(f: Float.() -> String): String = if (isNaN()) "N/A" else f()
