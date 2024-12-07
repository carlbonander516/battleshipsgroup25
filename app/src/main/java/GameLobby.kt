package com.example.battleshipsgroup25

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun GameLobby(
    navController: NavController,
    gameId: String,
    model: GameModel
) {
    val game = model.gameMap.value[gameId]
    val playerName = model.localPlayerId.value?.let { id ->
        model.playerMap.value[id]?.name
    } ?: "Unknown Player"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Player: $playerName")
        Text(text = "Game: ${game?.name ?: "Loading..."}")

        Spacer(modifier = Modifier.height(16.dp))

        if (game?.players?.size == 2) {
            Text("Game ready to start!")
            Button(onClick = { navController.navigate("game_board") }) {
                Text("Start Game")
            }
        } else {
            Text("Waiting for another player...")
        }
    }
}
