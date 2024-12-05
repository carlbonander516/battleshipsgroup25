package com.example.battleshipsgroup25

import kotlin.random.Random

class RuleEngine private constructor(private val boardSize: Int, private val ships: List<Ship>) {
    private val shotResults = mutableMapOf<Pair<Int, Int>, Boolean>()
    private val botState = BotState(boardSize, shotResults, ships)

    fun handleCellClick(row: Int, col: Int): Boolean {
        println("Cell clicked at: ($row, $col)")

        // Check if this cell has already been attacked
        shotResults[row to col]?.let {
            println("Cell ($row, $col) was already attacked. Result: ${if (it) "Hit" else "Miss"}")
            return it
        }

        // Check if the cell contains a part of a ship
        val shipHit = checkShipHit(row, col)
        if (shipHit != null) {
            shotResults[row to col] = true // Mark the cell as hit
            println("Hit! Ship: ${shipHit.name} at ($row, $col)")
            return true
        } else {
            shotResults[row to col] = false // Mark the cell as missed
            println("Missed at ($row, $col)")
            return false
        }
    }

    private fun checkShipHit(row: Int, col: Int): Ship? {
        return ships.find { ship -> Pair(row, col) in ship.positions }
    }

    fun botAttack(): Pair<Pair<Int, Int>, Boolean> {
        val (row, col) = botState.getNextTarget()
        println("Bot attacking at: ($row, $col)")

        val hit = handleCellClick(row, col)
        if (hit) {
            botState.registerHit(row, col)
        } else {
            botState.registerMiss(row, col)
        }

        return Pair(Pair(row, col), hit)
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

        fun botAttack(): Pair<Pair<Int, Int>, Boolean> {
            return instance?.botAttack()
                ?: error("RuleEngine is not initialized!")
        }
    }

    // BotState class to manage the bot's attack strategy
    private class BotState(
        private val boardSize: Int,
        private val shotResults: MutableMap<Pair<Int, Int>, Boolean>,
        private val ships: List<Ship>
    ) {
        private var mode = Mode.HUNT
        private val hitQueue = mutableListOf<Pair<Int, Int>>()
        private var huntIndex = 0 // Index for systematic 2x2 hunting

        fun getNextTarget(): Pair<Int, Int> {
            println("Current Bot Mode: ${mode.name}")
            return when (mode) {
                Mode.HUNT -> get2x2Target()
                Mode.TARGET -> getTargetedAttack()
            }
        }

        fun registerHit(row: Int, col: Int) {
            println("Registering hit at ($row, $col). Switching to Target Mode.")
            mode = Mode.TARGET
            addAdjacentCellsToQueue(row, col)
        }

        fun registerMiss(row: Int, col: Int) {
            println("Registering miss at ($row, $col).")
            if (mode == Mode.TARGET && hitQueue.isEmpty()) {
                println("No more adjacent targets. Returning to Hunt Mode.")
                mode = Mode.HUNT
            }
        }

        private fun get2x2Target(): Pair<Int, Int> {
            val totalCells = boardSize * boardSize
            while (huntIndex < totalCells) {
                val row = (huntIndex / boardSize) * 2
                val col = (huntIndex % boardSize) * 2
                huntIndex++

                if (row < boardSize && col < boardSize && !shotResults.containsKey(Pair(row, col))) {
                    println("Hunt Mode targeting 2x2 grid: ($row, $col)")
                    return Pair(row, col)
                }
            }
            println("No valid 2x2 targets remaining. Falling back to random target.")
            return getRandomTarget()
        }

        private fun getTargetedAttack(): Pair<Int, Int> {
            if (hitQueue.isNotEmpty()) {
                val nextTarget = hitQueue.removeAt(0)
                if (!shotResults.containsKey(nextTarget)) {
                    println("Target Mode attacking adjacent cell: $nextTarget")
                    return nextTarget
                }
            }
            println("No valid adjacent targets. Returning to Hunt Mode.")
            mode = Mode.HUNT
            return get2x2Target()
        }

        private fun addAdjacentCellsToQueue(row: Int, col: Int) {
            val potentialTargets = listOf(
                Pair(row - 1, col), // Up
                Pair(row + 1, col), // Down
                Pair(row, col - 1), // Left
                Pair(row, col + 1)  // Right
            ).filter {
                it.first in 0 until boardSize && it.second in 0 until boardSize && !shotResults.containsKey(it)
            }

            println("Adding adjacent cells to queue: $potentialTargets")
            hitQueue.addAll(potentialTargets)
        }

        private fun getRandomTarget(): Pair<Int, Int> {
            var target: Pair<Int, Int>
            do {
                target = Pair(Random.nextInt(0, boardSize), Random.nextInt(0, boardSize))
            } while (shotResults.containsKey(target))
            println("Random target selected: $target")
            return target
        }

        private enum class Mode {
            HUNT, TARGET
        }
    }
}
