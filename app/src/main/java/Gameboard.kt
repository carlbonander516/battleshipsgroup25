// GameBoard.kt
import android.util.Log
import com.example.battleshipsgroup25.RuleEngine

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
import com.example.battleshipsgroup25.Bot
import com.example.battleshipsgroup25.R
import com.example.battleshipsgroup25.Ship
import com.example.battleshipsgroup25.ShipManager


@Composable
fun Gameboard(navController: NavHostController) {

    // Separate managers for player and bot
    val boardSize = 10
    val shipManager = remember { ShipManager(boardSize) }
    val playerShips = remember { mutableStateListOf<Ship>(*shipManager.placeShips().toTypedArray()) }
    val botShips = remember { mutableStateListOf<Ship>(*shipManager.placeShips().toTypedArray()) }

        // Debugging: Log final ship placements
    Log.d("ShipPlacement", "Player Ships: ${playerShips.map { it.name to it.positions }}")
    Log.d("ShipPlacement", "Bot Ships: ${botShips.map { it.name to it.positions }}")

        // UI Logic Here...
    val selectedShip = remember { mutableStateOf<Ship?>(null) }
    val currentOrientation = remember { mutableStateOf("H") }
    val gameStarted = remember { mutableStateOf(false) }
    RuleEngine.initialize(boardSize, botShips.toList())
    val botGridHits = remember { mutableStateListOf<Pair<Int, Int>>() }
    val botGridMisses = remember { mutableStateListOf<Pair<Int, Int>>() }
    val turn = remember { mutableStateOf("Player") }
    val playerGridHits = remember { mutableStateListOf<Pair<Int, Int>>() } // Bot's successful hits
    val playerGridMisses = remember { mutableStateListOf<Pair<Int, Int>>() } // Bot's missed attacks


    fun botAttack(
        boardSize: Int,
        playerGridHits: MutableList<Pair<Int, Int>>,
        playerGridMisses: MutableList<Pair<Int, Int>>
    ): Triple<Int, Int, Boolean>? {
        // Randomly select a target cell
        val availableCells = (0 until boardSize).flatMap { row ->
            (0 until boardSize).map { col -> Pair(row, col) }
        }.filter { it !in playerGridHits && it !in playerGridMisses }
        if (availableCells.isEmpty()) {
            println("Bot has no cells left to attack!")
            return null
        }
        val target = availableCells.random()
        val (row, col) = target
        // Simulate a hit or miss
        val hit = playerShips.any { ship -> target in ship.positions }
        if (hit) {
            playerGridHits.add(target)
        } else {
            playerGridMisses.add(target)
        }
        return Triple(row, col, hit)
    }

    fun botTurn(
        boardSize: Int,
        playerGridHits: MutableList<Pair<Int, Int>>,
        playerGridMisses: MutableList<Pair<Int, Int>>
    ) {
        val result = botAttack(boardSize, playerGridHits, playerGridMisses)
        if (result != null) {
            val (row, col, hit) = result
            if (hit) {
                println("Bot hit a player's ship at ($row, $col)!")
                botTurn(boardSize, playerGridHits, playerGridMisses) // Continue bot turn on hit
            } else {
                println("Bot missed at ($row, $col).")
            }
        }
    }

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
                    text = "Turn: ${turn.value}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            //bot grid
            Grid(
                size = boardSize,
                ships = botShips, // Don't show bot's ships
                selectedShip = remember { mutableStateOf(null) },
                onCellClick = { row, col ->
                    if (gameStarted.value && Pair(row, col) !in botGridHits && Pair(row, col) !in botGridMisses) {
                        val hit = RuleEngine.handleCellClick(row, col)
                        if (hit) {
                            botGridHits.add(Pair(row, col))
                            println("Player 1 hit a bot ship at ($row, $col)!")
                        } else {
                            botGridMisses.add(Pair(row, col))
                            println("Player 1 missed at ($row, $col)!")
                            botTurn(boardSize, playerGridHits, playerGridMisses) // Pass turn to bot
                        }
                    }
                },
                onCellLongClick = { _, _ -> },
                highlights = botGridHits,
                misses = botGridMisses,
                gameStarted = gameStarted.value
            )

            // Player's grid (for bot to attack)
            Grid(
                size = boardSize,
                ships = playerShips,
                selectedShip = selectedShip,
                onCellClick = { row, col ->
                    if (!gameStarted.value) { // Allow interaction before the game starts
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
                onCellLongClick = { _, _ -> },
                highlights = emptyList(),
                misses = playerGridMisses,
                gameStarted = false // Always allow ship visibility
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
    highlights: List<Pair<Int, Int>> = emptyList(),
    misses: List<Pair<Int, Int>> = emptyList(),  // Add misses
    gameStarted: Boolean                         // Add gameStarted
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (i in 0 until size) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (j in 0 until size) {
                    val isShipTile = ships.any { ship -> Pair(i, j) in ship.positions }
                    val isSelectedTile = selectedShip.value?.positions?.contains(Pair(i, j)) == true
                    val isHit = highlights.contains(Pair(i, j))
                    val isMiss = misses.contains(Pair(i, j))

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .border(2.dp, if (isSelectedTile) Color.Red else Color.Black)
                            .background(
                                when {
                                    isHit -> Color.Red          // Hit cell
                                    isMiss -> Color.Blue        // Missed cell
                                    isShipTile && !gameStarted -> Color.DarkGray // Show ships during setup only
                                    else -> Color.Transparent   // Empty cell
                                }
                            )
                            .clickable { onCellClick(i, j) }
                    )
                }
            }
        }
    }
}
