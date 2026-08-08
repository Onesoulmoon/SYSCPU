package com.necrosed.asciisysinfo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors

@Composable
fun TimeGraph(
    history:   List<Float>,
    min:       Float = 0f,
    max:       Float = 100f,
    lineColor: Color? = null,
    unit:      String = "%",
    title:     String = "",
    height:    Int    = 60,
    modifier:  Modifier = Modifier
) {
    val colors = LocalAsciiColors.current
    val lc     = lineColor ?: colors.cpu
    val fill   = lc.copy(alpha = 0.15f)
    val grid   = lc.copy(alpha = 0.08f)

    Column(modifier = modifier.fillMaxWidth()) {
        if (title.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, style = AsciiType.small, color = colors.textDim)
                if (history.isNotEmpty())
                    Text("%.1f%s".format(history.last(), unit),
                        style = AsciiType.small, color = lc)
            }
            Spacer(Modifier.height(2.dp))
        }

        Canvas(modifier = Modifier.fillMaxWidth().height(height.dp)) {
            val w = size.width; val h = size.height
            val range = (max - min).coerceAtLeast(0.001f)
            for (i in 1..3) {
                val y = h - (i / 4f) * h
                drawLine(grid, Offset(0f, y), Offset(w, y), 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 4f)))
            }
            if (history.size >= 2) {
                val step = w / (history.size - 1).toFloat()
                val line = Path(); val fp = Path()
                history.forEachIndexed { i, v ->
                    val x = i * step
                    val y = h - ((v.coerceIn(min, max) - min) / range) * h
                    if (i == 0) { line.moveTo(x, y); fp.moveTo(x, h); fp.lineTo(x, y) }
                    else        { line.lineTo(x, y); fp.lineTo(x, y) }
                }
                fp.lineTo((history.size - 1) * step, h); fp.close()
                drawPath(fp,   color = fill, style = Fill)
                drawPath(line, color = lc,   style = Stroke(2.5f, join = StrokeJoin.Round))
                val ex = (history.size - 1) * step
                val ey = h - ((history.last().coerceIn(min, max) - min) / range) * h
                drawCircle(lc, 4f, Offset(ex, ey))
                drawCircle(colors.background, 2f, Offset(ex, ey))
            }
        }
    }
}
