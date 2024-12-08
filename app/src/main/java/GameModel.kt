package com.example.battleshipsgroup25

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class Game(
    val name: String = "",
    val playerCount: Int = 0,
    val players: List<String> = emptyList()
)

class GameModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance().reference

    // StateFlow to observe player and game data
    private val _playerMap = MutableStateFlow<Map<String, String>>(emptyMap()) // playerId to playerName
    val playerMap: StateFlow<Map<String, String>> get() = _playerMap

    private val _gameMap = MutableStateFlow<Map<String, Game>>(emptyMap()) // gameId to Game object
    val gameMap: StateFlow<Map<String, Game>> get() = _gameMap

    private val _localPlayerId = MutableStateFlow<String?>(null)
    val localPlayerId: StateFlow<String?> get() = _localPlayerId

    init {
        // Load games and players data from Firebase
        loadGames()
        loadPlayers()
    }

    private fun loadGames() {
        database.child("games").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val games = mutableMapOf<String, Game>()
                snapshot.children.forEach { gameSnapshot ->
                    val gameId = gameSnapshot.key ?: return@forEach
                    val name = gameSnapshot.child("name").getValue(String::class.java) ?: "Unnamed"
                    val playerCount = gameSnapshot.child("players").childrenCount.toInt()
                    val players = gameSnapshot.child("players").children.mapNotNull { it.key }
                    games[gameId] = Game(name, playerCount, players)
                }
                _gameMap.value = games
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }
        })
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
                // Handle errors
            }
        })
    }

    fun createGame(gameName: String, hostPlayerId: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        val newGameRef = database.child("games").push()
        val newGameId = newGameRef.key ?: return onError("Failed to generate game ID")
        val newGame = mapOf(
            "name" to gameName,
            "host" to hostPlayerId,
            "players/$hostPlayerId" to true
        )
        newGameRef.setValue(newGame).addOnSuccessListener {
            onSuccess(newGameId)
        }.addOnFailureListener {
            onError(it.message ?: "Failed to create game")
        }
    }

    fun joinGame(gameId: String, playerId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        database.child("games").child(gameId).child("players").child(playerId).setValue(true)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onError(it.message ?: "Failed to join game")
            }
    }

    fun setLocalPlayer(playerId: String) {
        _localPlayerId.value = playerId
        database.child("players").child(playerId).child("name").setValue("Player $playerId") // Example name
    }

    fun initListeners() {
        // Listener for games
        database.child("games").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val games = mutableMapOf<String, Game>()
                snapshot.children.forEach { gameSnapshot ->
                    val gameId = gameSnapshot.key ?: return@forEach
                    val name = gameSnapshot.child("name").getValue(String::class.java) ?: "Unnamed"
                    val playerCount = gameSnapshot.child("players").childrenCount.toInt()
                    val players = gameSnapshot.child("players").children.mapNotNull { it.key }
                    games[gameId] = Game(name, playerCount, players)
                }
                _gameMap.value = games
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle Firebase error
                println("Error loading games: ${error.message}")
            }
        })

        // Listener for players
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
                // Handle Firebase error
                println("Error loading players: ${error.message}")
            }
        })
    }

}
