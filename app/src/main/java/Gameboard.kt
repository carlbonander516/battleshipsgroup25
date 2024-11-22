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


@Composable
fun Gameboard(navController: NavHostController) {
    val boardSize = 10
    val shipManager = remember { ShipManager(boardSize) }
    val playerShips = remember { mutableStateListOf<Ship>(*shipManager.placeShips().toTypedArray()) }
    val selectedShip = remember { mutableStateOf<Ship?>(null) }
    val currentOrientation = remember { mutableStateOf("H") } // Default orientation

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.backgroundgame),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f), // Restore your original alpha or remove it if not needed
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Restore original padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ships left to place: ${playerShips.size - playerShips.count { it.positions.isNotEmpty() }}",
                color = Color.White, // Keep your preferred color
                style = MaterialTheme.typography.bodyLarge
            )

            // Orientation Toggle Button
            Button(
                onClick = {
                    currentOrientation.value = if (currentOrientation.value == "H") "V" else "H"
                },
                modifier = Modifier.padding(8.dp) // Restore button placement as needed
            ) {
                Text("Toggle Orientation: ${if (currentOrientation.value == "H") "Horizontal" else "Vertical"}")
            }

            // Grids (unchanged)
            Grid(
                size = boardSize,
                ships = emptyList(),
                selectedShip = remember { mutableStateOf(null) },
                onCellClick = { row, col ->
                    println("Shot fired at bot's grid: ($row, $col)")
                },
                onCellLongClick = { _, _ -> }
            )
            Grid(
                size = boardSize,
                ships = playerShips,
                selectedShip = selectedShip,
                onCellClick = { row, col ->
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





