package com.example.battleshipsgroup25

class RuleEngine(private val boardSize: Int, private val ships: List<Ship>) {

    // Grid to keep track of cell clicks (can be extended to store hit/miss state)
    private val clickedCells = mutableSetOf<Pair<Int, Int>>()

    // Function to handle a cell click
    fun handleCellClick(row: Int, col: Int) {
        // Record that the cell was clicked
        clickedCells.add(Pair(row, col))

        // Handle the game logic, for example, checking if a ship is in this cell
        // Here you can call methods that check whether this cell is part of a ship's position
        println("Cell clicked at: ($row, $col)")

        // For example, checking if there's a ship on the clicked cell:
        val shipHit = checkShipHit(row, col)
        if (shipHit != null) {
            println("Hit a ship: ${shipHit.name}")
        } else {
            println("Missed!")
        }
    }

    // Function to check if a ship is at the clicked position
    private fun checkShipHit(row: Int, col: Int): Ship? {
        // Loop through all ships to check if the clicked position matches any ship's position
        for (ship in ships) {
            if (ship.positions.contains(Pair(row, col))) {
                return ship
            }
        }
        return null
    }

    companion object {
        private var ruleEngineInstance: RuleEngine? = null

        // Initialize or get the existing instance
        fun initialize(boardSize: Int, ships: List<Ship>) {
            ruleEngineInstance = RuleEngine(boardSize, ships)
        }

        // Forward cell click to the instance
        fun handleCellClick(row: Int, col: Int) {
            ruleEngineInstance?.handleCellClick(row, col) ?:
            println("RuleEngine is not initialized!")
        }
    }
}