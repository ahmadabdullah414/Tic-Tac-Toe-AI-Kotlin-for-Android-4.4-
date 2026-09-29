package com.game.tiktactoe.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.game.tiktactoe.R

class MainMenuActivity : ThemedActivity() {

    private var matchCount: Int? = 1
    private lateinit var chipSingle: TextView
    private lateinit var chipBest3: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
        Shapes.applyScreenBackground(this)

        ThemeToggle.bind(this, findViewById<ImageButton>(R.id.themeToggleButton))

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

        val singlePlayerButton = findViewById<MaterialButton>(R.id.singlePlayerButton)
        val twoPlayerButton = findViewById<MaterialButton>(R.id.twoPlayerButton)
        Shapes.styleButtonIcon(singlePlayerButton, R.drawable.ic_player_one, cornerRadiusDp = 16)
        Shapes.style3d(singlePlayerButton, 22f, 0xFF6366F1.toInt(), 7f)
        Shapes.styleButtonIcon(twoPlayerButton, R.drawable.ic_player_two, cornerRadiusDp = 16)
        Shapes.style3d(twoPlayerButton, 22f, 0xFF10B981.toInt(), 7f)

        singlePlayerButton.setOnClickListener { startAiFlow() }
        twoPlayerButton.setOnClickListener { startPvpGame() }
    }

    private fun styleChips() {
        styleChip(chipSingle, matchCount == 1)
        styleChip(chipBest3, matchCount == 3)
    }

    private fun styleChip(chip: TextView, selected: Boolean) {
        if (selected) {
            Shapes.style3d(chip, 18f, Shapes.colorRes(this, R.color.brand_indigo), 5f)
            chip.setTextColor(0xFFFFFFFF.toInt())
        } else {
            Shapes.style3d(chip, 18f, Shapes.colorRes(this, R.color.color_surface), 3f, textShadow = false)
            chip.setTextColor(Shapes.colorRes(this, R.color.color_text_primary)); chip.setShadowLayer(0f, 0f, 0f, 0)
        }
    }

    private fun startPvpGame() {
        val count = matchCount
        if (count == null) {
            Snackbar.make(findViewById(android.R.id.content), R.string.please_select_match_type, Snackbar.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, TwoPlayerGameActivity::class.java).apply {
            putExtra(EXTRA_PLAYER_X, getString(R.string.red_team))
            putExtra(EXTRA_PLAYER_O, getString(R.string.blue_team))
            putExtra(EXTRA_MATCH_COUNT, count)
        }
        startActivity(intent)
    }

    private fun startAiFlow() {
        val intent = Intent(this, AIDifficultyActivity::class.java).apply {
            putExtra(EXTRA_PLAYER_NAME, getString(R.string.red_team))
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
