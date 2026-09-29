package com.game.tiktactoe.ui

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.hypot

/**
 * Transparent overlay drawn over the 3x3 board. Strikes a green line through the three winning cells:
 * it passes through the centre cell and stops a little inside the outer edge of the first and last cell.
 */
class WinLineView(context: Context) : View(context) {

    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }

    private var cells: IntArray? = null
    private var progress = 0f
    private var animator: ValueAnimator? = null

    init {
        isClickable = false
        isFocusable = false
    }

    /** Shows (animated) the strike for [winning] cells, or clears it when null. Repeated calls with the same cells are ignored. */
    fun show(winning: IntArray?) {
        if (winning contentEquals cells) return
        animator?.cancel()
        cells = winning
        if (winning == null) {
            progress = 0f
            invalidate()
            return
        }
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 380
            interpolator = DecelerateInterpolator()
            addUpdateListener { progress = it.animatedValue as Float; invalidate() }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        val list = cells ?: return
        if (list.size < 3) return
        val cell = width / 3f
        val first = list.minOrNull() ?: return
        val last = list.maxOrNull() ?: return
        val ax = (first % 3 + 0.5f) * cell
        val ay = (first / 3 + 0.5f) * cell
        val bx = (last % 3 + 0.5f) * cell
        val by = (last / 3 + 0.5f) * cell

        val len = hypot(bx - ax, by - ay)
        if (len == 0f) return
        val dx = (bx - ax) / len
        val dy = (by - ay) / len
        val diagonal = dx != 0f && dy != 0f
        // distance from a cell's centre to its outer edge along the line, minus breathing room
        val half = cell / 2f - 4 * density
        val reach = (if (diagonal) half * 1.4142f else half) - cell * 0.16f
        val sx = ax - dx * reach
        val sy = ay - dy * reach
        val ex = bx + dx * reach
        val ey = by + dy * reach
        val cx = sx + (ex - sx) * progress
        val cy = sy + (ey - sy) * progress

        val thickness = cell * 0.13f
        val lift = 3 * density
        // soft 3D shadow, dark body, bright core
        paint.strokeWidth = thickness + 4 * density
        paint.color = 0x55000000
        canvas.drawLine(sx, sy + lift, cx, cy + lift, paint)
        paint.strokeWidth = thickness + 3 * density
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawLine(sx, sy, cx, cy, paint)
        paint.strokeWidth = thickness
        paint.color = 0xFF059669.toInt()
        canvas.drawLine(sx, sy, cx, cy, paint)
        paint.strokeWidth = thickness * 0.3f
        paint.color = 0xFF6EE7B7.toInt()
        canvas.drawLine(sx, sy - thickness * 0.2f, cx, cy - thickness * 0.2f, paint)
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
