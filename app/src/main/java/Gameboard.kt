// GameBoard.kt
package com.example.battleshipsgroup25

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun Gameboard(navController: NavHostController) {
    // Create an instance of ShipManager
    val boardSize = 10
    val shipManager = remember { ShipManager(boardSize) } // Remember keeps it consistent across recompositions
    val ships = shipManager.placeShips() // Place ships on the board

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image filling the entire screen with transparency
        Image(
            painter = painterResource(id = R.drawable.backgroundgame),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.5f), // 50% transparency
            contentScale = ContentScale.Crop // Fills screen without borders
        )

        // Main Column to hold both grids and ship info
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Add some padding if necessary
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // Space out the two grids evenly
        ) {
            // Text to display number of ships left to place
            Text(
                text = "Ships left to place: ${ships.size}",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
                )

            // Top Grid
            Grid(size = boardSize)

            // Bottom Grid
            Grid(size = boardSize)
        }
    }
}

@Composable
fun Grid(size: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until size) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (j in 0 until size) {
                    Text(
                        text = "",
                        color = Color.Black,
                        modifier = Modifier
                            .size(32.dp)
                            .border(2.dp, Color.Black) // Black border
                            .background(Color.Transparent), // Transparent background
                            .clickable { // Pass the row and column index to the RuleEngine
                                RuleEngine.handleCellClick(i, j) },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}