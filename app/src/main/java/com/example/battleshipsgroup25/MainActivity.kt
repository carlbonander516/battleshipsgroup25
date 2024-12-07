package com.example.battleshipsgroup25

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.battleshipsgroup25.ui.theme.Battleshipsgroup25Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Battleshipsgroup25Theme {
                val navController = rememberNavController()
                val gameModel = remember { GameModel() }
                gameModel.initListeners()

                NavGraph(navController = navController, model = gameModel)
            }
        }
    }
}
