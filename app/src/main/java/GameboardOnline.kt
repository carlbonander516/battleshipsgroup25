
package com.example.battleshipsgroup25

import Grid
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.firebase.database.*

@Composable
fun GameboardOnline(navController: NavHostController, gameId: String, playerId: String, model: GameModel = viewModel()) {
    Log.d("GameboardOnline", "Initialized with gameId: $gameId and playerId: $playerId")
    val database = FirebaseDatabase.getInstance().reference
    val gameRef = database.child("games").child(gameId)

    val turn = remember { mutableStateOf("Player1") }
    val gameOver = remember { mutableStateOf(false) }
    val winner = remember { mutableStateOf("") }
    val playerGridHits = remember { mutableStateListOf<Pair<Int, Int>>() }
    val playerGridMisses = remember { mutableStateListOf<Pair<Int, Int>>() }
    val opponentGridHits = remember { mutableStateListOf<Pair<Int, Int>>() }
    val opponentGridMisses = remember { mutableStateListOf<Pair<Int, Int>>() }

    val gameData = remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }

// Listen for game state changes
    DisposableEffect(gameId) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                if (value is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    gameData.value = value as Map<String, Any?>
                    turn.value = gameData.value["turn"] as? String ?: "Player1"
                    gameOver.value = gameData.value["gameOver"] as? Boolean ?: false
                    winner.value = gameData.value["winner"] as? String ?: ""
                } else {
                    gameData.value = emptyMap()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GameboardOnline", "Error loading game data: ${error.message}")
            }
        }
        gameRef.addValueEventListener(listener)
        onDispose { gameRef.removeEventListener(listener) }
    }


    fun handleOnlineHit(row: Int, col: Int): Boolean {
        // Simulate fetching opponent ships from the game state
        val opponentShips = (gameData.value["opponentShips"] as? List<*>)?.filterIsInstance<Ship>() ?: return false
        val hit = opponentShips.any { ship -> Pair(row, col) in ship.positions }

        // Update the online game state
        val updates = mutableMapOf<String, Any?>()
        if (hit) {
            updates["opponentHits"] = opponentGridHits + Pair(row, col)
        } else {
            updates["opponentMisses"] = opponentGridMisses + Pair(row, col)
        }
        gameRef.updateChildren(updates) // `gameRef` is the reference to the Firebase database node for this game.

        return hit
    }
    fun checkWinCondition(hits: List<Pair<Int, Int>>): Boolean {
        val opponentShips = (gameData.value["opponentShips"] as? List<*>)?.filterIsInstance<Ship>() ?: return false
        val totalShipCells = opponentShips.flatMap { it.positions }.toSet()
        return totalShipCells.all { it in hits }
    }



    // Render UI based on gameData
    if (gameOver.value) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Game Over! Winner: ${winner.value}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = { navController.navigate("mainMenu") }) {
                    Text("Return to Main Menu")
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.backgroundgame),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Turn: ${turn.value}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )

                Grid(
                    size = 10, // Board size
                    ships = (gameData.value["opponentShips"] as? List<*>)?.filterIsInstance<Ship>() ?: emptyList(),
                    selectedShip = remember { mutableStateOf(null) },
                    onCellClick = { row, col ->
                        if (!gameOver.value && turn.value == playerId) {
                            val isHit = handleOnlineHit(row, col)
                            if (isHit) {
                                opponentGridHits.add(Pair(row, col))
                                if (checkWinCondition(opponentGridHits)) {
                                    gameRef.child("gameOver").setValue(true)
                                    gameRef.child("winner").setValue(playerId)
                                }
                            } else {
                                opponentGridMisses.add(Pair(row, col))
                                turn.value = if (playerId == "Player1") "Player2" else "Player1"
                                gameRef.child("turn").setValue(turn.value)
                            }
                        }
                    },
                    onCellLongClick = { _, _ -> },
                    highlights = opponentGridHits,
                    misses = opponentGridMisses,
                    gameStarted = true
                )


                Grid(
                    size = 10, // Board size
                    ships = (gameData.value["opponentShips"] as? List<*>)?.filterIsInstance<Ship>() ?: emptyList(),
                    selectedShip = remember { mutableStateOf(null) },
                    onCellClick = { _, _ -> },
                    onCellLongClick = { _, _ -> },
                    highlights = playerGridHits,
                    misses = playerGridMisses,
                    gameStarted = true
                )
            }
        }
    }
}
