package com.game.tiktactoe.ui

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.setPadding
import com.game.tiktactoe.R
import kotlin.random.Random

/**
 * Full-screen overlay that animates a "shuffle" between two players/colors to decide who
 * goes first, mirroring TurnShuffleWidget from the original app.
 */
class TurnShuffleView(
    context: Context,
    private val color1: Int,
    private val color2: Int,
    private val player1Name: String,
    private val player2Name: String,
    private val predeterminedResult: Boolean?,
    private val onComplete: (Boolean) -> Unit
) : FrameLayout(context) {

    private val handler = Handler(Looper.getMainLooper())
    private val density = context.resources.displayMetrics.density

    private var selectedColor1 = false
    private var isShuffling = true
    private var isSlowing = false
    private var isStopped = false
    private var shuffleCount = 0

    private lateinit var box1: FrameLayout
    private lateinit var box2: FrameLayout
    private lateinit var icon1: ImageView
    private lateinit var icon2: ImageView
    private lateinit var resultLabel: TextView

    init {
        setBackgroundColor(Shapes.colorRes(context, R.color.scrim_70))
        isClickable = true
        buildUi()
        startShuffle()
    }

    private fun dp(v: Int): Int = (v * density).toInt()

    private fun buildUi() {
        val column = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val columnParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        addView(column, columnParams)

        val title = TextView(context).apply {
            text = context.getString(R.string.determining_first_turn)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        column.addView(title)

        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        val rowParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        rowParams.topMargin = dp(40)
        column.addView(row, rowParams)

        box1 = buildColorBox(color1)
        box2 = buildColorBox(color2)
        icon1 = ((box1.getChildAt(0) as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as ImageView
        icon2 = ((box2.getChildAt(0) as LinearLayout).getChildAt(0) as FrameLayout).getChildAt(0) as ImageView

        val box1Params = LinearLayout.LayoutParams(dp(130), dp(130))
        row.addView(box1, box1Params)
        row.addView(android.view.View(context), LinearLayout.LayoutParams(dp(20), 1))
        val box2Params = LinearLayout.LayoutParams(dp(130), dp(130))
        row.addView(box2, box2Params)

        resultLabel = TextView(context).apply {
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 19f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            visibility = INVISIBLE
            setPadding(dp(24), dp(16), dp(24), dp(16))
        }
        val resultParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        resultParams.topMargin = dp(28)
        column.addView(resultLabel, resultParams)

        updateBoxState()
    }

    private fun buildColorBox(color: Int): FrameLayout {
        val box = FrameLayout(context)
        val inner = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
        }
        val icon = ImageView(context).apply {
            setImageResource(R.drawable.ic_circle)
            setPadding(dp(12))
        }
        val iconWrap = FrameLayout(context).apply {
            background = Shapes.circle(context, Shapes.colorWithAlpha(0xFFFFFFFF.toInt(), 0.2f))
        }
        iconWrap.addView(icon, ViewGroup.LayoutParams(dp(52), dp(52)))
        inner.addView(iconWrap, LinearLayout.LayoutParams(dp(52), dp(52)))

        val name = TextView(context).apply {
            text = if (color == color1) player1Name.take(10) else player2Name.take(10)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            maxLines = 1
        }
        val nameParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        nameParams.topMargin = dp(8)
        inner.addView(name, nameParams)

        box.addView(inner, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER))
        return box
    }

    private fun startShuffle() {
        handler.postDelayed({
            isSlowing = true
            handler.postDelayed({ stopShuffle() }, 800)
        }, 1200)
        shuffleStep()
    }

    private fun shuffleStep() {
        if (!isShuffling) return
        selectedColor1 = !selectedColor1
        shuffleCount++
        updateBoxState()

        val delay = if (isSlowing) (150 + shuffleCount * 25).toLong() else 120L
        handler.postDelayed({ if (isShuffling) shuffleStep() }, delay)
    }

    private fun stopShuffle() {
        isShuffling = false
        isSlowing = false
        isStopped = true
        selectedColor1 = predeterminedResult ?: Random.nextBoolean()
        updateBoxState()

        resultLabel.text = context.getString(
            R.string.goes_first,
            (if (selectedColor1) player1Name else player2Name).take(10)
        )
        val winnerColor = if (selectedColor1) color1 else color2
        resultLabel.background = Shapes.roundedRect(context, 16f, Shapes.colorWithAlpha(winnerColor, 0.2f), winnerColor, 2f)
        resultLabel.visibility = VISIBLE
        resultLabel.alpha = 0f
        resultLabel.scaleX = 0.7f
        resultLabel.scaleY = 0.7f
        resultLabel.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(250).start()

        handler.postDelayed({ onComplete(selectedColor1) }, 500)
    }

    private fun updateBoxState() {
        val box1Active = selectedColor1 && !isStopped
        val box2Active = !selectedColor1 && !isStopped
        val box1Winner = selectedColor1 && isStopped
        val box2Winner = !selectedColor1 && isStopped

        styleBox(box1, color1, box1Active || box1Winner, box1Winner)
        styleBox(box2, color2, box2Active || box2Winner, box2Winner)
        icon1.setImageResource(if (box1Winner) R.drawable.ic_check_circle else R.drawable.ic_circle)
        icon2.setImageResource(if (box2Winner) R.drawable.ic_check_circle else R.drawable.ic_circle)

        val activeScale = 1.15f
        val inactiveScale = 0.85f
        box1.scaleX = if (box1Active || box1Winner) activeScale else inactiveScale
        box1.scaleY = box1.scaleX
        box2.scaleX = if (box2Active || box2Winner) activeScale else inactiveScale
        box2.scaleY = box2.scaleX
    }

    private fun styleBox(box: FrameLayout, color: Int, active: Boolean, winner: Boolean) {
        val fill = if (active) color else Shapes.colorWithAlpha(color, 0.3f)
        val strokeColor = if (winner) 0xFFFFFFFF.toInt() else Shapes.colorWithAlpha(color, 0.5f)
        val strokeWidth = if (winner) 3f else 2f
        box.background = Shapes.roundedRect(context, 20f, fill, strokeColor, strokeWidth)
    }

    fun cancelPendingCallbacks() {
        handler.removeCallbacksAndMessages(null)
    }
}
