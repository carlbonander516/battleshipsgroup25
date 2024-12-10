package com.example.battleshipsgroup25

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase

@Composable
fun GameLobbyScreen(navController: NavController, gameId: String, model: GameModel, username: String) {
    val games by model.gameMap.collectAsStateWithLifecycle()
    val gameDetails = games[gameId]

    if (gameDetails != null) {
        val isHost = username == gameDetails.host

        // Monitor the game's status and navigate when it changes to "started"
        LaunchedEffect(gameDetails.status) {
            if (gameDetails.status == "started") {
                navController.navigate("gameboard_online/$gameId")
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text("Welcome to the Game Lobby!")
            Text("Game ID: $gameId")
            Text("Players in lobby: ${gameDetails.players.size}")

            Spacer(modifier = Modifier.height(16.dp))

            gameDetails.players.keys.forEach { player ->
                Text(player)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "Start Game" button for the host
            if (isHost) {
                Button(
                    onClick = {
                        Log.d("GameLobbyScreen", "Host $username started the game.")
                        FirebaseDatabase.getInstance().reference
                            .child("games").child(gameId)
                            .child("status").setValue("started")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Game")
                }
            } else {
                Text("Waiting for the host to start the game...")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terminate Lobby Button
            Button(
                onClick = {
                    model.terminateGame(gameId)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Leave Lobby")
            }
        }
    } else {
        // Show loading message while data is being fetched
        Text("Loading lobby details...")
    }
}
