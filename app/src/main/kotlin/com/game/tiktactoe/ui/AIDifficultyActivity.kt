package com.game.tiktactoe.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.game.tiktactoe.R
import com.game.tiktactoe.services.AIDifficulty

class AIDifficultyActivity : ThemedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ai_difficulty)
        Shapes.applyScreenBackground(this)

        val playerName = intent.getStringExtra(MainMenuActivity.EXTRA_PLAYER_NAME) ?: "Player"
        val matchCount = intent.getIntExtra(MainMenuActivity.EXTRA_MATCH_COUNT, 1)

        ThemeToggle.bind(this, findViewById<ImageButton>(R.id.themeToggleButton))
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setColorFilter(Shapes.colorRes(this, R.color.color_text_primary))
        backButton.setOnClickListener { onBackPressedCompat() }



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
        Shapes.style3d(row, 24f, color, 7f)

        row.findViewById<ImageView>(R.id.rowIcon).setImageDrawable(Shapes.drawable(this, icon))
        row.findViewById<TextView>(R.id.rowTitle).text = title

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
