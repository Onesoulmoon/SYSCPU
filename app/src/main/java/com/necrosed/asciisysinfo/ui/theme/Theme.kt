package com.necrosed.asciisysinfo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class TerminalTheme { BTOP, PAPER, CRT }

data class AsciiColors(
    val background: Color, val surface:   Color,
    val border:     Color, val borderHi:  Color,
    val text:       Color, val textDim:   Color,
    val cpu:        Color, val cpuDim:    Color,
    val memUsed:    Color, val memCache:  Color, val memFree: Color,
    val netDl:      Color, val netUl:     Color,
    val diskUsed:   Color, val diskFree:  Color,
    val proc:       Color,
    val red:        Color, val green:     Color,
    val yellow:     Color, val blue:      Color,
    val orange:     Color, val accent:    Color,
)

val btopColors = AsciiColors(
    background = BtopTheme.Background, surface   = BtopTheme.Surface,
    border     = BtopTheme.Border,     borderHi  = BtopTheme.BorderHi,
    text       = BtopTheme.Text,       textDim   = BtopTheme.TextDim,
    cpu        = BtopTheme.Cpu,        cpuDim    = BtopTheme.CpuDim,
    memUsed    = BtopTheme.MemUsed,    memCache  = BtopTheme.MemCache,
    memFree    = BtopTheme.MemFree,
    netDl      = BtopTheme.NetDl,      netUl     = BtopTheme.NetUl,
    diskUsed   = BtopTheme.DiskUsed,   diskFree  = BtopTheme.DiskFree,
    proc       = BtopTheme.Proc,
    red        = BtopTheme.Red,        green     = BtopTheme.Green,
    yellow     = BtopTheme.Yellow,     blue      = BtopTheme.Blue,
    orange     = BtopTheme.Orange,     accent    = BtopTheme.Accent,
)

val paperColors = AsciiColors(
    background = PaperTheme.Background, surface   = PaperTheme.Surface,
    border     = PaperTheme.Border,     borderHi  = PaperTheme.BorderHi,
    text       = PaperTheme.Text,       textDim   = PaperTheme.TextDim,
    cpu        = PaperTheme.Cpu,        cpuDim    = PaperTheme.CpuDim,
    memUsed    = PaperTheme.MemUsed,    memCache  = PaperTheme.MemCache,
    memFree    = PaperTheme.MemFree,
    netDl      = PaperTheme.NetDl,      netUl     = PaperTheme.NetUl,
    diskUsed   = PaperTheme.DiskUsed,   diskFree  = PaperTheme.DiskFree,
    proc       = PaperTheme.Proc,
    red        = PaperTheme.Red,        green     = PaperTheme.Green,
    yellow     = PaperTheme.Yellow,     blue      = PaperTheme.Blue,
    orange     = PaperTheme.Orange,     accent    = PaperTheme.Accent,
)

val crtColors = AsciiColors(
    background = CrtTheme.Background, surface   = CrtTheme.Surface,
    border     = CrtTheme.Border,     borderHi  = CrtTheme.BorderHi,
    text       = CrtTheme.Text,       textDim   = CrtTheme.TextDim,
    cpu        = CrtTheme.Cpu,        cpuDim    = CrtTheme.CpuDim,
    memUsed    = CrtTheme.MemUsed,    memCache  = CrtTheme.MemCache,
    memFree    = CrtTheme.MemFree,
    netDl      = CrtTheme.NetDl,      netUl     = CrtTheme.NetUl,
    diskUsed   = CrtTheme.DiskUsed,   diskFree  = CrtTheme.DiskFree,
    proc       = CrtTheme.Proc,
    red        = CrtTheme.Red,        green     = CrtTheme.Green,
    yellow     = CrtTheme.Yellow,     blue      = CrtTheme.Blue,
    orange     = CrtTheme.Orange,     accent    = CrtTheme.Accent,
)

val LocalAsciiColors = staticCompositionLocalOf { btopColors }

@Composable
fun AsciiSysTheme(theme: TerminalTheme = TerminalTheme.BTOP, content: @Composable () -> Unit) {
    val colors = when (theme) {
        TerminalTheme.BTOP  -> btopColors
        TerminalTheme.PAPER -> paperColors
        TerminalTheme.CRT   -> crtColors
    }
    CompositionLocalProvider(LocalAsciiColors provides colors) { content() }
}
