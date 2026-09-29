package com.game.tiktactoe.ui

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.Gravity
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.game.tiktactoe.R
import kotlin.random.Random

/**
 * Popup shown over the (visible) game tray that animates a "shuffle" between the two players
 * to decide who goes first. Two 3D blocks light up alternately, then settle on the winner.
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
    private var shuffleCount = 0

    private lateinit var card: LinearLayout
    private lateinit var block1: TextView
    private lateinit var block2: TextView
    private lateinit var resultLabel: TextView

    // Built once so the shuffle never allocates drawables (no lag).
    private val up1: Drawable = Shapes.raised(context, 22f, color1, 8f)
    private val down1: Drawable = Shapes.raised(context, 22f, Shapes.shade(color1, 0.55f), 3f)
    private val up2: Drawable = Shapes.raised(context, 22f, color2, 8f)
    private val down2: Drawable = Shapes.raised(context, 22f, Shapes.shade(color2, 0.55f), 3f)

    init {
        setBackgroundColor(0x99000000.toInt())
        clipChildren = false
        clipToPadding = false
        isClickable = true
        buildUi()
        applyState(animate = false, done = false)
        card.alpha = 0f
        card.scaleX = 0.85f
        card.scaleY = 0.85f
        card.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(260)
            .setInterpolator(OvershootInterpolator(1.1f)).start()
        startShuffle()
    }

    private fun dp(v: Int): Int = (v * density).toInt()

    private fun buildUi() {
        card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(22), dp(20), dp(22) + dp(6))
            clipChildren = false
            clipToPadding = false
        }
        val cardParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER)
        cardParams.setMargins(dp(28), 0, dp(28), 0)
        addView(card, cardParams)


        val row = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            clipChildren = false
        }
        card.addView(row, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(128)).apply {
            topMargin = 0
        })

        block1 = buildBlock("X", player1Name)
        block2 = buildBlock("O", player2Name)
        val wrap1 = FrameLayout(context).apply { clipChildren = false; addView(block1, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)) }
        val wrap2 = FrameLayout(context).apply { clipChildren = false; addView(block2, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)) }
        row.addView(wrap1, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply { rightMargin = dp(8) })
        row.addView(wrap2, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f).apply { leftMargin = dp(8) })

        resultLabel = TextView(context).apply {
            setTextColor(Shapes.colorRes(context, R.color.color_text_primary))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            alpha = 0f
            text = " "
            textSize = 17f
        }
        card.addView(resultLabel, LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            topMargin = dp(18)
        })
    }

    private fun buildBlock(symbol: String, name: String): TextView =
        TextView(context).apply {
            text = name.take(10)
            setTextColor(0xFFFFFFFF.toInt())
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            maxLines = 1
            setLineSpacing(0f, 0.95f)
            setShadowLayer(3f, 0f, 2f, 0x66000000)
            setPadding(dp(2), dp(6), dp(2), dp(8))
            compoundDrawablePadding = dp(4)
            setCompoundDrawablesWithIntrinsicBounds(null, Shapes.drawable(context, if (symbol == "X") R.drawable.ic_mark_moon else R.drawable.ic_mark_star), null, null)
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
        applyState(animate = true, done = false)
        val delay = if (isSlowing) (170 + shuffleCount * 25).toLong() else 130L
        handler.postDelayed({ if (isShuffling) shuffleStep() }, delay)
    }

    private fun stopShuffle() {
        isShuffling = false
        isSlowing = false
        selectedColor1 = predeterminedResult ?: Random.nextBoolean()
        applyState(animate = true, done = true)

        resultLabel.text = context.getString(
            R.string.goes_first,
            (if (selectedColor1) player1Name else player2Name).take(10)
        )
        resultLabel.setTextColor(0xFFFFFFFF.toInt())
        resultLabel.setShadowLayer(4f, 0f, 2f, 0x99000000.toInt())
        resultLabel.translationY = dp(8).toFloat()
        resultLabel.animate().alpha(1f).translationY(0f).setDuration(260)
            .setInterpolator(DecelerateInterpolator()).start()

        handler.postDelayed({ onComplete(selectedColor1) }, 900)
    }
    /** Lit block is raised, full size and bright; the other one is pressed down, smaller and dim. */
    private fun applyState(animate: Boolean, done: Boolean) {
        setBlock(block1, selectedColor1, up1, down1, animate)
        setBlock(block2, !selectedColor1, up2, down2, animate)
    }

    // Size changes go through layout margins (not scale), so blocks always draw at true size
    // with their real corner radius and can never be clipped by a parent.
    private fun setBlock(block: TextView, lit: Boolean, up: Drawable, down: Drawable, animate: Boolean) {
        block.background = if (lit) up else down
        block.alpha = if (lit) 1f else 0.75f
        val target = if (lit) 0 else dp(10)
        val params = block.layoutParams as LayoutParams
        if (!animate) { setMargin(block, target); return }
        val from = params.leftMargin
        android.animation.ValueAnimator.ofInt(from, target).apply {
            duration = 110
            addUpdateListener { setMargin(block, it.animatedValue as Int) }
            start()
        }
    }

    private fun setMargin(block: TextView, m: Int) {
        (block.layoutParams as LayoutParams).setMargins(m, m, m, m)
        block.requestLayout()
    }

    fun cancelPendingCallbacks() {
        handler.removeCallbacksAndMessages(null)
    }
}
