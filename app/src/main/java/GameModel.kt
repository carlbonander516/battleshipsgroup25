package com.example.battleshipsgroup25

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot


class GameModel {
    private val firestore = FirebaseFirestore.getInstance() // Initialize Firestore
    private val _gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())
    val gameMap: StateFlow<Map<String, Game>> get() = _gameMap

    // Listener to track changes in the games collection
    fun initListeners() {
        firestore.collection("games").addSnapshotListener { snapshots: QuerySnapshot?, e: FirebaseFirestoreException? ->
            Log.d("GameModel", "Listener triggered")
            if (e != null) {
                Log.e("GameModel", "Error loading games: ${e.message}")
                return@addSnapshotListener
            }

            Log.d("GameModel", "Processing snapshots")
            val games = mutableMapOf<String, Game>()
            snapshots?.documents?.forEach { gameDoc ->
                Log.d("GameModel", "Processing game document: ${gameDoc.id}")
                val gameId = gameDoc.id
                val name = gameDoc.getString("name") ?: "Unnamed"
                val players = gameDoc.get("players") as? Map<String, Boolean> ?: emptyMap()
                val status = gameDoc.getString("status") ?: "waiting"
                val readyStatus = gameDoc.getBoolean("readyStatus") ?: false

                games[gameId] = Game(name, players.size, players, readyStatus, status)
            }
            _gameMap.value = games
            Log.d("GameModel", "Games updated: ${games.keys}")
        }
    }



    var isCreatingLobby = false

    fun createGame(
        lobbyName: String,
        username: String,
        maxPlayers: Int,
        onError: (String) -> Unit
    ): String? {
        if (isCreatingLobby) {
            Log.d("GameModel", "Lobby creation already in progress.")
            return null
        }

        isCreatingLobby = true

        val newGameRef = firestore.collection("games").document()

        val newGameData = mapOf(
            "name" to lobbyName,
            "host" to username,
            "status" to "waiting",
            "readyStatus" to false,
            "players" to mapOf(username to true)
        )

        newGameRef.set(newGameData)
            .addOnSuccessListener {
                isCreatingLobby = false
                Log.d("GameModel", "Game created successfully with ID: ${newGameRef.id}")
            }
            .addOnFailureListener { error ->
                isCreatingLobby = false
                Log.e("GameModel", "Error creating game: ${error.message}")
                onError("Error creating game: ${error.message}")
            }

        return newGameRef.id
    }


    fun joinGame(
        gameId: String,
        username: String,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        val gameRef = firestore.collection("games").document(gameId)

        gameRef.update("players.$username", true) // Update Firestore document with a nested field
            .addOnSuccessListener {
                Log.d("GameModel", "Successfully joined game: $gameId")
                onSuccess()
            }
            .addOnFailureListener { error ->
                Log.e("GameModel", "Error joining game: ${error.message}")
                onError("Error joining game: ${error.message}")
            }
    }
}

fun sanitizeKey(key: String): String {
    return key.replace(Regex("[./#$\\[\\]]"), "_")
}

data class Game(
    val name: String,
    val playerCount: Int,
    val players: Map<String, Boolean>,
    val readyStatus: Boolean,
    val status: String
)
