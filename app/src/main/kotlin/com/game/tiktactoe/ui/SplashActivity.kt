package com.game.tiktactoe.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : ThemedActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        if (!isFinishing) {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The window background already shows the splash image, so there is nothing to fade in.
        setContentView(com.game.tiktactoe.R.layout.activity_splash)
        handler.postDelayed(navigateRunnable, 1500)
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateRunnable)
        super.onDestroy()
    }
}
