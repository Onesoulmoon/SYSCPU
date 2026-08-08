package com.necrosed.asciisysinfo.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.necrosed.asciisysinfo.ui.components.*
import com.necrosed.asciisysinfo.ui.theme.AsciiType
import com.necrosed.asciisysinfo.ui.theme.LocalAsciiColors

@Composable
fun AboutScreen() {
    val colors = LocalAsciiColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            AsciiPanel(title = "ASCII SYS") {
                Text("Android System Monitor", style = AsciiType.small, color = colors.textDim)
                Spacer(Modifier.height(6.dp))
                DataRow("VERSION", "1.0.0")
                InnerDivider()
                DataRow("BUILT WITH", "Kotlin")
                DataRow("",           "Jetpack Compose")
            }
        }

        item {
            AsciiPanel(title = "SOURCE") {
                // TODO: swap in your actual GitHub repo URL — left unfilled
                // deliberately rather than fabricated.
                DataRow("GITHUB", "— add repo URL —", colors.textDim)
            }
        }

        item {
            AsciiPanel(title = "ABOUT") {
                Text(
                    "A terminal-inspired system diagnostic tool for Android,\n" +
                    "styled after btop++. Built as a portfolio piece — no root\n" +
                    "required, no data leaves the device.",
                    style = AsciiType.small, color = colors.text
                )
            }
        }
    }
}
