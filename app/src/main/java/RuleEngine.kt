import com.example.battleshipsgroup25.Ship

// RuleEngine.kt

class RuleEngine(private val boardSize: Int, private val ships: List<Ship>) {

    // Track the results of shots (hit or miss) by storing the cell coordinates and the outcome
    private val shotResults = mutableMapOf<Pair<Int, Int>, Boolean>() // True for hit, False for miss

    // Function to handle a cell click and determine whether it's a hit or miss
    fun handleCellClick(row: Int, col: Int): Boolean {
        println("Cell clicked at: ($row, $col)")

        // If the shot was already taken, return the previous result
        shotResults[row to col]?.let {
            return it // Return true if it's a hit, false if it's a miss
        }

        // Check if the shot hits a ship
        val shipHit = checkShipHit(row, col)
        if (shipHit != null) {
            // Mark this cell as a hit
            shotResults[row to col] = true
            println("Hit a ship: ${shipHit.name}")
            return true
        } else {
            // Mark this cell as a miss
            shotResults[row to col] = false
            println("Missed!")
            return false
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
        fun handleCellClick(row: Int, col: Int): Boolean {
            return ruleEngineInstance?.handleCellClick(row, col)
                ?: run {
                    println("RuleEngine is not initialized!")
                    false
                }
        }

        // Get shot result for a specific cell (hit or miss)
        fun getShotResult(row: Int, col: Int): Boolean? {
            return ruleEngineInstance?.shotResults?.get(Pair(row, col))
        }
    }
}
