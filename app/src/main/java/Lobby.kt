package com.example.battleshipsgroup25

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.database.*

@Composable
fun Lobby(navController: NavController) {
    val context = LocalContext.current
    val database = Firebase.database.reference.child("games")
    val gameList = remember { mutableStateListOf<Game>() }

    // Variables for user input
    var username by remember { mutableStateOf("") }
    var lobbyName by remember { mutableStateOf("") }
    var isCreatingGame by remember { mutableStateOf(false) }

    // Load games from Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gameList.clear()
                for (gameSnapshot in snapshot.children) {
                    val game = gameSnapshot.getValue(Game::class.java)
                    if (game != null) {
                        gameList.add(game)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load games: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Create Game Button
        Button(
            onClick = {
                isCreatingGame = true // Show input fields for creating a game
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Game")
        }

        // Show Username and Lobby Name inputs when creating a game
        if (isCreatingGame) {
            Spacer(modifier = Modifier.height(16.dp))

            // Username input field
            TextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            // Lobby Name input field
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = lobbyName,
                onValueChange = { lobbyName = it },
                label = { Text("Name of Lobby") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (username.isNotEmpty() && lobbyName.isNotEmpty()) {
                        val gameId = database.push().key ?: return@Button
                        val newGame = Game(id = gameId, name = lobbyName, playerCount = 1)
                        database.child(gameId).setValue(newGame).addOnSuccessListener {
                            Toast.makeText(context, "Game Created!", Toast.LENGTH_SHORT).show()
                            // Navigate to GameLobby screen after creation
                            navController.navigate("game_lobby/$gameId/$username")
                        }.addOnFailureListener {
                            Toast.makeText(context, "Failed to create game.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Please enter both username and lobby name.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display list of existing games
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(gameList) { game ->
                GameItem(game = game, onJoinGame = { gameId ->
                    // Handle game join logic here
                    println("Joining game: $gameId")
                })
            }
        }
    }
}

@Composable
fun GameItem(game: Game, onJoinGame: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = game.name)
        Button(onClick = { onJoinGame(game.id) }) {
            Text("Join")
        }
    }
}
