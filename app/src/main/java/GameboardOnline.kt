package com.example.battleshipsgroup25

import Grid
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.google.firebase.firestore.FirebaseFirestore

// Function to parse ships from Firebase data
fun parseShipsFromData(shipsData: Map<String, List<String>>?): List<Ship> {
    return shipsData?.map { (name, positions) ->
        val parsedPositions = positions.map { position ->
            val coords = position.split(",")
            Pair(coords[0].toInt(), coords[1].toInt())
        }
        Ship(name, parsedPositions.size, parsedPositions)
    } ?: emptyList()
}



@Composable
fun GameboardOnline(
    navController: NavHostController,
    gameId: String,
    playerId: String,
    model: GameModel = viewModel()
) {
    val firestore = FirebaseFirestore.getInstance()
    val gameRef = firestore.collection("games").document(gameId)
    gameRef.addSnapshotListener { document, e ->
        if (e != null) {
            Log.e("GameboardOnline", "Error loading game data: ${e.message}")
            return@addSnapshotListener
        }
        val gameData = document?.data ?: emptyMap<String, Any>()
        // handle gameData
    }


    val turn = remember { mutableStateOf("player1") }
    val gameOver = remember { mutableStateOf(false) }
    val winner = remember { mutableStateOf("") }
    val playerGridHits = remember { mutableStateListOf<Pair<Int, Int>>() }
    val playerGridMisses = remember { mutableStateListOf<Pair<Int, Int>>() }
    val opponentGridHits = remember { mutableStateListOf<Pair<Int, Int>>() }
    val opponentGridMisses = remember { mutableStateListOf<Pair<Int, Int>>() }
    val opponentId = if (playerId == "player1") "player2" else "player1"

    val gameData = remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }


    DisposableEffect(gameId) {
        val listener = gameRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.e("GameboardOnline", "Error loading game data: ${e.message}")
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data ?: emptyMap<String, Any?>()
                gameData.value = data
                turn.value = data["turn"] as? String ?: "player1"
                gameOver.value = data["gameOver"] as? Boolean ?: false
                winner.value = data["winner"] as? String ?: ""
            } else {
                gameData.value = emptyMap()
            }
        }

        onDispose { listener.remove() }
    }

/*
    fun checkWinCondition() {
        val opponentData = (gameData.value["players"] as? Map<*, *>)?.get(opponentId) as? Map<*, *>
        val shipsData = opponentData?.get("ships") as? Map<String, List<String>>
        val allShipCells = shipsData?.values?.flatten()?.toSet() ?: emptySet()
        val allHits = (gameData.value["hits"] as? Map<*, *>)?.get(playerId) as? List<*>
        val hitCells = allHits?.map { it.toString() } ?: emptyList()

        if (allShipCells.all { it in hitCells }) {
            gameRef.child("gameOver").setValue(true)
            gameRef.child("winner").setValue(playerId)
        }
    }
*/
    fun updateTurn() {
        val nextTurn = if (turn.value == "player1") "player2" else "player1"
        gameRef.update("turn", nextTurn)
    }


    Log.d("GameboardOnline", "Full Game Data: ${gameData.value}")


    fun handleCellClick(row: Int, col: Int, turn: String, playerId: String, gameOver: Boolean, gameRef: DatabaseReference, opponentGridHits: MutableList<Pair<Int, Int>>, opponentGridMisses: MutableList<Pair<Int, Int>>) {
        if (turn != playerId || gameOver) return

        val cellKey = sanitizeKey("$row,$col")

        gameRef.child("hits/$playerId").push().setValue(cellKey).addOnSuccessListener {
            if (true) { // Replace with actual hit condition
                opponentGridHits.add(Pair(row, col))
            } else {
                opponentGridMisses.add(Pair(row, col))
            }
        }.addOnFailureListener {
            Log.e("handleCellClick", "Error updating cell click: ${it.message}")
        }
    }



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

                val rawPlayerData = (gameData.value["players"] as? Map<*, *>)?.get(playerId)
                Log.d("GameboardOnline", "Raw Player Data: $rawPlayerData")

                val playerShipsData = (rawPlayerData as? Map<*, *>)?.get("ships") as? Map<String, List<String>>
                Log.d("GameboardOnline", "Player Ships Data: $playerShipsData")

                val playerShips = parseShipsFromData(playerShipsData)
                Log.d("GameboardOnline", "Parsed Player Ships: $playerShips")


                val rawOpponentData = (gameData.value["players"] as? Map<*, *>)?.get(opponentId)
                Log.d("GameboardOnline", "Raw Opponent Data: $rawOpponentData")

                val opponentShipsData = (rawOpponentData as? Map<*, *>)?.get("ships") as? Map<String, List<String>>
                Log.d("GameboardOnline", "Opponent Ships Data: $opponentShipsData")

                val opponentShips = parseShipsFromData(opponentShipsData)
                Log.d("GameboardOnline", "Parsed Opponent Ships: $opponentShips")


                Grid(
                    size = 10,
                    ships = opponentShips,
                    selectedShip = remember { mutableStateOf(null) },
                    onCellClick = { _, _ -> },
                    onCellLongClick = { _, _ -> },
                    highlights = opponentGridHits,
                    misses = opponentGridMisses,
                    gameStarted = false
                )
                Log.d("GameboardOnline", "Ships Passed to Grid: $opponentShips")

                Grid(
                    size = 10,
                    ships = playerShips,
                    selectedShip = remember { mutableStateOf(null) },
                    onCellClick = { _, _ -> },
                    onCellLongClick = { _, _ -> },
                    highlights = playerGridHits,
                    misses = playerGridMisses,
                    gameStarted = false
                )
                Log.d("GameboardOnline", "Ships Passed to Grid: $playerShips")
            }
        }
    }
}
