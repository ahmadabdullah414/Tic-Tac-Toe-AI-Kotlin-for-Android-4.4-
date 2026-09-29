package com.game.tiktactoe.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.game.tiktactoe.R

class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private val navigateRunnable = Runnable {
        if (!isFinishing) {
            startActivity(Intent(this, MainMenuActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val icon = findViewById<android.view.View>(R.id.iconView)
        val title = findViewById<android.view.View>(R.id.titleView)
        val subtitle = findViewById<android.view.View>(R.id.subtitleView)
        val progress = findViewById<android.view.View>(R.id.progressBar)

        for (v in listOf(icon, title, subtitle, progress)) {
            v.alpha = 0f
        }
        icon.scaleX = 0.6f
        icon.scaleY = 0.6f

        icon.animate().alpha(1f).scaleX(1f).scaleY(1f)
            .setDuration(900)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        for ((index, v) in listOf(title, subtitle, progress).withIndex()) {
            v.animate().alpha(1f)
                .setStartDelay(200L + index * 150L)
                .setDuration(700)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }

        handler.postDelayed(navigateRunnable, 2000)
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateRunnable)
        super.onDestroy()
    }
}
