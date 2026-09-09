package com.raila.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private lateinit var repository: TrainRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        repository = TrainRepository(applicationContext)
        repository.checkForDatabaseUpdate()

        setContent {
            var themeColor by remember { mutableStateOf(repository.savedColorTheme()) }
            RailaTheme(themeColor = themeColor) {
                RailaApp(
                    repository = repository,
                    currentColorTheme = themeColor,
                    onColorThemeChange = { newTheme ->
                        themeColor = newTheme
                        repository.saveColorTheme(newTheme)
                    }
                )
            }
        }
    }
}
