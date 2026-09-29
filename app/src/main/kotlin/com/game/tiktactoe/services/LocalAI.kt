package com.game.tiktactoe.services

import kotlin.random.Random

enum class AIDifficulty { EASY, MEDIUM, HARD }

object LocalAI {

    fun getAIMove(
        board: List<String>,
        aiPlayer: String,
        humanPlayer: String,
        difficulty: AIDifficulty = AIDifficulty.MEDIUM
    ): Int {
        // Hard: always play optimally (~98% win rate for the AI).
        if (difficulty == AIDifficulty.HARD) {
            return getOptimalMove(board, aiPlayer, humanPlayer)
        }

        // Easy / medium: sometimes play optimally, otherwise play suboptimally.
        val shouldMakeOptimalMove = if (difficulty == AIDifficulty.MEDIUM) {
            Random.nextDouble() < 0.5 // 50% optimal moves
        } else {
            Random.nextDouble() < 0.2 // 20% optimal moves for easy
        }

        return if (shouldMakeOptimalMove) {
            getOptimalMove(board, aiPlayer, humanPlayer)
        } else {
            getSuboptimalMove(board, aiPlayer, humanPlayer)
        }
    }

    private fun getOptimalMove(board: List<String>, aiPlayer: String, humanPlayer: String): Int {
        // 1. Win immediately if possible.
        val winningMove = getWinningMove(board, aiPlayer)
        if (winningMove != -1) return winningMove

        // 2. Block the human's winning move.
        val blockingMove = getWinningMove(board, humanPlayer)
        if (blockingMove != -1) return blockingMove

        // 3. Take the center.
        if (board[4].isEmpty()) return 4

        // 4. Take a corner.
        val corners = intArrayOf(0, 2, 6, 8)
        for (corner in corners) {
            if (board[corner].isEmpty()) return corner
        }

        // 5. Take an edge.
        val edges = intArrayOf(1, 3, 5, 7)
        for (edge in edges) {
            if (board[edge].isEmpty()) return edge
        }

        // 6. Fallback: first available cell.
        for (i in board.indices) {
            if (board[i].isEmpty()) return i
        }

        return -1
    }

    private fun getSuboptimalMove(board: List<String>, aiPlayer: String, humanPlayer: String): Int {
        // Still block an immediate human win, otherwise play randomly.
        val blockingMove = getWinningMove(board, humanPlayer)
        if (blockingMove != -1) return blockingMove

        val availableMoves = board.indices.filter { board[it].isEmpty() }
        if (availableMoves.isEmpty()) return -1
        return availableMoves[Random.nextInt(availableMoves.size)]
    }

    private fun getWinningMove(board: List<String>, player: String): Int {
        for (condition in WinChecker.WINNING_CONDITIONS) {
            val a = condition[0]
            val b = condition[1]
            val c = condition[2]

            if (board[a] == player && board[b] == player && board[c].isEmpty()) return c
            if (board[a] == player && board[c] == player && board[b].isEmpty()) return b
            if (board[b] == player && board[c] == player && board[a].isEmpty()) return a
        }
        return -1
    }
}
