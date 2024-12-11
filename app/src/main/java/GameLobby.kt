package com.example.battleshipsgroup25

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.Logger


fun createLobby(
    gameId: String,
    lobbyName: String,
    username: String,
    playerId: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    FirebaseDatabase.getInstance().reference.child("test").setValue("Connected!")
        .addOnSuccessListener {
            Log.d("FirebaseTest", "Database connection successful!")
        }
        .addOnFailureListener { error ->
            Log.e("FirebaseTest", "Database connection failed: ${error.message}")
        }


    val sanitizedGameId = sanitizeKey(gameId)
    val sanitizedLobbyName = sanitizeKey(lobbyName)
    val gameRef = FirebaseDatabase.getInstance().reference.child("games").child(sanitizedGameId)

    val newGameData = mapOf(
        "name" to sanitizedLobbyName,
        "host" to username,
        "status" to "waiting",
        "players" to mapOf(playerId to true)
    )

    gameRef.setValue(newGameData)
        .addOnSuccessListener {
            Log.d("Firebase", "Lobby created successfully: $sanitizedGameId")
            onSuccess()
        }
        .addOnFailureListener { error ->
            val errorMessage = "Failed to create lobby: ${error.message}"
            Log.e("Firebase", errorMessage)
            onError(errorMessage)
        }

}

@Composable
fun GameLobbyScreen(
    navController: NavController,
    gameId: String,
    model: GameModel,
    username: String,
    playerId: String
) {
    var lobbyName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = lobbyName,
            onValueChange = { lobbyName = it },
            label = { Text("Lobby Name") },
            modifier = Modifier.fillMaxWidth()
        )


        FirebaseDatabase.getInstance().reference.child("test").setValue("Connected!")
            .addOnSuccessListener {
                Log.d("FirebaseTest", "Connection successful!")
            }
            .addOnFailureListener { error ->
                Log.e("FirebaseTest", "Connection failed: ${error.message}")
            }

        Button(
            onClick = {
                if (lobbyName.isBlank()) {
                    Log.e("GameLobbyScreen", "Lobby name is empty!")

                    return@Button
                }

                isLoading = true

                createLobby(
                    gameId = gameId,
                    lobbyName = lobbyName,
                    username = username,
                    playerId = playerId,
                    onSuccess = {
                        isLoading = false
                        navController.navigate("game_lobby/${sanitizeKey(gameId)}")
                    },
                    onError = { errorMessage ->
                        isLoading = false
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Creating..." else "Create Lobby")
        }
    }
}



