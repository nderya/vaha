package com.kampplus.hava

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kampplus.hava.ui.HavaNavHost
import com.kampplus.hava.ui.theme.AppThemeStyle
import com.kampplus.hava.ui.theme.HavaTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            var currentTheme by remember { mutableStateOf(AppThemeStyle.SYSTEM) }

            HavaTheme(themeStyle = currentTheme) {
                HavaNavHost(onThemeChange = { selectedTheme -> currentTheme = selectedTheme })
            }
        }
    }
}
