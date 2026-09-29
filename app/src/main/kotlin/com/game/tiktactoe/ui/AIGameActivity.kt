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
import com.game.tiktactoe.services.AIDifficulty
import com.game.tiktactoe.services.GameStateManager
import com.game.tiktactoe.services.LocalAI
import com.game.tiktactoe.services.MatchHistoryService
import com.game.tiktactoe.services.WinChecker

class AIGameActivity : AppCompatActivity() {

    private lateinit var playerName: String
    private lateinit var difficulty: AIDifficulty
    private var matchCount = 1

    private val board = MutableList(9) { "" }
    private var gameActive = false
    private var isAITurn = false
    private val scores = mutableMapOf("Human" to 0, "AI" to 0)
    private var winningCells: IntArray? = null
    private var matchesPlayed = 0
    private var tournamentWinner: String? = null
    private var turnOrder: List<Boolean>? = null // true = AI goes first

    private lateinit var rootContainer: FrameLayout
    private lateinit var contentScroll: ScrollView
    private lateinit var statusText: android.widget.TextView
    private lateinit var scoreCardHuman: ScoreCardView
    private lateinit var scoreCardAI: ScoreCardView
    private lateinit var cells: Array<SquareCell>
    private lateinit var thinkingProgress: View
    private lateinit var actionButtons: View

    private val handler = Handler(Looper.getMainLooper())
    private var shuffleView: TurnShuffleView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        playerName = intent.getStringExtra(MainMenuActivity.EXTRA_PLAYER_NAME) ?: "Player"
        difficulty = AIDifficulty.valueOf(intent.getStringExtra(AIDifficultyActivity.EXTRA_DIFFICULTY) ?: AIDifficulty.MEDIUM.name)
        matchCount = intent.getIntExtra(MainMenuActivity.EXTRA_MATCH_COUNT, 1)

        rootContainer = findViewById(R.id.rootContainer)
        contentScroll = findViewById(R.id.contentScroll)
        statusText = findViewById(R.id.statusText)
        scoreCardHuman = findViewById(R.id.scoreCardLeft)
        scoreCardAI = findViewById(R.id.scoreCardRight)
        thinkingProgress = findViewById(R.id.thinkingProgress)
        actionButtons = findViewById(R.id.actionButtons)

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
        turnOrder = (if ((0..1).random() == 0) listOf(false, true, true) else listOf(true, false, false)).shuffled()
    }

    private fun showShuffle() {
        contentScroll.visibility = View.INVISIBLE
        val predetermined = turnOrder?.let { !it[matchesPlayed] }
        val view = TurnShuffleView(
            this,
            color1 = Shapes.colorRes(this, R.color.brand_purple),
            color2 = Shapes.colorRes(this, R.color.brand_orange),
            player1Name = playerName,
            player2Name = "AI",
            predeterminedResult = predetermined,
            onComplete = ::onShuffleComplete
        )
        shuffleView = view
        rootContainer.addView(view, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    private fun onShuffleComplete(color1Wins: Boolean) {
        isAITurn = turnOrder?.get(matchesPlayed) ?: !color1Wins
        for (i in board.indices) board[i] = ""
        gameActive = true
        winningCells = null

        shuffleView?.let { rootContainer.removeView(it) }
        shuffleView = null
        contentScroll.visibility = View.VISIBLE

        renderBoard()
        updateScoreCards()
        updateStatusText()
        updateTurnUi()

        if (isAITurn) {
            handler.postDelayed({ if (gameActive) makeAIMove() }, 500)
        }
    }

    private fun updateStatusText() {
        statusText.text = if (isAITurn) getString(R.string.ai_turn) else getString(R.string.your_turn_x)
    }

    private fun updateScoreCards() {
        scoreCardHuman.bind(playerName, scores["Human"] ?: 0, Shapes.colorRes(this, R.color.brand_purple), !isAITurn)
        scoreCardAI.bind("AI", scores["AI"] ?: 0, Shapes.colorRes(this, R.color.brand_orange), isAITurn)
    }

    private fun updateTurnUi() {
        thinkingProgress.visibility = if (isAITurn) View.VISIBLE else View.GONE
        actionButtons.visibility = if (isAITurn) View.GONE else View.VISIBLE
    }

    private fun renderBoard() {
        for (i in cells.indices) {
            val cell = cells[i]
            val value = board[i]
            cell.text = value
            cell.setTextColor(
                if (value == "X") Shapes.colorRes(this, R.color.brand_indigo)
                else Shapes.colorRes(this, R.color.brand_amber_custom)
            )
            cell.textSize = 32f
            val isWinning = winningCells?.contains(i) == true
            val fill = if (isWinning) Shapes.colorWithAlpha(Shapes.colorRes(this, R.color.brand_green_win), 0.4f)
                else Shapes.colorRes(this, R.color.color_surface_alt)
            val stroke = if (isWinning) Shapes.colorRes(this, R.color.brand_green_win)
                else Shapes.colorRes(this, R.color.color_cell_border)
            cell.background = Shapes.roundedRect(this, 8f, fill, stroke, if (isWinning) 2.5f else 1f)
        }
    }

    private fun handleCellClick(index: Int) {
        if (!gameActive || isAITurn || board[index].isNotEmpty()) return
        board[index] = "X"
        renderBoard()
        if (checkResult()) return

        isAITurn = true
        updateStatusText()
        updateScoreCards()
        updateTurnUi()

        handler.postDelayed({ if (gameActive) makeAIMove() }, 500)
    }

    private fun makeAIMove() {
        val move = LocalAI.getAIMove(board, "O", "X", difficulty)
        if (move == -1 || board[move].isNotEmpty()) return
        board[move] = "O"
        isAITurn = false
        renderBoard()
        checkResult()
    }

    /** Returns true if the round/tournament ended (win or draw). */
    private fun checkResult(): Boolean {
        val line = WinChecker.findWinningLine(board)
        if (line != null) {
            winningCells = line
            renderBoard()
            gameActive = false
            matchesPlayed++

            val humanWon = board[line[0]] == "X"
            if (humanWon) {
                scores["Human"] = (scores["Human"] ?: 0) + 1
                GameStateManager.incrementHumanWin()
            } else {
                scores["AI"] = (scores["AI"] ?: 0) + 1
                GameStateManager.incrementAIWin()
            }
            updateScoreCards()
            updateTurnUi()

            // Mirrors the original: the majority-win short-circuit only applies to
            // multi-match tournaments. A matchCount == 1 game always goes through
            // showRoundResult, whose button then routes straight back to the main menu.
            if (matchCount > 1) {
                val humanWins = scores["Human"] ?: 0
                val aiWins = scores["AI"] ?: 0
                if (humanWins > matchCount / 2) {
                    tournamentWinner = playerName
                    showTournamentResult()
                    return true
                } else if (aiWins > matchCount / 2) {
                    tournamentWinner = "AI"
                    showTournamentResult()
                    return true
                }
            }

            showRoundResult(
                title = if (humanWon) getString(R.string.wins_title, playerName) else getString(R.string.ai_wins_title),
                message = if (humanWon) getString(R.string.you_win_message) else getString(R.string.ai_wins_message),
                outcome = if (humanWon) Outcome.HUMAN_WIN else Outcome.AI_WIN
            )
            return true
        }

        if (WinChecker.isBoardFull(board)) {
            gameActive = false
            matchesPlayed++
            updateTurnUi()
            showRoundResult(
                title = getString(R.string.draw_title),
                message = getString(R.string.draw_message),
                outcome = Outcome.DRAW
            )
            return true
        }

        return false
    }

    private enum class Outcome { HUMAN_WIN, AI_WIN, DRAW }

    private fun difficultyText(): String = when (difficulty) {
        AIDifficulty.EASY -> "easy"
        AIDifficulty.MEDIUM -> "medium"
        AIDifficulty.HARD -> "hard"
    }

    private fun showRoundResult(title: String, message: String, outcome: Outcome) {
        // Single-match rounds are saved here (mirrors the original's matchCount == 1 gate);
        // multi-match tournaments are only saved once, at the very end.
        if (matchCount == 1) {
            val winner = when (outcome) {
                Outcome.HUMAN_WIN -> playerName
                Outcome.AI_WIN -> "AI"
                Outcome.DRAW -> "Draw"
            }
            MatchHistoryService.addMatch(
                this,
                MatchRecord(
                    id = System.currentTimeMillis().toString(),
                    gameType = "ai",
                    player1Name = playerName,
                    player2Name = "AI",
                    winner = winner,
                    player1Score = scores["Human"] ?: 0,
                    player2Score = scores["AI"] ?: 0,
                    matchCount = matchCount,
                    difficulty = difficultyText(),
                    dateTimeMillis = System.currentTimeMillis()
                )
            )
        }

        handler.postDelayed({
            if (isFinishing) return@postDelayed
            val scoreText = getString(R.string.score_label, scores["Human"] ?: 0, scores["AI"] ?: 0)
            val matchesText = if (matchCount > 1) getString(R.string.matches_played, matchesPlayed, matchCount) else null
            val extra = GameDialogs.buildScoreBadge(this, scoreText, matchesText)

            val iconRes = when (outcome) {
                Outcome.HUMAN_WIN -> R.drawable.ic_trophy
                Outcome.AI_WIN -> R.drawable.ic_smart_toy
                Outcome.DRAW -> R.drawable.ic_handshake
            }
            val accent = when (outcome) {
                Outcome.HUMAN_WIN -> Shapes.colorRes(this, R.color.brand_green)
                Outcome.AI_WIN -> Shapes.colorRes(this, R.color.brand_red)
                Outcome.DRAW -> Shapes.colorRes(this, R.color.brand_orange)
            }

            val showNextMatch = matchCount > 1 && matchesPlayed < matchCount
            val buttonLabel = if (showNextMatch) getString(R.string.next_match) else getString(R.string.main_menu)

            GameDialogs.buildDialog(
                context = this,
                iconRes = iconRes,
                accentColor = accent,
                title = title,
                message = message,
                extraContent = extra,
                buttonLabel = buttonLabel,
                buttonColor = Shapes.colorRes(this, R.color.brand_purple)
            ) {
                if (showNextMatch) {
                    showShuffle()
                } else if (matchCount > 1) {
                    val humanWins = scores["Human"] ?: 0
                    val aiWins = scores["AI"] ?: 0
                    tournamentWinner = when {
                        humanWins > aiWins -> playerName
                        aiWins > humanWins -> "AI"
                        else -> null
                    }
                    showTournamentResult()
                } else {
                    goToMainMenu()
                }
            }.show()
        }, 1000)
    }

    private fun showTournamentResult() {
        val winner = tournamentWinner
        val winnerMessage = when {
            winner == "AI" -> getString(R.string.tournament_ai_winner)
            winner != null -> getString(R.string.tournament_winner, winner)
            else -> getString(R.string.tournament_draw)
        }

        MatchHistoryService.addMatch(
            this,
            MatchRecord(
                id = System.currentTimeMillis().toString(),
                gameType = "ai",
                player1Name = playerName,
                player2Name = "AI",
                winner = winner ?: "Draw",
                player1Score = scores["Human"] ?: 0,
                player2Score = scores["AI"] ?: 0,
                matchCount = matchCount,
                difficulty = difficultyText(),
                dateTimeMillis = System.currentTimeMillis()
            )
        )

        handler.postDelayed({
            if (isFinishing) return@postDelayed
            val isHumanWin = winner != null && winner != "AI"
            val isAiWin = winner == "AI"
            val iconRes = if (isAiWin) R.drawable.ic_smart_toy else R.drawable.ic_trophy
            val accent = when {
                isHumanWin -> Shapes.colorRes(this, R.color.brand_amber)
                isAiWin -> Shapes.colorRes(this, R.color.brand_red)
                else -> Shapes.colorRes(this, R.color.brand_grey_500)
            }
            val extra = GameDialogs.buildFinalScoreBox(this, playerName, scores["Human"] ?: 0, "AI", scores["AI"] ?: 0)

            GameDialogs.buildDialog(
                context = this,
                iconRes = iconRes,
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
        }, 1000)
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
