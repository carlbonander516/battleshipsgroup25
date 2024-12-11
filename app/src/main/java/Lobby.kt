package com.example.battleshipsgroup25

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LobbyScreen(navController: NavController, model: GameModel, username: String, maxPlayers: Int) {
    val games by model.gameMap.collectAsStateWithLifecycle()
    var lobbyName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    FirebaseFirestore.getInstance().collection("test").document("testDoc").set(mapOf("status" to "Connected!"))


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
                val newGameId = model.createGame(lobbyName, username, maxPlayers) { errorMessage ->
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    isLoading = false
                }
                if (newGameId != null) {
                    isLoading = false
                    navController.navigate("game_lobby/$newGameId")
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
                        supportingContent = {
                            Text("Players: ${game.players.size} / $maxPlayers")
                        },
                        trailingContent = {
                            Button(
                                onClick = {
                                    model.joinGame(
                                        gameId = gameId,
                                        username = username,
                                        onError = { errorMessage ->
                                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                                        },
                                        onSuccess = {
                                            navController.navigate("game_lobby/$gameId")
                                        }
                                    )
                                },
                                enabled = game.players.size < maxPlayers
                            ) {
                                Text("Join")
                            }
                        }
                    )
                }
            }
        }
    }
}
