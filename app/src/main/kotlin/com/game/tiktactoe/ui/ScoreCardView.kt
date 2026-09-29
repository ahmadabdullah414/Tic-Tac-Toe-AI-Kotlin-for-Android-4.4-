package com.game.tiktactoe.ui

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import com.game.tiktactoe.R

class ScoreCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {

    private val nameView: TextView
    private val scoreView: TextView

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER
        val pad = (12 * resources.displayMetrics.density).toInt()
        setPadding(pad, pad, pad, pad + (5 * resources.displayMetrics.density).toInt())

        nameView = TextView(context).apply {
            gravity = Gravity.CENTER
            maxLines = 1
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
        }
        scoreView = TextView(context).apply {
            gravity = Gravity.CENTER
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Shapes.colorRes(context, R.color.color_text_primary))
        }

        addView(nameView)
        addView(scoreView)
    }

    fun bind(name: String, score: Int, accentColor: Int, highlighted: Boolean) {
        val displayName = if (name.length > 10) name.substring(0, 10) else name
        nameView.text = displayName
        nameView.setTextColor(if (highlighted) 0xFFFFFFFF.toInt() else accentColor)
        scoreView.text = score.toString()
        scoreView.setTextColor(if (highlighted) 0xFFFFFFFF.toInt() else Shapes.colorRes(context, R.color.color_text_primary))

        background = if (highlighted) {
            Shapes.raised(context, 16f, Shapes.colorRes(context, R.color.brand_green), 5f)
        } else {
            Shapes.raised(context, 16f, Shapes.colorRes(context, R.color.color_surface), 5f)
        }
    }
}
