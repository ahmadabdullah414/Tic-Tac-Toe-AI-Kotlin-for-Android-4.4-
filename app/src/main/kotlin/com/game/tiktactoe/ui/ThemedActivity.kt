package com.game.tiktactoe.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.game.tiktactoe.services.ThemePrefs

/**
 * Applies the saved light/dark preference before the screen is built, so AppCompat never has to
 * recreate it afterwards (which flashes black and would lose live state). See [ThemeToggle].
 *
 * A theme switch opens a fresh copy of the screen. Screens with live state (the games) override
 * [saveThemeSwitchState] so the new copy can pick up exactly where the old one was, via [restoredState].
 *
 * Screens further down the back stack keep the theme they were created with; when the user navigates
 * back to one after the theme was changed, it cross-fades into a fresh copy in the current theme.
 */
open class ThemedActivity : AppCompatActivity() {
    private var appliedDark = false

    /** Whether the theme button may be used right now (games block it while a result is pending). */
    open fun canSwitchTheme(): Boolean = true

    /** Write everything needed to continue this screen unchanged in a fresh copy. */
    open fun saveThemeSwitchState(out: Bundle) {}

    /** State handed over by a theme switch (or saved by the system), or null for a normal fresh start. */
    protected fun restoredState(saved: Bundle?): Bundle? =
        saved?.getBundle(EXTRA_STATE) ?: intent.getBundleExtra(EXTRA_STATE)

    /** Intent for a fresh copy of this screen carrying its current state. */
    fun relaunchIntent(): Intent {
        val i = Intent(intent)
        val state = Bundle().also { saveThemeSwitchState(it) }
        if (state.isEmpty) i.removeExtra(EXTRA_STATE) else i.putExtra(EXTRA_STATE, state)
        return i
    }

    override fun attachBaseContext(newBase: Context) {
        // Must happen before the base context is attached: changing the mode later makes AppCompat recreate us.
        appliedDark = ThemePrefs.isDarkMode(newBase)
        AppCompatDelegate.setDefaultNightMode(
            if (appliedDark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        super.attachBaseContext(newBase)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        val state = Bundle().also { saveThemeSwitchState(it) }
        if (!state.isEmpty) outState.putBundle(EXTRA_STATE, state)
    }

    override fun onResume() {
        super.onResume()
        if (!isFinishing && ThemePrefs.isDarkMode(this) != appliedDark) {
            startActivity(relaunchIntent())
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    companion object {
        const val EXTRA_STATE = "themeSwitchState"
    }
}
