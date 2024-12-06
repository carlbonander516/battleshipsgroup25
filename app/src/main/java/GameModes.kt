import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.battleshipsgroup25.R

@Composable
fun GameModes(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image filling the entire screen
        Image(
            painter = painterResource(id = R.drawable.tempmodes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Fills screen without borders
        )

        // Centered text
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Text("Welcome to Battleships!")
        }

        // Button positioned at the bottom center of the screen
        Button(
            onClick = { navController.navigate("lobby") },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Aligns button at the bottom center of the Box
                .padding(bottom = 275.dp) // Adds padding from the bottom edge
        ) {
            Text("Online")
        }
        Button(
            onClick = { navController.navigate("game_board") },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Aligns button at the bottom center of the Box
                .padding(bottom = 225.dp) // Adds padding from the bottom edge
        ) {
            Text("Offline")
        }
    }
}