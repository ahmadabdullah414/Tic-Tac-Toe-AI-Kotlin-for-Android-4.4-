package com.game.tiktactoe.ui

import android.content.res.ColorStateList
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ImageViewCompat
import com.game.tiktactoe.R
import com.game.tiktactoe.services.ThemePrefs

/** Wires up the recurring dark/light toggle button that appears on most screens. */
object ThemeToggle {
    fun bind(activity: AppCompatActivity, button: ImageButton) {
        val isDark = ThemePrefs.isDarkMode(activity)
        button.setImageResource(if (isDark) R.drawable.ic_sun else R.drawable.ic_moon)
        ImageViewCompat.setImageTintList(button, ColorStateList.valueOf(Shapes.colorRes(activity, R.color.color_text_primary)))
        button.setOnClickListener {
            ThemePrefs.setDarkMode(activity, !isDark)
            activity.recreate()
        }
    }
}
