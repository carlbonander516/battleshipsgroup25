package com.example.battleshipsgroup25

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow

data class Player(
    val id: String = "",
    val name: String = ""
)

data class Game(
    val id: String = "",
    val name: String = "",
    val playerCount: Int = 0,
    val gameState: String = "waiting", // States: "waiting", "in_progress", "game_over"
    val players: List<String> = emptyList()
)

class GameModel : ViewModel() {
    val yo = Firebase.firestore
    private val db = Firebase.firestore
    val localPlayerId = MutableStateFlow<String?>(null)
    val playerMap = MutableStateFlow<Map<String, Player>>(emptyMap())
    val gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())

    private fun testFirestoreConnection() {
        db.collection("test").document("testDoc")
            .set(mapOf("testField" to "Hello Firestore"))
            .addOnSuccessListener {
                println("Test document written successfully!")
            }
            .addOnFailureListener { error ->
                println("Failed to write test document: ${error.message}")
            }
    }
    fun initListeners() {
        testFirestoreConnection()
        // Listen for players
        db.collection("players").addSnapshotListener { value, error ->
            if (error == null && value != null) {
                val updatedPlayers = value.documents.associate { doc ->
                    doc.id to doc.toObject(Player::class.java)!!
                }
                playerMap.value = updatedPlayers
            }
        }

        // Listen for games
        db.collection("games").addSnapshotListener { value, error ->
            if (error == null && value != null) {
                val updatedGames = value.documents.associate { doc ->
                    doc.id to doc.toObject(Game::class.java)!!
                }
                gameMap.value = updatedGames
            }
        }
    }

    fun createPlayer(name: String, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val newPlayer = Player(name = name)
        db.collection("players").add(newPlayer)
            .addOnSuccessListener { documentRef ->
                onSuccess(documentRef.id)
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    fun createGame(lobbyName: String, playerId: String, onSuccess: (String) -> Unit, onError: (Exception) -> Unit) {
        val newGame = Game(
            name = lobbyName,
            playerCount = 1,
            players = listOf(playerId)
        )
        db.collection("games").add(newGame)
            .addOnSuccessListener { documentRef ->
                onSuccess(documentRef.id)
            }
            .addOnFailureListener { error ->
                onError(error)
            }
    }

    fun joinGame(gameId: String, playerId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        db.collection("games").document(gameId)
            .update("players", FieldValue.arrayUnion(playerId), "playerCount", 2)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { error -> onError(error) }
    }
}
