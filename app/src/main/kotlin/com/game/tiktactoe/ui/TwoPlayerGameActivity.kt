package com.game.tiktactoe.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.game.tiktactoe.R
import com.game.tiktactoe.model.MatchRecord
import com.game.tiktactoe.services.GameStateManager
import com.game.tiktactoe.services.MatchHistoryService
import com.game.tiktactoe.services.WinChecker

class TwoPlayerGameActivity : AppCompatActivity() {

    private lateinit var playerXName: String
    private lateinit var playerOName: String
    private var matchCount = 1

    private val board = MutableList(9) { "" }
    private lateinit var currentPlayer: String
    private var gameActive = false
    private val scores = mutableMapOf("X" to 0, "O" to 0)
    private var winningCells: IntArray? = null
    private var matchesPlayed = 0
    private var tournamentWinner: String? = null
    private var turnOrder: List<String>? = null

    private lateinit var rootContainer: FrameLayout
    private lateinit var contentScroll: ScrollView
    private lateinit var statusText: android.widget.TextView
    private lateinit var scoreCardX: ScoreCardView
    private lateinit var scoreCardO: ScoreCardView
    private lateinit var cells: Array<SquareCell>

    private val handler = Handler(Looper.getMainLooper())
    private var shuffleView: TurnShuffleView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        playerXName = intent.getStringExtra(MainMenuActivity.EXTRA_PLAYER_X) ?: "Player X"
        playerOName = intent.getStringExtra(MainMenuActivity.EXTRA_PLAYER_O) ?: "Player O"
        matchCount = intent.getIntExtra(MainMenuActivity.EXTRA_MATCH_COUNT, 1)

        rootContainer = findViewById(R.id.rootContainer)
        contentScroll = findViewById(R.id.contentScroll)
        statusText = findViewById(R.id.statusText)
        scoreCardX = findViewById(R.id.scoreCardLeft)
        scoreCardO = findViewById(R.id.scoreCardRight)
        findViewById<View>(R.id.thinkingProgress).visibility = View.GONE

        ThemeToggle.bind(this, findViewById<ImageButton>(R.id.themeToggleButton))

        cells = arrayOf(
            findViewById(R.id.cell0), findViewById(R.id.cell1), findViewById(R.id.cell2),
            findViewById(R.id.cell3), findViewById(R.id.cell4), findViewById(R.id.cell5),
            findViewById(R.id.cell6), findViewById(R.id.cell7), findViewById(R.id.cell8)
        )
        cells.forEachIndexed { index, cell -> cell.setOnClickListener { handleCellClick(index) } }

        val resetButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.resetButton)
        val mainMenuButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.mainMenuButton)
        Shapes.styleButtonIcon(resetButton, R.drawable.ic_refresh)
        Shapes.styleButtonIcon(mainMenuButton, R.drawable.ic_home)
        resetButton.setOnClickListener { showShuffle() }
        mainMenuButton.setOnClickListener { goToMainMenu() }

        if (matchCount == 3) initializeTurnOrder()
        showShuffle()
    }

    private fun initializeTurnOrder() {
        turnOrder = (if ((0..1).random() == 0) listOf("X", "O", "O") else listOf("O", "X", "X")).shuffled()
    }

    private fun showShuffle() {
        contentScroll.visibility = View.INVISIBLE
        val predetermined = turnOrder?.let { it[matchesPlayed] == "X" }
        val view = TurnShuffleView(
            this,
            color1 = Shapes.colorRes(this, R.color.brand_purple),
            color2 = Shapes.colorRes(this, R.color.brand_orange),
            player1Name = playerXName,
            player2Name = playerOName,
            predeterminedResult = predetermined,
            onComplete = ::onShuffleComplete
        )
        shuffleView = view
        rootContainer.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun onShuffleComplete(color1Wins: Boolean) {
        currentPlayer = turnOrder?.get(matchesPlayed) ?: (if (color1Wins) "X" else "O")
        for (i in board.indices) board[i] = ""
        gameActive = true
        winningCells = null

        shuffleView?.let { rootContainer.removeView(it) }
        shuffleView = null
        contentScroll.visibility = View.VISIBLE

        renderBoard()
        updateScoreCards()
        updateStatusText()
    }

    private fun updateStatusText() {
        val name = if (currentPlayer == "X") playerXName else playerOName
        statusText.text = getString(R.string.turn_of, name)
    }

    private fun updateScoreCards() {
        scoreCardX.bind(playerXName, scores["X"] ?: 0, Shapes.colorRes(this, R.color.brand_purple), currentPlayer == "X")
        scoreCardO.bind(playerOName, scores["O"] ?: 0, Shapes.colorRes(this, R.color.brand_orange), currentPlayer == "O")
    }

    private fun renderBoard() {
        for (i in cells.indices) {
            val cell = cells[i]
            val value = board[i]
            cell.text = value
            val isWinning = winningCells?.contains(i) == true
            cell.setTextColor(
                if (value == "X") Shapes.colorRes(this, R.color.brand_indigo)
                else Shapes.colorRes(this, R.color.brand_amber_custom)
            )
            cell.textSize = 32f
            val fill = if (isWinning) Shapes.colorWithAlpha(Shapes.colorRes(this, R.color.brand_green_win), 0.3f)
                else Shapes.colorRes(this, R.color.color_surface)
            val stroke = if (isWinning) Shapes.colorRes(this, R.color.brand_green_win)
                else Shapes.colorRes(this, R.color.color_cell_border)
            cell.background = Shapes.roundedRect(this, 16f, fill, stroke, if (isWinning) 2.5f else 1.5f)
        }
    }

    private fun handleCellClick(index: Int) {
        if (!gameActive || board[index].isNotEmpty()) return
        board[index] = currentPlayer
        renderBoard()
        checkResult()
    }

    private fun checkResult() {
        val line = WinChecker.findWinningLine(board)
        if (line != null) {
            winningCells = line
            renderBoard()

            scores[currentPlayer] = (scores[currentPlayer] ?: 0) + 1
            gameActive = false
            matchesPlayed++
            if (currentPlayer == "X") GameStateManager.incrementPlayerXWin() else GameStateManager.incrementPlayerOWin()
            updateScoreCards()

            val winnerName = if (currentPlayer == "X") playerXName else playerOName
            if ((scores[currentPlayer] ?: 0) > matchCount / 2) {
                tournamentWinner = winnerName
                showTournamentResult()
            } else {
                showRoundResult(
                    title = getString(R.string.wins_title, winnerName),
                    message = getString(R.string.wins_message, winnerName),
                    isWin = true
                )
            }
            return
        }

        if (WinChecker.isBoardFull(board)) {
            matchesPlayed++
            gameActive = false
            showRoundResult(
                title = getString(R.string.draw_title),
                message = getString(R.string.draw_message),
                isWin = false
            )
            return
        }

        currentPlayer = if (currentPlayer == "X") "O" else "X"
        updateScoreCards()
        updateStatusText()
    }

    private fun showRoundResult(title: String, message: String, isWin: Boolean) {
        handler.postDelayed({
            if (isFinishing) return@postDelayed
            val scoreText = "$playerXName: ${scores["X"]} - $playerOName: ${scores["O"]}"
            val matchesText = if (matchCount > 1) getString(R.string.matches_played, matchesPlayed, matchCount) else null
            val extra = GameDialogs.buildScoreBadge(this, scoreText, matchesText)

            val iconRes = if (isWin) R.drawable.ic_trophy else R.drawable.ic_handshake
            val accent = if (isWin) Shapes.colorRes(this, R.color.brand_green) else Shapes.colorRes(this, R.color.brand_orange)

            GameDialogs.buildDialog(
                context = this,
                iconRes = iconRes,
                accentColor = accent,
                title = title,
                message = message,
                extraContent = extra,
                buttonLabel = getString(R.string.continue_label),
                buttonColor = Shapes.colorRes(this, R.color.brand_purple)
            ) {
                if (matchesPlayed < matchCount) {
                    showShuffle()
                } else {
                    val xScore = scores["X"] ?: 0
                    val oScore = scores["O"] ?: 0
                    tournamentWinner = when {
                        xScore > oScore -> playerXName
                        oScore > xScore -> playerOName
                        else -> null
                    }
                    showTournamentResult()
                }
            }.show()
        }, 1000)
    }

    private fun showTournamentResult() {
        val winner = tournamentWinner
        val winnerMessage = if (winner != null) getString(R.string.tournament_winner, winner) else getString(R.string.tournament_draw)

        MatchHistoryService.addMatch(
            this,
            MatchRecord(
                id = System.currentTimeMillis().toString(),
                gameType = "human",
                player1Name = playerXName,
                player2Name = playerOName,
                winner = winner ?: "Draw",
                player1Score = scores["X"] ?: 0,
                player2Score = scores["O"] ?: 0,
                matchCount = matchCount,
                difficulty = "",
                dateTimeMillis = System.currentTimeMillis()
            )
        )

        handler.postDelayed({
            if (isFinishing) return@postDelayed
            val isWin = winner != null
            val extra = GameDialogs.buildFinalScoreBox(this, playerXName, scores["X"] ?: 0, playerOName, scores["O"] ?: 0)
            val accent = if (isWin) Shapes.colorRes(this, R.color.brand_amber) else Shapes.colorRes(this, R.color.brand_grey_500)

            GameDialogs.buildDialog(
                context = this,
                iconRes = R.drawable.ic_trophy,
                accentColor = accent,
                iconSizeDp = 56,
                title = getString(R.string.tournament_over),
                message = winnerMessage,
                extraContent = extra,
                buttonLabel = getString(R.string.main_menu),
                buttonColor = Shapes.colorRes(this, R.color.brand_orange),
                buttonIcon = R.drawable.ic_home
            ) {
                goToMainMenu()
            }.show()
        }, 4000)
    }

    private fun goToMainMenu() {
        startActivity(Intent(this, MainMenuActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        shuffleView?.cancelPendingCallbacks()
        super.onDestroy()
    }
}
