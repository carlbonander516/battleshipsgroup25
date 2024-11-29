package com.example.battleshipsgroup25

import android.util.Log

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
        val shipsToPlace = listOf(
            Ship(name = "Carrier", length = 5),
            Ship(name = "Battleship", length = 4),
            Ship(name = "Cruiser", length = 3),
            Ship(name = "Submarine", length = 3),
            Ship(name = "Destroyer", length = 2)
        )
        val placedShips = mutableListOf<Ship>()
        for (ship in shipsToPlace) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 100) {
                val startRow = (0 until boardSize).random()
                val startCol = (0 until boardSize).random()
                val direction = listOf("H", "V").random()

                Log.d("ShipPlacement", "Attempting to place ${ship.name} at ($startRow, $startCol) with direction $direction")
                if (canPlaceShip(ship, startRow, startCol, direction)) {
                    ship.positions = calculatePositions(ship, startRow, startCol, direction)
                    placedShips.add(ship)
                    Log.d("ShipPlacement", "Successfully placed ${ship.name} at ${ship.positions}")
                    placed = true
                } else {
                    Log.d("ShipPlacement", "Failed to place ${ship.name} at ($startRow, $startCol) with direction $direction")
                }
                attempts++
            }
            if (!placed) {
                Log.d("ShipPlacement", "Failed to place ${ship.name} after $attempts attempts")
            }
        }
        Log.d("ShipPlacement", "Final Ship Placements: ${placedShips.map { it.name to it.positions }}")
        return placedShips
    }


    private fun canPlaceShip(ship: Ship, startRow: Int, startCol: Int, direction: String): Boolean {
        val newPositions = calculatePositions(ship, startRow, startCol, direction)
        val existingShipPositions = ships.flatMap { it.positions }
        val adjacentPositions = newPositions.flatMap { (row, col) ->
            listOf(
                Pair(row - 1, col), Pair(row + 1, col), Pair(row, col - 1), Pair(row, col + 1),
                Pair(row - 1, col - 1), Pair(row - 1, col + 1), Pair(row + 1, col - 1), Pair(row + 1, col + 1)
            )
        }
        // Debug: Log the validation process
        Log.d("ShipPlacement", "Validating ${ship.name}: Positions=$newPositions")
        Log.d("ShipPlacement", "Existing ship positions: $existingShipPositions")
        Log.d("ShipPlacement", "Adjacent positions to check: $adjacentPositions")

        if (newPositions.any { it.first !in 0 until boardSize || it.second !in 0 until boardSize }) {
            Log.d("ShipPlacement", "${ship.name} is out of bounds")
            return false
        }
        if (newPositions.any { it in existingShipPositions }) {
            Log.d("ShipPlacement", "${ship.name} overlaps with another ship")
            return false
        }
        if (adjacentPositions.any { it in existingShipPositions }) {
            Log.d("ShipPlacement", "${ship.name} is too close to another ship")
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
            "H" -> (0 until ship.length).map { Pair(startRow, startCol + it) } // Horizontal
            "V" -> (0 until ship.length).map { Pair(startRow + it, startCol) } // Vertical
            else -> emptyList()
        }
    }}

