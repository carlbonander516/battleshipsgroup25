package com.example.battleshipsgroup25

data class Ship(val name: String, val length: Int, var positions: List<Pair<Int, Int>> = emptyList())

class ShipManager(private val boardSize: Int) {

    private val ships = mutableListOf<Ship>()

    // List of predefined ships
    init {
        ships.add(Ship(name = "Carrier", length = 5))
        ships.add(Ship(name = "Battleship", length = 4))
        ships.add(Ship(name = "Cruiser", length = 3))
        ships.add(Ship(name = "Submarine", length = 3))
        ships.add(Ship(name = "Destroyer", length = 2))
    }

    // Function to randomly place ships on the gameboard
    fun placeShips(): List<Ship> {
        ships.forEach { ship ->
            var placed = false
            while (!placed) {
                val startRow = (0 until boardSize).random()
                val startCol = (0 until boardSize).random()
                val direction = listOf("H", "V").random() // Horizontal or Vertical

                if (canPlaceShip(ship, startRow, startCol, direction)) {
                    ship.positions = calculatePositions(ship, startRow, startCol, direction)
                    placed = true
                }
            }
        }
        return ships
    }

    // Check if a ship can be placed at the given position
    private fun canPlaceShip(ship: Ship, startRow: Int, startCol: Int, direction: String): Boolean {
        val positions = calculatePositions(ship, startRow, startCol, direction)

        // Validate positions are within bounds and do not overlap existing ships
        if (positions.any { it.first !in 0 until boardSize || it.second !in 0 until boardSize }) {
            return false
        }

        if (positions.any { pos -> ships.any { other -> pos in other.positions } }) {
            return false
        }

        return true
    }

    // Calculate the positions a ship will occupy based on its direction
    private fun calculatePositions(ship: Ship, startRow: Int, startCol: Int, direction: String): List<Pair<Int, Int>> {
        return when (direction) {
            "H" -> (0 until ship.length).map { Pair(startRow, startCol + it) }
            "V" -> (0 until ship.length).map { Pair(startRow + it, startCol) }
            else -> emptyList()
        }
    }
}
