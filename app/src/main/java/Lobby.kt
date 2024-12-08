package com.example.battleshipsgroup25

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController

@Composable
fun LobbyScreen(navController: NavController, model: GameModel, username: String) {
    val players by model.playerMap.collectAsStateWithLifecycle()
    val games by model.gameMap.collectAsStateWithLifecycle()

    var lobbyName by remember { mutableStateOf("") }
    val localPlayerId = model.localPlayerId.collectAsStateWithLifecycle().value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Welcome, $username!")

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = lobbyName,
            onValueChange = { lobbyName = it },
            label = { Text("Lobby Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                localPlayerId?.let { playerId ->
                    model.createGame(
                        lobbyName, playerId,
                        onSuccess = { gameId ->
                            println("Navigating to GameLobby with ID: $gameId") // Debug log
                            navController.navigate("game/$gameId") // Correct navigation
                        },
                        onError = { error ->
                            println("Error creating game: $error") // Debug log
                        }
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Lobby")
        }



        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(games.entries.toList()) { (gameId, game) ->
                ListItem(
                    headlineContent = { Text(game.name) },
                    supportingContent = { Text("Players: ${game.playerCount}") },
                    trailingContent = {
                        Button(onClick = {
                            localPlayerId?.let { playerId ->
                                model.joinGame(
                                    gameId, playerId,
                                    onSuccess = {
                                        navController.navigate("game/$gameId")
                                    },
                                    onError = { error ->
                                        // Handle error
                                    }
                                )
                            }
                        }) {
                            Text("Join")
                        }
                    }
                )
            }
        }
    }
}

