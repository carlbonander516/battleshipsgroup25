// GameBoard.kt
package com.example.battleshipsgroup25

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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


@Composable
fun Gameboard(navController: NavHostController) {
    val boardSize = 10
    val shipManager = remember { ShipManager(boardSize) }
    val ships = remember { mutableStateListOf<Ship>(*shipManager.placeShips().toTypedArray()) } // Place ships only once
    val selectedShip = remember { mutableStateOf<Ship?>(null) } // Track the selected ship

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
            // Text to display the selected ship status
            Text(
                text = "Ships left to place: ${ships.size - ships.count { it.positions.isNotEmpty() }}",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )

            // Top Grid (static for now)
           Grid(
                size = boardSize,
                ships = ships,
                selectedShip = selectedShip,
                onCellClick = { row, col ->
                    // Select a ship if it exists at the clicked position
                    val clickedShip = ships.find { ship -> ship.positions.contains(Pair(row, col)) }
                    if (clickedShip != null) {
                        shipManager.selectShip(clickedShip)
                        selectedShip.value = clickedShip
                    }
                },
                onCellLongClick = { row, col ->
                    // Example: Implement movement here if needed
                }
            )

// Bottom Grid
            Grid(
                size = boardSize,
                ships = ships,
                selectedShip = selectedShip,
                onCellClick = { row, col ->
                    if (selectedShip.value != null) {
                        // Try to move the selected ship to the new position
                        val success = shipManager.moveSelectedShip(row, col, "H") // Example: Default to horizontal
                        if (success) {
                            selectedShip.value = null // Deselect after move
                        } else {
                            // Provide feedback if move fails (optional)
                            println("Cannot place ship at this position")
                        }
                    }
                },
                onCellLongClick = { _, _ -> } // Add a no-op for onCellLongClick to match Grid parameters
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
    onCellLongClick: (Int, Int) -> Unit
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

                    Text(
                        text = "",
                        color = Color.Black,
                        modifier = Modifier
                            .size(32.dp)
                            .border(2.dp, if (isSelectedTile) Color.Red else Color.Black)
                            .background(if (isShipTile) Color.DarkGray else Color.Transparent)
                            .clickable { onCellClick(i, j) },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}





