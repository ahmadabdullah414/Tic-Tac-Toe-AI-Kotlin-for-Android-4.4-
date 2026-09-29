package com.game.tiktactoe.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

/** Lightweight helper for building rounded-rect backgrounds without extra XML resources. */
object Shapes {

    fun roundedRect(
        context: Context,
        cornerRadiusDp: Float,
        fillColor: Int,
        strokeColor: Int? = null,
        strokeWidthDp: Float = 0f
    ): GradientDrawable {
        val density = context.resources.displayMetrics.density
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = cornerRadiusDp * density
            setColor(fillColor)
            if (strokeColor != null && strokeWidthDp > 0f) {
                setStroke((strokeWidthDp * density).toInt(), strokeColor)
            }
        }
    }

    fun circle(context: Context, fillColor: Int): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(fillColor)
        }
    }

    fun colorWithAlpha(color: Int, alpha: Float): Int {
        val a = (alpha.coerceIn(0f, 1f) * 255).toInt()
        return (color and 0x00FFFFFF) or (a shl 24)
    }

    fun colorRes(context: Context, resId: Int): Int = ContextCompat.getColor(context, resId)

    /** Applies icon + corner-radius styling that would otherwise need MaterialButton's custom XML attrs. */
    fun styleButtonIcon(button: MaterialButton, iconRes: Int, cornerRadiusDp: Int = 12) {
        val density = button.context.resources.displayMetrics.density
        button.icon = ContextCompat.getDrawable(button.context, iconRes)
        button.iconTint = ColorStateList.valueOf(0xFFFFFFFF.toInt())
        button.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        button.cornerRadius = (cornerRadiusDp * density).toInt()
    }
}
