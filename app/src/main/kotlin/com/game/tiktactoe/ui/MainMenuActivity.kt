package com.game.tiktactoe.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.game.tiktactoe.R

class MainMenuActivity : AppCompatActivity() {

    private var matchCount: Int? = 1
    private lateinit var playerXInput: EditText
    private lateinit var playerOInput: EditText
    private lateinit var chipSingle: TextView
    private lateinit var chipBest3: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)

        ThemeToggle.bind(this, findViewById<ImageButton>(R.id.themeToggleButton))

        playerXInput = findViewById(R.id.playerXInput)
        playerOInput = findViewById(R.id.playerOInput)
        styleInput(playerXInput, R.color.brand_indigo)
        styleInput(playerOInput, R.color.brand_amber_custom)

        if (playerXInput.text.isNullOrEmpty()) playerXInput.setText("Player X")
        if (playerOInput.text.isNullOrEmpty()) playerOInput.setText("Player O")

        chipSingle = findViewById(R.id.chipSingleMatch)
        chipBest3 = findViewById(R.id.chipBestOf3)
        matchCount = 1
        styleChips()

        chipSingle.setOnClickListener {
            matchCount = if (matchCount == 1) null else 1
            styleChips()
        }
        chipBest3.setOnClickListener {
            matchCount = if (matchCount == 3) null else 3
            styleChips()
        }

        val startGameButton = findViewById<MaterialButton>(R.id.startGameButton)
        val playWithAiButton = findViewById<MaterialButton>(R.id.playWithAiButton)
        val matchHistoryButton = findViewById<MaterialButton>(R.id.matchHistoryButton)
        Shapes.styleButtonIcon(startGameButton, R.drawable.ic_play_arrow, cornerRadiusDp = 16)
        Shapes.styleButtonIcon(playWithAiButton, R.drawable.ic_smart_toy, cornerRadiusDp = 16)
        Shapes.styleButtonIcon(matchHistoryButton, R.drawable.ic_history, cornerRadiusDp = 16)

        startGameButton.setOnClickListener { startPvpGame() }
        playWithAiButton.setOnClickListener { startAiFlow() }
        matchHistoryButton.setOnClickListener {
            startActivity(Intent(this, MatchHistoryActivity::class.java))
        }
    }

    private fun styleInput(input: EditText, iconTintRes: Int) {
        input.background = Shapes.roundedRect(this, 16f, Shapes.colorRes(this, R.color.color_surface))
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_person)?.mutate()
        icon?.setTint(Shapes.colorRes(this, iconTintRes))
        input.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
    }

    private fun styleChips() {
        styleChip(chipSingle, matchCount == 1)
        styleChip(chipBest3, matchCount == 3)
    }

    private fun styleChip(chip: TextView, selected: Boolean) {
        if (selected) {
            chip.background = Shapes.roundedRect(this, 12f, Shapes.colorRes(this, R.color.brand_indigo), Shapes.colorRes(this, R.color.brand_indigo), 2f)
            chip.setTextColor(0xFFFFFFFF.toInt())
        } else {
            chip.background = Shapes.roundedRect(this, 12f, Shapes.colorRes(this, R.color.color_chip_bg), Shapes.colorRes(this, R.color.color_chip_border), 1f)
            chip.setTextColor(Shapes.colorRes(this, R.color.color_text_secondary))
        }
    }

    private fun startPvpGame() {
        val xName = playerXInput.text?.toString().orEmpty()
        val oName = playerOInput.text?.toString().orEmpty()
        val count = matchCount

        if (xName.isEmpty() || oName.isEmpty() || count == null) {
            Snackbar.make(findViewById(android.R.id.content), R.string.please_enter_names, Snackbar.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, TwoPlayerGameActivity::class.java).apply {
            putExtra(EXTRA_PLAYER_X, xName)
            putExtra(EXTRA_PLAYER_O, oName)
            putExtra(EXTRA_MATCH_COUNT, count)
        }
        startActivity(intent)
    }

    private fun startAiFlow() {
        val xName = playerXInput.text?.toString().orEmpty()
        if (xName.isEmpty()) {
            Snackbar.make(findViewById(android.R.id.content), R.string.please_enter_your_name, Snackbar.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(this, AIDifficultyActivity::class.java).apply {
            putExtra(EXTRA_PLAYER_NAME, xName)
            putExtra(EXTRA_MATCH_COUNT, matchCount ?: 1)
        }
        startActivity(intent)
    }

    companion object {
        const val EXTRA_PLAYER_X = "playerXName"
        const val EXTRA_PLAYER_O = "playerOName"
        const val EXTRA_PLAYER_NAME = "playerName"
        const val EXTRA_MATCH_COUNT = "matchCount"
    }
}
