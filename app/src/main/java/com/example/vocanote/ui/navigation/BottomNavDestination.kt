package com.example.vocanote.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavDestination(
    val label: String,
    val icon: ImageVector
) {
    Words("단어장", Icons.AutoMirrored.Filled.MenuBook),
    Add("등록", Icons.Filled.AddCircle),
    Search("검색", Icons.Filled.Search),
}
