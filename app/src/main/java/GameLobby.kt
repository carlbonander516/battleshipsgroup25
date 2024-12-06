package com.example.battleshipsgroup25

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun GameLobby(
    navController: NavController,
    gameId: String,
    username: String
) {
    var gameName by remember { mutableStateOf("") }
    var playerName by remember { mutableStateOf("") }

    // Logic for loading the game data from Firebase could be added here (e.g., listen for the second player to join)
    LaunchedEffect(gameId) {
        // Fetch game info, like other players, from Firebase
        // If player 2 joins, update the UI accordingly.
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display Username
        Text(text = "Username: $username")

        Spacer(modifier = Modifier.height(16.dp))

        // VS Section showing the current state
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$username vs")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Waiting for player")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Optional: Show some kind of "ready" or "waiting" button
        Button(
            onClick = {
                // TODO: Update game state and notify players when the second player joins
                Toast.makeText(navController.context, "Waiting for another player...", Toast.LENGTH_SHORT).show()
            }
        ) {
            Text("Start")
        }
    }
}
