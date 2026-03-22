package com.example.vocanote.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import com.example.vocanote.features.add.presentation.AddWordPage
import com.example.vocanote.features.search.presentation.SearchPage
import com.example.vocanote.features.words.presentation.WordsPage
import com.example.vocanote.ui.navigation.BottomNavDestination
import com.example.vocanote.ui.navigation.VocaBottomNavigationBar

@Composable
fun VocaNoteApp() {
    var selectedDestination by rememberSaveable { androidx.compose.runtime.mutableStateOf(BottomNavDestination.Words) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            VocaBottomNavigationBar(
                selectedDestination = selectedDestination,
                onDestinationSelected = { selectedDestination = it }
            )
        }
    ) { innerPadding ->
        when (selectedDestination) {
            BottomNavDestination.Words -> WordsPage(modifier = Modifier.padding(innerPadding))
            BottomNavDestination.Add -> AddWordPage(modifier = Modifier.padding(innerPadding))
            BottomNavDestination.Search -> SearchPage(modifier = Modifier.padding(innerPadding))
        }
    }
}
