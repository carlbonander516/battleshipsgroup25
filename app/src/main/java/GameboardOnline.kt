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

// Function to parse ships from Firebase data
fun parseShipsFromData(shipsData: Map<String, List<String>>?): List<Ship> {
    Log.d("parseShipsFromData", "Input Ships Data: $shipsData")
    return shipsData?.map { (name, positions) ->
        val parsedPositions = positions.map { position ->
            val coords = position.split(",")
            Pair(coords[0].toInt(), coords[1].toInt())
        }
        Log.d("parseShipsFromData", "Parsed Ship: $name with positions $parsedPositions")
        Ship(name = name, length = parsedPositions.size, positions = parsedPositions)
    } ?: emptyList()
}


@Composable
fun GameboardOnline(
    navController: NavHostController,
    gameId: String,
    playerId: String,
    model: GameModel = viewModel()
) {
    val database = FirebaseDatabase.getInstance().reference
    val gameRef = database.child("games").child(gameId)

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
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val value = snapshot.value
                if (value is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    gameData.value = value as Map<String, Any?>
                    turn.value = gameData.value["turn"] as? String ?: "player1"
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

    fun updateTurn() {
        val nextTurn = if (turn.value == "player1") "player2" else "player1"
        gameRef.child("turn").setValue(nextTurn)
    }

    Log.d("GameboardOnline", "Full Game Data: ${gameData.value}")


    fun handleCellClick(row: Int, col: Int) {
        if (turn.value != playerId || gameOver.value) return

        val cellKey = "$row,$col"
        val opponentData = (gameData.value["players"] as? Map<*, *>)?.get(opponentId) as? Map<*, *>
        val shipsData = opponentData?.get("ships") as? Map<String, List<String>>
        val hit = shipsData?.values?.flatten()?.contains(cellKey) == true

        val updatePath = if (hit) "hits/$playerId" else "misses/$playerId"
        gameRef.child(updatePath).push().setValue(cellKey).addOnSuccessListener {
            if (hit) {
                opponentGridHits.add(Pair(row, col))
                checkWinCondition()
            } else {
                opponentGridMisses.add(Pair(row, col))
            }
            updateTurn()
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
                    onCellClick = { row, col -> handleCellClick(row, col) },
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
