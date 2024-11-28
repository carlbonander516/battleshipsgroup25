package com.example.battleshipsgroup25

class Bot(boardSize: Int) {
    private val shipManager = ShipManager(boardSize)
    val ships: List<Ship>

    init {
        // Randomize the bot's ship placements
        ships = shipManager.placeShips()
    }

    // Check if a player's attack hits the bot's ships
    fun handleAttack(row: Int, col: Int): Boolean {
        val hitShip = ships.find { ship -> Pair(row, col) in ship.positions }
        return hitShip != null
    }
}
