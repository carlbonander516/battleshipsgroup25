package com.example.battleshipsgroup25

import GameModes
import Gameboard
import IntroScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun NavGraph(navController: NavHostController, model: GameModel, username: String) {
    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") {
            IntroScreen(navController = navController)
        }

        composable("game_modes") {
            GameModes(navController = navController)
        }

        // Offline game board
        composable("game_board") {
            Gameboard(navController = navController)
        }

        composable("enter_username") {
            EnterUsernameScreen(
                navController = navController,
                onUsernameCreated = { enteredUsername ->
                    navController.navigate("lobby/$enteredUsername") // Navigate to lobby with username
                }
            )
        }

        composable(
            "lobby/{username}",
            arguments = listOf(navArgument("username") { type = NavType.StringType })
        ) { backStackEntry ->
            val enteredUsername = backStackEntry.arguments?.getString("username") ?: "Unknown"
            LobbyScreen(
                navController = navController,
                model = model,
                username = enteredUsername, // Pass the correctly renamed variable
                maxPlayers = MAX_PLAYERS
            )
        }



        composable(
            "game/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "Unknown"
            GameLobbyScreen(
                navController = navController,
                gameId = gameId,
                model = model,
                username = username // Use the username from the previous screen
            )
        }


        composable(
            "game_board/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "Unknown"
            Gameboard(navController = navController, gameId = gameId)
        }

        composable("game_lobby/{gameId}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: return@composable
            GameLobbyScreen(
                navController = navController,
                gameId = gameId,
                model = model,
                username = username
            )
        }

        composable(
            "gameboard_online/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "Unknown"
            GameboardOnline(
                navController = navController,
                gameId = gameId,
                playerId = username // Use the username as playerId
            )
        }

    }
}
