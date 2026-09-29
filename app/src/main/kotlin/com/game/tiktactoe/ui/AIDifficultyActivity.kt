package com.game.tiktactoe.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.game.tiktactoe.R
import com.game.tiktactoe.services.AIDifficulty

class AIDifficultyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_difficulty)

        val playerName = intent.getStringExtra(MainMenuActivity.EXTRA_PLAYER_NAME) ?: "Player"
        val matchCount = intent.getIntExtra(MainMenuActivity.EXTRA_MATCH_COUNT, 1)

        ThemeToggle.bind(this, findViewById<ImageButton>(R.id.themeToggleButton))
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setColorFilter(Shapes.colorRes(this, R.color.color_text_primary))
        backButton.setOnClickListener { onBackPressedCompat() }

        findViewById<android.view.View>(R.id.headerCard).background = Shapes.roundedRect(
            this, 20f,
            Shapes.colorWithAlpha(Shapes.colorRes(this, R.color.color_surface), 0.7f),
            Shapes.colorWithAlpha(Shapes.colorRes(this, R.color.brand_indigo), 0.2f), 1.5f
        )
        findViewById<ImageView>(R.id.headerIcon).setColorFilter(Shapes.colorRes(this, R.color.brand_indigo))

        findViewById<TextView>(R.id.matchTypeLabel).text = getString(
            R.string.match_type_label,
            if (matchCount == 1) getString(R.string.single_match) else getString(R.string.best_of_3)
        )

        configureRow(
            rowId = R.id.difficultyEasy,
            color = Shapes.colorRes(this, R.color.brand_green),
            icon = R.drawable.ic_sentiment_happy,
            title = getString(R.string.difficulty_easy),
            description = getString(R.string.difficulty_easy_desc),
            difficulty = AIDifficulty.EASY,
            playerName = playerName,
            matchCount = matchCount
        )
        configureRow(
            rowId = R.id.difficultyMedium,
            color = Shapes.colorRes(this, R.color.brand_orange),
            icon = R.drawable.ic_sentiment_neutral,
            title = getString(R.string.difficulty_medium),
            description = getString(R.string.difficulty_medium_desc),
            difficulty = AIDifficulty.MEDIUM,
            playerName = playerName,
            matchCount = matchCount
        )
        configureRow(
            rowId = R.id.difficultyHard,
            color = Shapes.colorRes(this, R.color.brand_red),
            icon = R.drawable.ic_sentiment_sad,
            title = getString(R.string.difficulty_hard),
            description = getString(R.string.difficulty_hard_desc),
            difficulty = AIDifficulty.HARD,
            playerName = playerName,
            matchCount = matchCount
        )
    }

    private fun configureRow(
        rowId: Int,
        color: Int,
        icon: Int,
        title: String,
        description: String,
        difficulty: AIDifficulty,
        playerName: String,
        matchCount: Int
    ) {
        val row = findViewById<android.view.View>(rowId)
        row.background = Shapes.roundedRect(this, 20f, color)

        row.findViewById<ImageView>(R.id.rowIcon).setImageResource(icon)
        row.findViewById<TextView>(R.id.rowTitle).text = title
        row.findViewById<TextView>(R.id.rowDescription).text = description

        row.setOnClickListener {
            val intent = Intent(this, AIGameActivity::class.java).apply {
                putExtra(MainMenuActivity.EXTRA_PLAYER_NAME, playerName)
                putExtra(EXTRA_DIFFICULTY, difficulty.name)
                putExtra(MainMenuActivity.EXTRA_MATCH_COUNT, matchCount)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun onBackPressedCompat() {
        @Suppress("DEPRECATION")
        onBackPressed()
    }

    companion object {
        const val EXTRA_DIFFICULTY = "difficulty"
    }
}
