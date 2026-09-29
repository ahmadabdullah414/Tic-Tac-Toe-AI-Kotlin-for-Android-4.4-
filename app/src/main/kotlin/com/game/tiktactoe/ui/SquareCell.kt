package com.game.tiktactoe.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import com.game.tiktactoe.R

/**
 * A square board cell. Marks are drawn as vector shapes (red moon for "X", blue star for "O"),
 * tinted with the cell's text colour, so they render identically on every device (no emoji fonts).
 */
class SquareCell @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : AppCompatTextView(context, attrs) {

    private var markDrawable: Drawable? = null

    /** Game mark to show: "X" -> moon, "O" -> star, anything else -> empty. */
    var mark: String = ""
        set(value) {
            if (field == value) return
            field = value
            markDrawable = when (value) {
                "X" -> Shapes.drawable(context, R.drawable.ic_mark_moon)?.mutate()
                "O" -> Shapes.drawable(context, R.drawable.ic_mark_star)?.mutate()
                else -> null
            }
            invalidate()
        }

    init {
        gravity = Gravity.CENTER
        isClickable = true
        isFocusable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val d = markDrawable ?: return
        val size = (width * 0.56f).toInt()
        val left = (width - size) / 2
        val top = (height - paddingBottom - size) / 2
        d.setBounds(left, top, left + size, top + size)
        d.setColorFilter(currentTextColor, PorterDuff.Mode.SRC_IN)
        d.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredWidth)
    }
}
