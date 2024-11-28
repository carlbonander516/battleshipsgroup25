// GameBoard.kt
package com.example.battleshipsgroup25

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.battleshipsgroup25.Ship


@Composable
fun Gameboard(navController: NavHostController) {
    val boardSize = 10
    val shipManager = remember { ShipManager(boardSize) }
    val playerShips = remember { mutableStateListOf<Ship>(*shipManager.placeShips().toTypedArray()) }
    val selectedShip = remember { mutableStateOf<Ship?>(null) }
    val currentOrientation = remember { mutableStateOf("H") } // Default orientation
    val gameStarted = remember { mutableStateOf(false) } // Tracks if the game has started
    val bot = remember { Bot(boardSize) } // Initialize the bot
    val botGridHits = remember { mutableStateListOf<Pair<Int, Int>>() } // Tracks hits on bot's grid

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
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
            // Text or Buttons Area
            if (!gameStarted.value) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            currentOrientation.value = if (currentOrientation.value == "H") "V" else "H"
                        }
                    ) {
                        Text("Toggle Orientation: ${if (currentOrientation.value == "H") "Horizontal" else "Vertical"}")
                    }

                    Button(
                        onClick = { gameStarted.value = true }
                    ) {
                        Text("Start")
                    }
                }
            } else {
                Text(
                    text = "Player 1's Turn!",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Grids
            // Bot's grid for Player 1 to attack
            Grid(
                size = boardSize,
                ships = emptyList(), // Bot's ships are always hidden
                selectedShip = remember { mutableStateOf(null) },
                onCellClick = { row, col ->
                    if (gameStarted.value && Pair(row, col) !in botGridHits) {
                        botGridHits.add(Pair(row, col)) // Mark the clicked cell as hit
                        val hit = RuleEngine.handleCellClick(row, col) // Use RuleEngine to handle the shot
                        if (hit) {
                            println("Player 1 hit a ship at ($row, $col)!")
                        } else {
                            println("Player 1 missed!")
                        }
                    }
                },
                onCellLongClick = { _, _ -> },
                highlights = botGridHits // Highlight cells clicked by the player
            )

            // Player's grid for ship placement
            Grid(
                size = boardSize,
                ships = playerShips,
                selectedShip = selectedShip,
                onCellClick = { row, col ->
                    if (!gameStarted.value) {
                        if (selectedShip.value != null) {
                            val success = shipManager.moveSelectedShip(row, col, currentOrientation.value)
                            if (success) {
                                selectedShip.value = null
                            } else {
                                println("Cannot place ship at this position")
                            }
                        } else {
                            val clickedShip = playerShips.find { ship -> ship.positions.contains(Pair(row, col)) }
                            if (clickedShip != null) {
                                shipManager.selectShip(clickedShip)
                                selectedShip.value = clickedShip
                            }
                        }
                    }
                },
                onCellLongClick = { _, _ -> }
            )
        }
    }
}



@Composable
fun Grid(
    size: Int,
    ships: List<Ship>,
    selectedShip: MutableState<Ship?>,
    onCellClick: (Int, Int) -> Unit,
    onCellLongClick: (Int, Int) -> Unit,
    highlights: List<Pair<Int, Int>> = emptyList() // New parameter to track clicked cells
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until size) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (j in 0 until size) {
                    val isShipTile = ships.any { ship -> Pair(i, j) in ship.positions }
                    val isSelectedTile = selectedShip.value?.positions?.contains(Pair(i, j)) == true
                    val isHit = highlights.contains(Pair(i, j))

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(2.dp, if (isSelectedTile) Color.Red else Color.Black)
                            .background(
                                when {
                                    isHit -> Color.Red // Cell hit by Player 1
                                    isShipTile -> Color.DarkGray // Ship tiles
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { onCellClick(i, j) }
                    )
                }
            }
        }
    }
}






