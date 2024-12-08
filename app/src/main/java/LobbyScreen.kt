import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.battleshipsgroup25.GameModel
import com.google.firebase.database.*

@Composable
fun LobbyScreen(navController: NavController, model: GameModel, username: String) {
    val players by model.playerMap.collectAsStateWithLifecycle()
    var games by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) } // (Game ID, Host Name)
    val database = FirebaseDatabase.getInstance().reference.child("games")

// Fetch games from Firebase
    LaunchedEffect(Unit) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gameList = mutableListOf<Pair<String, String>>()
                snapshot.children.forEach { game ->
                    val gameId = game.key.orEmpty()
                    val host = game.child("host").getValue(String::class.java).orEmpty()
                    gameList.add(gameId to host)
                }
                games = gameList
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error fetching games: ${error.message}")
            }
        })
    }

    // Check if games exist
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (games.isEmpty()) {
            // No games available: Allow user to create a game
            Text("No games available. Create a game to get started!")

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val newGameId = database.push().key.orEmpty()
                    val newGameData = mapOf(
                        "host" to username,
                        "status" to "waiting"
                    )
                    database.child(newGameId).setValue(newGameData)
                        .addOnSuccessListener {
                            navController.navigate("game/$newGameId") // Navigate to created game
                        }
                        .addOnFailureListener { error ->
                            println("Error creating game: ${error.message}")
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Game")
            }
        } else {
            // Games exist: Display join options
            Text("Available Games")

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(games) { (gameId, host) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Game by $host")
                        Button(onClick = { navController.navigate("game/$gameId") }) {
                            Text("Join")
                        }
                    }
                }
            }
        }
    }
}
