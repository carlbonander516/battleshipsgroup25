package com.example.battleshipsgroup25

import GameModes
import IntroScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.battleshipsgroup25.ui.theme.Battleshipsgroup25Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Battleshipsgroup25Theme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "intro"
                ) {
                    composable("intro") { IntroScreen(navController) }
                    composable("game_modes") { GameModes(navController) }
                }
            }
        }
    }
}
