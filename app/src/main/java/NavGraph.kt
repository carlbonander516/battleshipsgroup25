package com.example.battleshipsgroup25

import GameModes
import Gameboard
import IntroScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

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
                username = username
            )
        }
        composable(
            "game/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "Unknown"
            println("Navigating to GameLobby with Game ID: $gameId") // Debug log

            GameLobby(
                navController = navController,
                gameId = gameId,
                model = model
            )
        }


    }
}

