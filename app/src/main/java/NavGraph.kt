package com.example.battleshipsgroup25

import GameModes
import Gameboard
import IntroScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(navController: NavHostController, model: GameModel) {
    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") { IntroScreen(navController) }
        composable("game_modes") { GameModes(navController) }
        composable("enter_username") {
            EnterUsernameScreen(
                navController = navController,
                onUsernameCreated = { username ->
                    model.createPlayer(
                        name = username,
                        onSuccess = { playerId ->
                            model.localPlayerId.value = playerId
                            navController.navigate("lobby")
                        },
                        onError = { error ->
                            // Handle error (e.g., show a toast)
                        }
                    )
                }
            )
        }
        composable("lobby") {
            LobbyScreen(navController, model)
        }
        composable("game_board/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            Gameboard(navController, gameId = gameId) // Pass gameId to Gameboard
        }
    }
}
