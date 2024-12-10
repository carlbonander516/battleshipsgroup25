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
import com.google.firebase.database.FirebaseDatabase

@Composable
fun LobbyScreen(navController: NavController, model: GameModel, username: String, maxPlayers: Int) {
    val players by model.playerMap.collectAsStateWithLifecycle()
    val games by model.gameMap.collectAsStateWithLifecycle()
    val database = FirebaseDatabase.getInstance().reference.child("games")
    var lobbyName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
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
                isLoading = true
                val newGameId = database.push().key.orEmpty()

                if (lobbyName.isBlank()) {
                    println("Lobby name cannot be empty!")
                    isLoading = false
                    return@Button
                }

                val newGameData = mapOf(
                    "name" to lobbyName,
                    "host" to username,
                    "status" to "waiting",
                    "players" to mapOf(username to true) // Add the creator to the players list
                )

                database.child(newGameId).setValue(newGameData)
                    .addOnSuccessListener {
                        println("Game created successfully with ID: $newGameId")
                        isLoading = false
                        navController.navigate("game/$newGameId")
                    }
                    .addOnFailureListener { error ->
                        println("Error creating game: ${error.message}")
                        isLoading = false
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Creating..." else "Create Lobby")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (games.isEmpty()) {
            Text("No games available. Create a lobby to get started!")
        } else {
            Text("Available Games")
            LazyColumn {
                items(games.entries.toList()) { (gameId, game) ->
                    ListItem(
                        headlineContent = { Text(game.name) },
                        supportingContent = { Text("Players: ${game.playerCount}/$maxPlayers") },
                        trailingContent = {
                            Button(
                                onClick = {
                                    isLoading = true
                                    model.joinLobby(gameId, username) { success ->
                                        isLoading = false
                                        if (success) {
                                            navController.navigate("game/$gameId")
                                        } else {
                                            println("Failed to join lobby. Lobby might be full or an error occurred.")
                                            // Optionally, show a Snackbar or other UI feedback here
                                        }
                                    }
                                }
                            ) {
                                Text(if (isLoading) "Joining..." else "Join")
                            }
                        }
                    )
                }
            }

        }
    }
}
