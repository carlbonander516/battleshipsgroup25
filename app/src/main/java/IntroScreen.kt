// IntroScreen.kt (updated)
package com.example.battleshipsgroup25.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
fun IntroScreen(navController: NavController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image filling the entire screen
        Image(
            painter = painterResource(id = R.drawable.battleship1),
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
            onClick = { navController.navigate("GameModes") },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Aligns button at the bottom center of the Box
                .padding(bottom = 275.dp) // Adds padding from the bottom edge
        ) {
            Text("Game modes")
        }
        Button(
            onClick = { navController.navigate("") },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Aligns button at the bottom center of the Box
                .padding(bottom = 225.dp) // Adds padding from the bottom edge
        ) {
            Text("Settings")
        }
        Button(
            onClick = { navController.navigate("") },
            modifier = Modifier
                .align(Alignment.BottomCenter) // Aligns button at the bottom center of the Box
                .padding(bottom = 175.dp) // Adds padding from the bottom edge
        ) {
            Text("Account")
        }
    }
}
