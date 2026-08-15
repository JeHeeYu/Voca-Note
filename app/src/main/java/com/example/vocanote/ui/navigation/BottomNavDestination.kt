package com.example.vocanote.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Home("home", "오늘", Icons.Default.Home),
    Library("library", "단어장", Icons.AutoMirrored.Filled.MenuBook),
    Review("review", "연습", Icons.Default.School),
    Settings("settings", "내 학습", Icons.Default.Person)
}
