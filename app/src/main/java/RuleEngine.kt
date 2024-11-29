package com.example.battleshipsgroup25

class RuleEngine private constructor(private val boardSize: Int, private val ships: List<Ship>) {
    private val shotResults = mutableMapOf<Pair<Int, Int>, Boolean>()

    fun handleCellClick(row: Int, col: Int): Boolean {
        println("Cell clicked at: ($row, $col)")
        shotResults[row to col]?.let { return it }

        val shipHit = checkShipHit(row, col)
        return if (shipHit != null) {
            shotResults[row to col] = true
            println("Hit a ship: ${shipHit.name}")
            true
        } else {
            shotResults[row to col] = false
            println("Missed!")
            false
        }
    }

    private fun checkShipHit(row: Int, col: Int): Ship? {
        return ships.find { ship -> Pair(row, col) in ship.positions }
    }

    companion object {
        private var instance: RuleEngine? = null

        fun initialize(boardSize: Int, ships: List<Ship>) {
            instance = RuleEngine(boardSize, ships)
        }

        fun handleCellClick(row: Int, col: Int): Boolean {
            return instance?.handleCellClick(row, col)
                ?: error("RuleEngine is not initialized!")
        }
    }
}
