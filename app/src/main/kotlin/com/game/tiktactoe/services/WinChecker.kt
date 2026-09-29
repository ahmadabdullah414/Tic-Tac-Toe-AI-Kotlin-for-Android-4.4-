package com.game.tiktactoe.services

object WinChecker {
    val WINNING_CONDITIONS: Array<IntArray> = arrayOf(
        intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8), // rows
        intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8), // columns
        intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)                       // diagonals
    )

    /** Returns the winning triple of cell indices, or null if no winner yet. */
    fun findWinningLine(board: List<String>): IntArray? {
        for (condition in WINNING_CONDITIONS) {
            val (a, b, c) = Triple(condition[0], condition[1], condition[2])
            if (board[a].isEmpty() || board[b].isEmpty() || board[c].isEmpty()) continue
            if (board[a] == board[b] && board[b] == board[c]) return condition
        }
        return null
    }

    fun isBoardFull(board: List<String>): Boolean = board.none { it.isEmpty() }
}
