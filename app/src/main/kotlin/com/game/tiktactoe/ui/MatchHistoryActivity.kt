package com.game.tiktactoe.ui

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.game.tiktactoe.R
import com.game.tiktactoe.services.MatchHistoryService

class MatchHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var clearButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_history)

        recyclerView = findViewById(R.id.historyList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        emptyState = findViewById(R.id.emptyState)
        clearButton = findViewById(R.id.clearButton)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setColorFilter(Shapes.colorRes(this, R.color.color_text_primary))
        clearButton.setColorFilter(Shapes.colorRes(this, R.color.color_text_primary))
        findViewById<android.widget.ImageView>(R.id.emptyStateIcon)
            .setColorFilter(Shapes.colorRes(this, R.color.color_text_tertiary))

        backButton.setOnClickListener {
            @Suppress("DEPRECATION")
            onBackPressed()
        }

        clearButton.setOnClickListener {
            GameDialogs.buildConfirmDialog(
                context = this,
                title = getString(R.string.clear_history_title),
                message = getString(R.string.clear_history_message),
                cancelLabel = getString(R.string.cancel),
                confirmLabel = getString(R.string.clear)
            ) {
                MatchHistoryService.clearHistory(this)
                refresh()
            }.show()
        }

        refresh()
    }

    private fun refresh() {
        val history = MatchHistoryService.getHistory(this)
        recyclerView.adapter = MatchHistoryAdapter(history)

        val isEmpty = history.isEmpty()
        emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        clearButton.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
