package com.necrosed.asciisysinfo.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors

// btop uses single-line borders
private const val H  = "─"
private const val V  = "│"
private const val TL = "┌"
private const val TR = "┐"
private const val BL = "└"
private const val BR = "┘"

@Composable
fun AsciiPanel(
    modifier: Modifier = Modifier,
    title:    String   = "",
    number:   String   = "",       // btop-style superscript e.g. "¹"
    extra:    String   = "",       // right-side header info
    borderColor: Color? = null,
    content:  @Composable ColumnScope.() -> Unit
) {
    val colors    = LocalAsciiColors.current
    val bc        = borderColor ?: colors.border
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val maxPx  = constraints.maxWidth.toFloat()
        val charPx = remember(textMeasurer) {
            textMeasurer.measure("─".repeat(20), style = AsciiType.body).size.width / 20f
        }.coerceAtLeast(1f)
        val innerN = ((maxPx / charPx).toInt() - 2).coerceAtLeast(0)

        // Build top border: ┌─ ¹ TITLE ──────────── EXTRA ─┐
        val topLine = buildString {
            append(TL)
            val prefix = if (number.isNotEmpty()) "$H $number $title " else "$H $title "
            append(prefix)
            val rightPad = if (extra.isNotEmpty()) " $extra $H" else H
            val fillLen  = (innerN - prefix.length - rightPad.length).coerceAtLeast(0)
            append(H.repeat(fillLen))
            append(rightPad)
            append(TR)
        }
        val botLine = "$BL${H.repeat(innerN)}$BR"

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(topLine, style = AsciiType.body, color = bc,
                maxLines = 1, softWrap = false, overflow = TextOverflow.Clip)
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(V, style = AsciiType.body, color = bc)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) { content() }
                Text(V, style = AsciiType.body, color = bc)
            }
            Text(botLine, style = AsciiType.body, color = bc,
                maxLines = 1, softWrap = false, overflow = TextOverflow.Clip)
        }
    }
}

@Composable
fun InnerDivider(modifier: Modifier = Modifier) {
    val colors = LocalAsciiColors.current
    Text(
        text = "─".repeat(80), style = AsciiType.small,
        color = colors.border.copy(alpha = 0.4f),
        maxLines = 1, softWrap = false,
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp)
    )
}

@Composable
fun DataRow(
    label: String, value: String,
    valueColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val colors = LocalAsciiColors.current
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 1.dp)) {
        Text(label, style = AsciiType.label, color = colors.textDim,
            modifier = Modifier.weight(0.40f))
        Text(":", style = AsciiType.label, color = colors.border,
            modifier = Modifier.padding(horizontal = 3.dp))
        Text(value, style = AsciiType.value, color = valueColor ?: colors.text,
            modifier = Modifier.weight(0.60f),
            maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
    }
}
