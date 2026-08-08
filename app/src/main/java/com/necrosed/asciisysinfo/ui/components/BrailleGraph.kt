package com.necrosed.asciisysinfo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors

/**
 * Braille columns — each char represents one data point at heights 0-8.
 * Matches the btop braille graph style.
 */
private val BRAILLE = arrayOf(
    "⠀", // 0   empty
    "⡀", // 1/8
    "⡄", // 2/8
    "⡆", // 3/8
    "⡇", // 4/8
    "⣇", // 5/8
    "⣧", // 6/8
    "⣷", // 7/8
    "⣿"  // 8/8  full
)

/** Full-width braille history graph, btop-style */
@Composable
fun BrailleGraph(
    history:   List<Float>,
    min:       Float = 0f,
    max:       Float = 100f,
    color:     Color? = null,
    dimColor:  Color? = null,
    rows:      Int    = 4,
    modifier:  Modifier = Modifier
) {
    val colors     = LocalAsciiColors.current
    val lineColor  = color    ?: colors.cpu
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxPx  = constraints.maxWidth.toFloat()
        val charPx = remember(textMeasurer) {
            textMeasurer.measure("⣿".repeat(10), style = AsciiType.body).size.width / 10f
        }.coerceAtLeast(1f)
        val cols   = ((maxPx / charPx).toInt()).coerceAtLeast(1)

        val range  = (max - min).coerceAtLeast(0.001f)
        val data   = history.takeLast(cols)

        // Build rows from bottom up — each row is a "threshold" layer
        // Row 0 = bottom, row (rows-1) = top
        val lines = (rows - 1 downTo 0).map { row ->
            val threshold = row.toFloat() / rows
            data.joinToString("") { v ->
                val ratio = ((v - min) / range).coerceIn(0f, 1f)
                if (ratio > threshold) {
                    val level = ((ratio - threshold) * rows * 8).toInt().coerceIn(0, 8)
                    BRAILLE[level]
                } else {
                    BRAILLE[0]  // empty
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            lines.forEachIndexed { i, line ->
                val isTop = i == 0
                Text(
                    text  = line,
                    style = AsciiType.body,
                    color = if (isTop) lineColor else lineColor.copy(alpha = 0.5f + i * 0.1f),
                    maxLines  = 1,
                    softWrap  = false,
                    overflow  = TextOverflow.Clip,
                    modifier  = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Btop-style colored progress bar */
@Composable
fun BtopBar(
    value:     Float,
    max:       Float  = 100f,
    width:     Int    = 22,
    fillColor: Color? = null,
    label:     String = "",
    showPct:   Boolean = true,
    modifier:  Modifier = Modifier
) {
    val colors = LocalAsciiColors.current
    val ratio  = (value / max.coerceAtLeast(0.001f)).coerceIn(0f, 1f)
    val filled = (ratio * width).toInt()
    val empty  = width - filled
    val pct    = (ratio * 100).toInt()
    val fc     = fillColor ?: when {
        ratio >= 0.90f -> colors.red
        ratio >= 0.70f -> colors.yellow
        ratio >= 0.40f -> colors.cpu
        else           -> colors.green
    }
    val bar = buildString {
        if (label.isNotEmpty()) append("$label ")
        append("[")
        append("█".repeat(filled))
        append("░".repeat(empty))
        append("]")
        if (showPct) append(" $pct%")
    }
    Text(bar, style = AsciiType.label, color = fc,
        maxLines = 1, softWrap = false, modifier = modifier)
}

/** Per-core CPU row — btop compact grid style */
@Composable
fun CoreRow(
    index:   Int,
    percent: Float,
    width:   Int  = 10,
    freqMhz: Long = 0L,
    modifier: Modifier = Modifier
) {
    val colors = LocalAsciiColors.current
    val color  = when {
        percent >= 80f -> colors.red
        percent >= 50f -> colors.yellow
        else           -> colors.cpu
    }
    val filled = ((percent / 100f) * width).toInt().coerceIn(0, width)
    val empty  = width - filled
    val label  = "C%-2d".format(index)
    val bar    = "${"█".repeat(filled)}${"░".repeat(empty)}"
    val pct    = "%3d%%".format(percent.toInt())
    val freq   = if (freqMhz > 0) " %4dMHz".format(freqMhz) else ""
    
    Text(
        text  = "$label $bar $pct$freq",
        style = AsciiType.small,
        color = color,
        maxLines = 1, softWrap = false,
        modifier = modifier
    )
}
