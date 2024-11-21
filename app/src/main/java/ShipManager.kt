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

    fun canPlaceShip(ship: Ship, startRow: Int, startCol: Int, direction: String): Boolean {
        val newPositions = calculatePositions(ship, startRow, startCol, direction)

        // Check bounds and no overlap
        if (newPositions.any { it.first !in 0 until boardSize || it.second !in 0 until boardSize }) {
            return false
        }
        val otherShipPositions = ships.filter { it != ship }.flatMap { it.positions }
        if (newPositions.any { it in otherShipPositions }) {
            return false
        }
        // Check for 1-tile spacing (adjust as needed)
        val adjacentPositions = newPositions.flatMap { (r, c) ->
            listOf(
                Pair(r - 1, c), Pair(r + 1, c), Pair(r, c - 1), Pair(r, c + 1),
                Pair(r - 1, c - 1), Pair(r - 1, c + 1), Pair(r + 1, c - 1), Pair(r + 1, c + 1)
            )
        }
        if (adjacentPositions.any { it in otherShipPositions }) {
            return false
        }
        return true
    }


    private var selectedShip: Ship? = null
    fun selectShip(ship: Ship) {
        selectedShip = ship
    }
    fun moveSelectedShip(newStartRow: Int, newStartCol: Int, direction: String): Boolean {
        selectedShip?.let { ship ->
            if (canPlaceShip(ship, newStartRow, newStartCol, direction)) {
                ship.positions = calculatePositions(ship, newStartRow, newStartCol, direction)
                return true
            }
        }
        return false
    }


    private fun calculatePositions(ship: Ship, startRow: Int, startCol: Int, direction: String): List<Pair<Int, Int>> {
        return when (direction) {
            "H" -> (0 until ship.length).map { Pair(startRow, startCol + it) }
            "V" -> (0 until ship.length).map { Pair(startRow + it, startCol) }
            else -> emptyList()
        }
    }}
