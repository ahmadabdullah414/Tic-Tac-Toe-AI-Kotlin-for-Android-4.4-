package com.game.tiktactoe.ui

import android.content.Context
import android.content.res.ColorStateList
import android.app.Activity
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.StateListDrawable
import android.view.View
import android.widget.TextView
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

    /** Loads vector drawables on API 19 too (via AppCompat) instead of relying on aapt PNG conversion. */
    fun drawable(context: Context, resId: Int): Drawable? =
        androidx.appcompat.content.res.AppCompatResources.getDrawable(context, resId)

    /** Applies icon + corner-radius styling that would otherwise need MaterialButton's custom XML attrs. */
    fun styleButtonIcon(button: MaterialButton, iconRes: Int, cornerRadiusDp: Int = 12) {
        val density = button.context.resources.displayMetrics.density
        button.icon = drawable(button.context, iconRes)
        button.iconTint = ColorStateList.valueOf(0xFFFFFFFF.toInt())
        button.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
        button.cornerRadius = (cornerRadiusDp * density).toInt()
    }

    /** Team colours: bright red for X, bright blue for O. Chosen to stay visible on both light and dark backgrounds. */
    private const val RED = 0xFFFF2B3A.toInt()
    private const val BLUE = 0xFF2D7DFF.toInt()

    @Suppress("UNUSED_PARAMETER") fun xAccent(context: Context): Int = RED
    @Suppress("UNUSED_PARAMETER") fun xMark(context: Context): Int = RED
    @Suppress("UNUSED_PARAMETER") fun xBlock(context: Context): Int = RED
    @Suppress("UNUSED_PARAMETER") fun oAccent(context: Context): Int = BLUE
    @Suppress("UNUSED_PARAMETER") fun oMark(context: Context): Int = BLUE
    @Suppress("UNUSED_PARAMETER") fun oBlock(context: Context): Int = BLUE

    fun isNight(context: Context): Boolean =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    fun shade(color: Int, factor: Float): Int {
        val r = ((color shr 16 and 0xFF) * factor).toInt().coerceIn(0, 255)
        val g = ((color shr 8 and 0xFF) * factor).toInt().coerceIn(0, 255)
        val b = ((color and 0xFF) * factor).toInt().coerceIn(0, 255)
        return (color and 0xFF000000.toInt()) or (r shl 16) or (g shl 8) or b
    }

    fun tint(color: Int, amount: Float): Int {
        val r = (color shr 16 and 0xFF); val g = (color shr 8 and 0xFF); val b = (color and 0xFF)
        val nr = (r + (255 - r) * amount).toInt(); val ng = (g + (255 - g) * amount).toInt(); val nb = (b + (255 - b) * amount).toInt()
        return (color and 0xFF000000.toInt()) or (nr shl 16) or (ng shl 8) or nb
    }

    private fun px(context: Context, dp: Float) = (dp * context.resources.displayMetrics.density).toInt()

    /** One frame of a 3D "raised key": dark base underneath, glossy gradient face on top offset by [faceTopDp]. */
    private fun keyFrame(context: Context, radiusDp: Float, color: Int, depthDp: Float, faceTopDp: Float): Drawable {
        val r = radiusDp * context.resources.displayMetrics.density
        val base = GradientDrawable().apply { shape = GradientDrawable.RECTANGLE; cornerRadius = r; setColor(shade(color, 0.62f)) }
        val face = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(tint(color, 0.28f), color, shade(color, 0.92f))).apply {
            shape = GradientDrawable.RECTANGLE; cornerRadius = r
        }
        val gloss = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(0x55FFFFFF, 0x00FFFFFF)).apply {
            shape = GradientDrawable.RECTANGLE; cornerRadius = r
        }
        val d = px(context, depthDp); val top = px(context, faceTopDp)
        return LayerDrawable(arrayOf(base, face, gloss)).apply {
            setLayerInset(1, 0, top, 0, d - top)
            setLayerInset(2, px(context, 3f), top + px(context, 2f), px(context, 3f), d - top + px(context, 24f))
        }
    }

    /** Raised 3D drawable that sinks when pressed. Pair with [style3d] to also pad content above the ledge. */
    fun raised(context: Context, radiusDp: Float, color: Int, depthDp: Float = 5f): Drawable {
        val sel = StateListDrawable()
        sel.addState(intArrayOf(android.R.attr.state_pressed), keyFrame(context, radiusDp, color, depthDp, depthDp * 0.7f))
        sel.addState(intArrayOf(), keyFrame(context, radiusDp, color, depthDp, 0f))
        return sel
    }

    fun style3d(view: View, radiusDp: Float, color: Int, depthDp: Float = 5f, textShadow: Boolean = true) {
        if (view is MaterialButton) view.backgroundTintList = null
        view.background = raised(view.context, radiusDp, color, depthDp)
        view.setPadding(view.paddingLeft, view.paddingTop, view.paddingRight, px(view.context, depthDp))
        if (textShadow && view is TextView) view.setShadowLayer(3f, 0f, 2f, 0x66000000)
    }

    /** Colorful gradient behind every screen. */
    fun applyScreenBackground(activity: Activity) {
        (activity.findViewById<View>(android.R.id.content) as? android.view.ViewGroup)?.getChildAt(0)?.background = null
    }
}
