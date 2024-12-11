package com.example.battleshipsgroup25

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.google.firebase.database.*

class GameModel {
    private val database = FirebaseDatabase.getInstance().reference
    private val _gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())
    val gameMap: StateFlow<Map<String, Game>> get() = _gameMap

    fun initListeners() {
        database.child("games").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val games = mutableMapOf<String, Game>()
                snapshot.children.forEach { gameSnapshot ->
                    val gameId = gameSnapshot.key ?: return@forEach

                    val name = gameSnapshot.child("name").getValue(String::class.java) ?: "Unnamed"
                    val players =
                        gameSnapshot.child("players").getValue<Map<String, Boolean>>() ?: emptyMap()
                    val status =
                        gameSnapshot.child("status").getValue(String::class.java) ?: "waiting"
                    val readyStatus =
                        gameSnapshot.child("readyStatus").getValue(Boolean::class.java) ?: false

                    games[gameId] = Game(name, players.size, players, readyStatus, status)
                }
                _gameMap.value = games
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GameModel", "Error loading games: ${error.message}")
            }
        })
    }

    fun createGame(
        lobbyName: String,
        username: String,
        maxPlayers: Int,
        onError: (String) -> Unit
    ): String? {
        if (lobbyName.isBlank()) {
            onError("Lobby name cannot be empty!")
            return null
        }

        val newGameId = database.child("games").push().key.orEmpty()

        val newGameData = mapOf(
            "name" to lobbyName,
            "host" to username,
            "status" to "waiting",
            "readyStatus" to false,
            "players" to mapOf(username to true)
        )

        database.child("games").child(newGameId).setValue(newGameData)
            .addOnSuccessListener {
                Log.d("GameModel", "Game created successfully with ID: $newGameId")
            }
            .addOnFailureListener { error ->
                Log.e("GameModel", "Error creating game: ${error.message}")
                onError("Error creating game: ${error.message}")
            }

        return newGameId
    }

    fun joinGame(
        gameId: String,
        username: String,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        val gameRef = database.child("games").child(gameId)

        gameRef.child("players").child(username).setValue(true)
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
