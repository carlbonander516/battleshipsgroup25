package com.example.battleshipsgroup25

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Game(
    val name: String = "",
    val playerCount: Int = 0,
    val players: Map<String, Boolean> = emptyMap(),
    val readyStatus: Map<String, Boolean> = emptyMap(),
    val host: String = "",
    val status: String = "waiting"
)



class GameModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference

    private val _playerMap = MutableStateFlow<Map<String, String>>(emptyMap())
    val playerMap: StateFlow<Map<String, String>> get() = _playerMap

    private val _gameMap = MutableStateFlow<Map<String, Game>>(emptyMap())
    val gameMap: StateFlow<Map<String, Game>> get() = _gameMap

    private val _localPlayerId = MutableStateFlow<String?>(null)
    val localPlayerId: StateFlow<String?> get() = _localPlayerId
    val MAX_PLAYERS = 2

    init {
        loadGames()
        loadPlayers()
    }
    private fun loadPlayers() {
        database.child("players").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val players = mutableMapOf<String, String>()
                snapshot.children.forEach { playerSnapshot ->
                    val playerId = playerSnapshot.key ?: return@forEach
                    val playerName = playerSnapshot.child("name").getValue(String::class.java) ?: "Unknown"
                    players[playerId] = playerName
                }
                _playerMap.value = players
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error loading players: ${error.message}")
            }
        })
    }

    private fun loadGames() {
        database.child("games").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val games = mutableMapOf<String, Game>()
                snapshot.children.forEach { gameSnapshot ->
                    val gameId = gameSnapshot.key ?: return@forEach
                    val name = gameSnapshot.child("name").getValue(String::class.java) ?: "Unnamed"
                    val playersMap = gameSnapshot.child("players").getValue<Map<String, Boolean>>() ?: emptyMap()
                    val rawReadyStatus = gameSnapshot.child("readyStatus").value

                    val readyStatus = if (rawReadyStatus is Map<*, *>) {
                        rawReadyStatus as Map<String, Boolean>
                    } else {
                        emptyMap()
                    }

                    val playerCount = playersMap.size
                    games[gameId] = Game(
                        name = name,
                        playerCount = playerCount,
                        players = playersMap.toMutableMap(),
                        readyStatus = readyStatus
                    )
                }
                _gameMap.value = games
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GameModel", "Error loading games: ${error.message}")
            }
        })
    }



    fun joinGame(gameId: String, username: String, onError: (String) -> Unit, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val gameRef = database.child("games").child(gameId)
            gameRef.child("players").get().addOnSuccessListener { snapshot ->
                val players = snapshot.childrenCount
                if (players >= 2) {
                    onError("Lobby is full. Cannot join game.")
                    Log.e("GameModel", "Game $gameId is full.")
                } else {
                    gameRef.child("players").child(username).setValue(true)
                        .addOnSuccessListener {
                            Log.d("GameModel", "Player $username joined game $gameId successfully.")
                            onSuccess()
                        }
                        .addOnFailureListener { exception ->
                            onError("Failed to join the lobby. Please try again.")
                            Log.e("GameModel", "Error adding player to game: ${exception.message}")
                        }
                }
            }.addOnFailureListener { exception ->
                onError("Failed to check lobby status. Please try again.")
                Log.e("GameModel", "Error fetching players: ${exception.message}")
            }
        }
    }

    fun joinLobby(gameId: String, playerName: String, onComplete: (Boolean) -> Unit) {
        database.child("games").child(gameId).runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                val game = currentData.getValue(Game::class.java) ?: return Transaction.success(currentData)

                // Check if the lobby is already full
                if (game.playerCount >= MAX_PLAYERS) {
                    return Transaction.abort()
                }

                // Add the player to the game
                val updatedPlayers = game.players.toMutableMap().apply { this[playerName] = true }
                currentData.value = game.copy(playerCount = game.playerCount + 1, players = updatedPlayers)

                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    println("Error joining lobby: ${error.message}")
                } else if (!committed) {
                    println("Transaction not committed, possibly due to full lobby.")
                } else {
                    println("Player successfully added to lobby.")
                }
                onComplete(committed && error == null)
            }
        })
    }



    private fun setLocalPlayer(playerId: String) {
        _localPlayerId.value = playerId
        database.child("players").child(playerId).child("name").setValue("Player $playerId")
    }

    fun terminateGame(gameId: String) {
        database.child("games").child(gameId).removeValue()
            .addOnSuccessListener {
                println("Game $gameId successfully terminated.")
            }
            .addOnFailureListener {
                println("Error terminating game $gameId: ${it.message}")
            }
    }

    fun initListeners() {
        database.child("games").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val games = mutableMapOf<String, Game>()
                snapshot.children.forEach { gameSnapshot ->
                    val gameId = gameSnapshot.key ?: return@forEach
                    val name = gameSnapshot.child("name").getValue(String::class.java) ?: "Unnamed"
                    val playersMap = gameSnapshot.child("players").getValue<Map<String, Boolean>>() ?: emptyMap()
                    val playerCount = playersMap.size
                    val status = gameSnapshot.child("status").getValue(String::class.java) ?: "waiting"

                    // Safely deserialize readyStatus
                    val readyStatus = try {
                        val rawReadyStatus = gameSnapshot.child("readyStatus").value
                        if (rawReadyStatus is Map<*, *>) {
                            @Suppress("UNCHECKED_CAST")
                            rawReadyStatus as Map<String, Boolean>
                        } else {
                            Log.e("GameModel", "Invalid readyStatus format: $rawReadyStatus")
                            emptyMap()
                        }
                    } catch (e: Exception) {
                        Log.e("GameModel", "Error deserializing readyStatus: ${e.message}")
                        emptyMap()
                    }

                    // Create a Game object and add it to the map
                    games[gameId] = Game(
                        name = name,
                        playerCount = playerCount,
                        players = playersMap.toMutableMap(),
                        readyStatus = readyStatus,
                        status = status
                    )
                }
                _gameMap.value = games
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GameModel", "Error loading games: ${error.message}")
            }
        })
    }
    fun fetchGameData(gameId: String, onUpdate: (Map<String, Any?>) -> Unit) {
        database.child("games").child(gameId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onUpdate(snapshot.value as? Map<String, Any?> ?: emptyMap())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GameModel", "Error fetching game data: ${error.message}")
            }
        })
    }

    fun initializeGame(gameId: String, playerId: String, opponentId: String) {
        val ships = mapOf(
            "Carrier" to listOf("0,0", "0,1", "0,2", "0,3", "0,4"),
            "Battleship" to listOf("2,0", "2,1", "2,2", "2,3"),
            "Cruiser" to listOf("4,0", "4,1", "4,2"),
            "Submarine" to listOf("6,0", "6,1", "6,2"),
            "Destroyer" to listOf("8,0", "8,1")
        )

        // Initialize ships for both players
        database.child("games").child(gameId).child("players").child(playerId).child("ships").setValue(ships)
        database.child("games").child(gameId).child("players").child(opponentId).child("ships").setValue(ships)

        Log.d("GameModel", "Initialized game $gameId with ships for $playerId and $opponentId")
    }

}
