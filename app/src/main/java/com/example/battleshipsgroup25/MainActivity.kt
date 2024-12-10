package com.example.battleshipsgroup25

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.battleshipsgroup25.ui.theme.Battleshipsgroup25Theme
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)

        super.onCreate(savedInstanceState)
        setContent {
            Battleshipsgroup25Theme {
                val navController = rememberNavController()
                val gameModel = remember { GameModel() }
                val database = FirebaseDatabase.getInstance().reference

                // Test Firebase connection
                try {
                    database.child("test").setValue("Firebase connected!")
                        .addOnSuccessListener {
                            Log.d("FirebaseTest", "Connection successful!")
                        }
                        .addOnFailureListener {
                            Log.e("FirebaseTest", "Connection failed: ${it.message}")
                        }
                } catch (e: Exception) {
                    Log.e("FirebaseTest", "Unexpected error: ${e.message}")
                }

                // Initialize listeners
                try {
                    gameModel.initListeners()
                } catch (e: Exception) {
                    Log.e("GameModelInit", "Error initializing listeners: ${e.message}")
                }

                // Initialize NavGraph
                NavGraph(
                    navController = navController,
                    model = gameModel,
                    username = "" // Username will be dynamically set in the navigation
                )
            }
        }
    }
}

