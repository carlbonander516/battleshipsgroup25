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
import com.example.battleshipsgroup25.R
import com.example.battleshipsgroup25.Ship
import com.example.battleshipsgroup25.ShipManager

@Composable
fun Gameboard(navController: NavHostController) {

    val boardSize = 10
    val shipManager = remember { ShipManager(boardSize) }
    val playerShips = remember { mutableStateListOf<Ship>(*shipManager.placeShips().toTypedArray()) }
    val botShips = remember { mutableStateListOf<Ship>(*shipManager.placeShips().toTypedArray()) }

    // Debugging: Log ship placements
    Log.d("ShipPlacement", "Player Ships: ${playerShips.map { it.name to it.positions }}")
    Log.d("ShipPlacement", "Bot Ships: ${botShips.map { it.name to it.positions }}")

    val selectedShip = remember { mutableStateOf<Ship?>(null) }
    val currentOrientation = remember { mutableStateOf("H") }
    val gameStarted = remember { mutableStateOf(false) }
    val gameOver = remember { mutableStateOf(false) }
    val winner = remember { mutableStateOf("") }
    RuleEngine.initialize(boardSize, botShips.toList())
    val botGridHits = remember { mutableStateListOf<Pair<Int, Int>>() }
    val botGridMisses = remember { mutableStateListOf<Pair<Int, Int>>() }
    val turn = remember { mutableStateOf("Player") }
    val playerGridHits = remember { mutableStateListOf<Pair<Int, Int>>() }
    val playerGridMisses = remember { mutableStateListOf<Pair<Int, Int>>() }

    fun botAttack(): Triple<Int, Int, Boolean>? {
        val availableCells = (0 until boardSize).flatMap { row ->
            (0 until boardSize).map { col -> Pair(row, col) }
        }.filter { it !in playerGridHits && it !in playerGridMisses }

        if (availableCells.isEmpty()) {
            println("Bot has no cells left to attack!")
            return null
        }

        val target = availableCells.random()
        val (row, col) = target

        val hit = playerShips.any { ship -> target in ship.positions }

        if (hit) {
            playerGridHits.add(target)
            Log.d("BotAttack", "Hit detected at ($row, $col)")
        } else {
            playerGridMisses.add(target)
            Log.d("BotAttack", "Miss detected at ($row, $col)")
        }

        // Check for game over condition
        if (playerGridHits.size == playerShips.flatMap { it.positions }.size) {
            gameOver.value = true
            winner.value = "Bot"
        }

        return Triple(row, col, hit)
    }

    fun botTurn() {
        if (gameOver.value) return

        val result = botAttack()
        if (result != null) {
            val (row, col, hit) = result
            if (hit) {
                println("Bot hit a player's ship at ($row, $col)!")
                botTurn() // Continue bot turn on hit
            } else {
                println("Bot missed at ($row, $col).")
            }
        }
    }

    if (gameOver.value) {
        // Game Over Screen
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
        // Main Game UI
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
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
                if (!gameStarted.value) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(onClick = {
                            currentOrientation.value = if (currentOrientation.value == "H") "V" else "H"
                        }) {
                            Text("Toggle Orientation: ${if (currentOrientation.value == "H") "Horizontal" else "Vertical"}")
                        }

                        Button(onClick = { gameStarted.value = true }) {
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

                // Bot grid
                Grid(
                    size = boardSize,
                    ships = botShips,
                    selectedShip = remember { mutableStateOf(null) },
                    onCellClick = { row, col ->
                        if (!gameOver.value && gameStarted.value && Pair(row, col) !in botGridHits && Pair(row, col) !in botGridMisses) {
                            val hit = RuleEngine.handleCellClick(row, col)
                            if (hit) {
                                botGridHits.add(Pair(row, col))
                                println("Player hit a bot ship at ($row, $col)!")
                                if (botGridHits.size == botShips.flatMap { it.positions }.size) {
                                    gameOver.value = true
                                    winner.value = "Player"
                                }
                            } else {
                                botGridMisses.add(Pair(row, col))
                                println("Player missed at ($row, $col)!")
                                botTurn()
                            }
                        }
                    },
                    onCellLongClick = { _, _ -> },
                    highlights = botGridHits,
                    misses = botGridMisses,
                    gameStarted = gameStarted.value
                )

                // Player's grid
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
                    onCellLongClick = { _, _ -> },
                    highlights = playerGridHits,
                    misses = playerGridMisses,
                    gameStarted = false
                )
            }
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
    misses: List<Pair<Int, Int>> = emptyList(),
    gameStarted: Boolean
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
                                    isHit -> Color.Red
                                    isMiss -> Color.Blue
                                    isShipTile && !gameStarted -> Color.DarkGray
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
