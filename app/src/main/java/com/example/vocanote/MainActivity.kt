package com.example.vocanote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.vocanote.ui.VocaNoteApp
import com.example.vocanote.ui.theme.VocaNoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VocaNoteTheme {
                VocaNoteApp()
            }
        }
    }
}
