package com.example.battleshipsgroup25

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun GameLobby(
    navController: NavController,
    gameId: String,
    model: GameModel
) {
    val game by model.gameMap.collectAsStateWithLifecycle()
    val gameDetails = game[gameId]
    val localPlayerId = model.localPlayerId.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Welcome to the Game Lobby!")
        Text(text = "Game ID: $gameId")

        Spacer(modifier = Modifier.height(16.dp))

        Text("Players in lobby: ${gameDetails?.playerCount ?: 1}")

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                model.terminateGame(gameId)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Terminate Lobby")
        }

        if ((gameDetails?.playerCount ?: 0) >= 2 && gameDetails?.players?.contains(localPlayerId) == true) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    navController.navigate("game_board/$gameId")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start")
            }
        }
    }
}
