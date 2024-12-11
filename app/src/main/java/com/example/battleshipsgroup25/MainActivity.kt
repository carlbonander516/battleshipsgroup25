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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.Logger
import com.google.firebase.database.ValueEventListener

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG)

        setContent {
            Battleshipsgroup25Theme {
                val navController = rememberNavController()
                val gameModel = remember { GameModel() }

                FirebaseAuth.getInstance().signInAnonymously()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Auth", "Sign-in successful: ${task.result?.user?.uid}")
                        } else {
                            Log.e("Auth", "Sign-in failed: ${task.exception?.message}")
                        }
                    }

                NavGraph(navController, gameModel, username = "", playerId = "")
            }
        }
    }
}



