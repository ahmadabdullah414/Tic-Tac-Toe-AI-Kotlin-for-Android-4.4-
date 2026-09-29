package com.game.tiktactoe.services

/** In-memory session win counters (reset on app start, not persisted). */
object GameStateManager {
    var humanWins: Int = 0
        private set
    var aiWins: Int = 0
        private set
    var playerXWins: Int = 0
        private set
    var playerOWins: Int = 0
        private set

    fun incrementHumanWin() { humanWins++ }
    fun incrementAIWin() { aiWins++ }
    fun incrementPlayerXWin() { playerXWins++ }
    fun incrementPlayerOWin() { playerOWins++ }

    fun resetAll() {
        humanWins = 0
        aiWins = 0
        playerXWins = 0
        playerOWins = 0
    }
}
