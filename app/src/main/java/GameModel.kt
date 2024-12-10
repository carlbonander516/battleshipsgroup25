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
    val players: MutableMap<String, Boolean> = mutableMapOf() // Key: PlayerName, Value: true
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

    private fun loadGames() {
        database.child("games").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val games = mutableMapOf<String, Game>()
                snapshot.children.forEach { gameSnapshot ->
                    val gameId = gameSnapshot.key ?: return@forEach
                    val name = gameSnapshot.child("name").getValue(String::class.java) ?: "Unnamed"
                    val playersMap = gameSnapshot.child("players").getValue<Map<String, Boolean>>() ?: emptyMap()
                    val playerCount = playersMap.size
                    games[gameId] = Game(name = name, playerCount = playerCount, players = playersMap.toMutableMap())
                }
                _gameMap.value = games
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error loading games: ${error.message}")
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
                println("Error loading players: ${error.message}")
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
            setLocalPlayer(hostPlayerId)
            onSuccess(newGameId)
        }.addOnFailureListener {
            onError(it.message ?: "Failed to create game")
        }
    }

    fun joinGame(gameId: String, playerId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        database.child("games").child(gameId).child("players").child(playerId).setValue(true)
            .addOnSuccessListener {
                setLocalPlayer(playerId)
                onSuccess()
            }
            .addOnFailureListener {
                println("Error joining game: ${it.message}")
                onError(it.message ?: "Failed to join game")
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
                    games[gameId] = Game(name, playerCount, playersMap.toMutableMap())
                }
                _gameMap.value = games
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error loading games: ${error.message}")
            }
        })

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
}
