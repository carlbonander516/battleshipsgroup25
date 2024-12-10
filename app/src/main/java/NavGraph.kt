package com.example.battleshipsgroup25

import GameModes
import Gameboard
import IntroScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun NavGraph(navController: NavHostController, model: GameModel) {
    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") {
            IntroScreen(navController = navController)
        }

        composable("game_modes") {
            GameModes(navController = navController)
        }

        composable("enter_username") {
            EnterUsernameScreen(
                navController = navController,
                onUsernameCreated = { username ->
                    navController.navigate("lobby/$username") // Navigate to lobby with username
                }
            )
        }

        composable(
            "lobby/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Unknown"
            LobbyScreen(
                navController = navController,
                model = model,
                username = username,
                maxPlayers = MAX_PLAYERS
            )
        }
        composable(
            "game/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "Unknown"
            GameLobby(
                navController = navController,
                gameId = gameId,
                model = model
            )
        }

        composable(
            "game_board/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "Unknown"
            Gameboard(navController = navController, gameId = gameId)
        }
    }
}
