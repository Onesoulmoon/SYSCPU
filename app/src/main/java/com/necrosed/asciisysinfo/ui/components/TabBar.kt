package com.necrosed.asciisysinfo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors
import com.necrosed.asciisysinfo.viewmodel.AppTab

private val TAB_LABELS = mapOf(
    AppTab.OVERVIEW to "OVERVIEW",
    AppTab.SOC      to "SOC",
    AppTab.DEVICE   to "DEVICE",
    AppTab.SYSTEM   to "SYSTEM",
    AppTab.BATTERY  to "BATTERY",
    AppTab.THERMAL  to "THERMAL",
    AppTab.SENSORS  to "SENSORS",
    AppTab.NETWORK  to "NETWORK",
    AppTab.ABOUT    to "ABOUT"
)

@Composable
fun TabBar(active: AppTab, onSelect: (AppTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = LocalAsciiColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.surface)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 2.dp)
    ) {
        AppTab.entries.forEach { tab ->
            val isActive = tab == active
            Text(
                text     = if (isActive) "[${TAB_LABELS[tab]}]" else " ${TAB_LABELS[tab]} ",
                style    = AsciiType.tab,
                color    = if (isActive) colors.background else colors.textDim,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier
                    .background(if (isActive) colors.borderHi else colors.surface)
                    .clickable { onSelect(tab) }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
            )
        }
    }
}
