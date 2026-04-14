package com.example.vocanote.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavDestination(
    val label: String,
    val icon: ImageVector
) {
    Words("단어장", Icons.AutoMirrored.Filled.MenuBook),
    List("리스트", Icons.AutoMirrored.Filled.FormatListBulleted),
    Search("복습", Icons.Filled.Bolt),
    Profile("마이", Icons.Filled.Person),
}
