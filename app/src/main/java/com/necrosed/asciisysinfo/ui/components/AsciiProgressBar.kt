package com.necrosed.asciisysinfo.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors

@Composable
fun AsciiBar(
    value: Float,
    max: Float = 100f,
    width: Int = 26,
    label: String = "",
    modifier: Modifier = Modifier
) {
    val colors = LocalAsciiColors.current
    val ratio  = (value / max.coerceAtLeast(0.001f)).coerceIn(0f, 1f)
    val filled = (ratio * width).toInt()
    val empty  = width - filled
    val pct    = (ratio * 100).toInt()

    val barColor = when {
        ratio >= 0.90f -> colors.red
        ratio >= 0.70f -> colors.orange
        ratio >= 0.40f -> colors.accent
        else           -> colors.green
    }

    val bar = if (label.isNotEmpty())
        "$label [${"█".repeat(filled)}${"░".repeat(empty)}] $pct%"
    else
        "[${"█".repeat(filled)}${"░".repeat(empty)}] $pct%"

    Text(
        text = bar,
        style = AsciiType.label,
        color = barColor,
        maxLines = 1, softWrap = false, overflow = TextOverflow.Clip,
        modifier = modifier
    )
}
