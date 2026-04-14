package com.example.vocanote.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.vocanote.ui.theme.BottomBarBackground
import com.example.vocanote.ui.theme.BottomBarBorder
import com.example.vocanote.ui.theme.BottomBarInactive
import com.example.vocanote.ui.theme.PrimaryBlue

@Composable
fun VocaBottomNavigationBar(
    selectedDestination: BottomNavDestination,
    onDestinationSelected: (BottomNavDestination) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.systemBars)
            .background(BottomBarBackground)
            .drawBehind {
                drawLine(
                    color = BottomBarBorder,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp)
                .background(BottomBarBackground),
            containerColor = BottomBarBackground,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            BottomNavDestination.entries.forEach { destination ->
                NavigationBarItem(
                    modifier = Modifier.fillMaxHeight(),
                    selected = selectedDestination == destination,
                    onClick = { onDestinationSelected(destination) },
                    icon = {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.label
                        )
                    },
                    label = { Text(destination.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        selectedTextColor = PrimaryBlue,
                        indicatorColor = PrimaryBlue.copy(alpha = 0.12f),
                        unselectedIconColor = BottomBarInactive,
                        unselectedTextColor = BottomBarInactive
                    )
                )
            }
        }
    }
}
