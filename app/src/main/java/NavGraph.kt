// NavGraph.kt

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.battleshipsgroup25.Lobby // import Lobby.kt
import com.example.battleshipsgroup25.GameLobby // import GameLobby.kt

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") { IntroScreen(navController) }
        composable("game_modes") { GameModes(navController) }
        composable("lobby") { Lobby(navController) } // Use Lobby here
        composable("game_board") { Gameboard(navController) }

        // GameLobby route with parameters for gameId and username
        composable("game_lobby/{gameId}/{username}") { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val username = backStackEntry.arguments?.getString("username") ?: ""
            GameLobby(navController, gameId, username)
        }
    }
}
