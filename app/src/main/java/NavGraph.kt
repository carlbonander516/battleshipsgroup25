// NavGraph.kt

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

//import com.example.battleshipsgroup25.screens.HomeScreen // replace with your actual home screen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "intro") {
        composable("intro") { IntroScreen(navController) }
        composable("game_modes") { GameModes(navController) } // Add other screens as needed
    }
}
