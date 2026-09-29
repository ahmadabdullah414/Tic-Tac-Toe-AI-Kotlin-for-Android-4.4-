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
        button.setImageDrawable(Shapes.drawable(activity, if (isDark) R.drawable.ic_sun else R.drawable.ic_moon))
        ImageViewCompat.setImageTintList(button, ColorStateList.valueOf(Shapes.colorRes(activity, R.color.color_text_primary)))
        button.setOnClickListener {
            val themed = activity as? ThemedActivity
            if (themed != null && !themed.canSwitchTheme()) return@setOnClickListener
            ThemePrefs.setDarkMode(activity, !isDark)
            // Cross-fade into a fresh copy of this screen in the new theme instead of recreate()
            // (recreate() shows a black frame on older Android). The copy inherits the screen's state,
            // so a game in progress carries on untouched.
            activity.startActivity(themed?.relaunchIntent() ?: android.content.Intent(activity.intent))
            activity.finish()
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
