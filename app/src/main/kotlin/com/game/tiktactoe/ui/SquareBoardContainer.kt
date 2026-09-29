package com.game.tiktactoe.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.min

/**
 * Sizes itself to a square that is 86% of the available width, capped at 440dp —
 * mirrors the board's responsive sizing from the original app without relying on
 * ConstraintLayout's percentage/ratio attributes.
 */
class SquareBoardContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val parentWidth = android.view.View.MeasureSpec.getSize(widthMeasureSpec)
        val parentHeight = android.view.View.MeasureSpec.getSize(heightMeasureSpec).let { if (it <= 0) Int.MAX_VALUE else it }
        val maxSizePx = (440 * resources.displayMetrics.density).toInt()
        val size = min(min(parentWidth, parentHeight), maxSizePx)
        val exactSpec = android.view.View.MeasureSpec.makeMeasureSpec(size, android.view.View.MeasureSpec.EXACTLY)
        super.onMeasure(exactSpec, exactSpec)
    }
}
