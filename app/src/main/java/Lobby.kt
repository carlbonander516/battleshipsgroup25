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
fun LobbyScreen(navController: NavController, model: GameModel, username: String) {
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
                val newGameData = mapOf(
                    "host" to username,
                    "status" to "waiting"
                )
                database.child(newGameId).setValue(newGameData)
                    .addOnSuccessListener {
                        isLoading = false
                        navController.navigate("game/$newGameId")
                    }
                    .addOnFailureListener { error ->
                        isLoading = false
                        println("Error creating game: ${error.message}")
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading // Disable button while loading
        ) {
            Text(if (isLoading) "Creating..." else "Create Server")
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

