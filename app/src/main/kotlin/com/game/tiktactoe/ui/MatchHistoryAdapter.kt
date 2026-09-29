package com.game.tiktactoe.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.game.tiktactoe.R
import com.game.tiktactoe.model.MatchRecord
import java.util.concurrent.TimeUnit

class MatchHistoryAdapter(
    private val items: List<MatchRecord>
) : RecyclerView.Adapter<MatchHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val avatarWrap: android.view.View = itemView
        val avatarIcon: ImageView = itemView.findViewById(R.id.avatarIcon)
        val matchupText: TextView = itemView.findViewById(R.id.matchupText)
        val winnerText: TextView = itemView.findViewById(R.id.winnerText)
        val scoreText: TextView = itemView.findViewById(R.id.scoreText)
        val difficultyText: TextView = itemView.findViewById(R.id.difficultyText)
        val metaText: TextView = itemView.findViewById(R.id.metaText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_match_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = items[position]
        val context = holder.itemView.context
        val isAi = record.gameType == "ai"

        holder.itemView.background = Shapes.roundedRect(context, 12f, Shapes.colorRes(context, R.color.color_surface))

        val avatarColor = if (isAi) Shapes.colorRes(context, R.color.brand_blue) else Shapes.colorRes(context, R.color.brand_purple)
        val avatarFrame = holder.avatarIcon.parent as android.view.View
        avatarFrame.background = Shapes.circle(context, avatarColor)
        holder.avatarIcon.setImageResource(if (isAi) R.drawable.ic_smart_toy else R.drawable.ic_person)

        holder.matchupText.text = if (isAi) {
            context.getString(R.string.vs_ai, record.player1Name)
        } else {
            context.getString(R.string.vs_human, record.player1Name, record.player2Name)
        }

        holder.winnerText.text = context.getString(R.string.winner_label, record.winner)
        holder.winnerText.setTextColor(
            if (record.winner == "Draw") Shapes.colorRes(context, R.color.brand_grey_500)
            else Shapes.colorRes(context, R.color.brand_green_dark)
        )

        holder.scoreText.text = context.getString(R.string.score_label, record.player1Score, record.player2Score)

        if (record.difficulty.isNotEmpty()) {
            holder.difficultyText.visibility = android.view.View.VISIBLE
            holder.difficultyText.text = context.getString(R.string.difficulty_label, record.difficulty.uppercase())
        } else {
            holder.difficultyText.visibility = android.view.View.GONE
        }

        val matchTypeText = if (record.matchCount == 1) context.getString(R.string.single_match) else context.getString(R.string.best_of_3)
        holder.metaText.text = "$matchTypeText • ${relativeTime(context, record.dateTimeMillis)}"
    }

    private fun relativeTime(context: android.content.Context, millis: Long): String {
        val diff = System.currentTimeMillis() - millis
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            hours < 1 -> if (minutes < 1) context.getString(R.string.just_now) else context.getString(R.string.minutes_ago, minutes.toInt())
            days < 1 -> context.getString(R.string.hours_ago, hours.toInt())
            days.toInt() == 1 -> context.getString(R.string.yesterday)
            days < 7 -> context.getString(R.string.days_ago, days.toInt())
            else -> {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = millis }
                "${cal.get(java.util.Calendar.DAY_OF_MONTH)}/${cal.get(java.util.Calendar.MONTH) + 1}/${cal.get(java.util.Calendar.YEAR)}"
            }
        }
    }
}
