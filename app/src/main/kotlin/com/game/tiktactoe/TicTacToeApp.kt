package com.game.tiktactoe

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.game.tiktactoe.services.GameStateManager
import com.game.tiktactoe.services.ThemePrefs

class TicTacToeApp : MultiDexApplication() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        androidx.appcompat.app.AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        ThemePrefs.applyNightMode(ThemePrefs.isDarkMode(this))
        GameStateManager.resetAll()
    }
}
