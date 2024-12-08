package com.example.battleshipsgroup25

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun GameLobby(
    navController: NavController,
    gameId: String,
    model: GameModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Welcome to the Game Lobby!")
        Text(text = "Game ID: $gameId")
        Button(onClick = { navController.popBackStack() }) {
            Text("Back to Lobby")
        }
    }
}
