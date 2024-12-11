package com.example.battleshipsgroup25
import com.example.battleshipsgroup25.GameLobbyScreen

import GameModes
import Gameboard
import IntroScreen
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun NavGraph(navController: NavHostController, model: GameModel, username: String, playerId: String) {
    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") {
            IntroScreen(navController = navController)
        }
        composable("game_modes") {
            GameModes(navController = navController)
        }
        composable("game_board") {
            Gameboard(navController = navController)
        }
        composable("enter_username") {
            EnterUsernameScreen(
                navController = navController,
                onUsernameCreated = { enteredUsername ->
                    navController.navigate("lobby/$enteredUsername")
                }
            )
        }
        composable("lobby/{username}", arguments = listOf(navArgument("username") { type = NavType.StringType })) { backStackEntry ->
            val enteredUsername = backStackEntry.arguments?.getString("username") ?: "Unknown"
            LobbyScreen(navController, model, enteredUsername, MAX_PLAYERS)
        }
        composable("game/{gameId}", arguments = listOf(navArgument("gameId") { type = NavType.StringType })) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "Unknown"
            GameLobbyScreen(navController, gameId, model, username, playerId)
        }
        composable("game_lobby/{gameId}", arguments = listOf(navArgument("gameId") { type = NavType.StringType })) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "Unknown"
            GameLobbyScreen(navController, gameId, model, username, playerId)
        }
        composable("gameboard_online/{gameId}", arguments = listOf(navArgument("gameId") { type = NavType.StringType })) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: "Unknown"
            GameboardOnline(navController, gameId, playerId)
        }
    }
}
