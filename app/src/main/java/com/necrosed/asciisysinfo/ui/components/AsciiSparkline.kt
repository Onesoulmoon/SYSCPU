package com.necrosed.asciisysinfo.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors

private const val BLOCKS = "▁▂▃▄▅▆▇█"

@Composable
fun AsciiSparkline(
    history: List<Float>,
    min: Float = 0f,
    max: Float = 100f,
    samples: Int = 32,
    modifier: Modifier = Modifier,
    color: Color? = null
) {
    val colors       = LocalAsciiColors.current
    val displayColor = color ?: colors.accent

    val spark = remember(history, min, max, samples) {
        val data  = history.takeLast(samples)
        if (data.isEmpty()) return@remember " ".repeat(samples)
        val range = (max - min).coerceAtLeast(0.001f)
        data.joinToString("") { v ->
            val n   = ((v - min) / range).coerceIn(0f, 1f)
            val idx = (n * (BLOCKS.length - 1)).toInt()
            BLOCKS[idx].toString()
        }
    }

    Text(
        text     = spark,
        style    = AsciiType.body,
        color    = displayColor,
        maxLines = 1,
        softWrap = false,
        modifier = modifier
    )
}
