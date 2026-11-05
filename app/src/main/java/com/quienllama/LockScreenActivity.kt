package com.quienllama

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager

/**
 * Cartel que se ve sobre la pantalla de bloqueo. Es una ventana chica arriba de todo:
 * lo que queda fuera del cartel sigue tocable (se puede atender la llamada desde la pantalla
 * de llamada, que queda detrás). Se cierra al tocarlo, al pasar el tiempo o al desbloquear.
 */
class LockScreenActivity : Activity() {

    private val handler = Handler(Looper.getMainLooper())
    private var msg: Message? = null
    private var endAt = 0L

    private val unlocked = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Al desbloquear, pasa al cartel flotante normal por el tiempo que quedaba.
            val m = msg
            val left = ((endAt - SystemClock.elapsedRealtime()) / 1000).toInt()
            if (m != null && left >= 3) Overlay.show(applicationContext, m, left)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        setFinishOnTouchOutside(false)

        window.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            addFlags(
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            )
            setGravity(Gravity.TOP)
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val lp = attributes
            lp.y = (Prefs.overlayTop(this@LockScreenActivity) *
                resources.displayMetrics.density).toInt()
            attributes = lp
        }

        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(unlocked, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(unlocked, filter)
        }

        render(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        render(intent)
    }

    private fun render(i: Intent) {
        val m = Message(
            i.getStringExtra(LockScreen.EXTRA_TITLE) ?: "",
            i.getStringExtra(LockScreen.EXTRA_BODY) ?: "",
            i.getBooleanExtra(LockScreen.EXTRA_AMBIGUOUS, false)
        )
        val secs = i.getIntExtra(LockScreen.EXTRA_SECONDS, 30)
        msg = m
        endAt = SystemClock.elapsedRealtime() + secs * 1000L
        setContentView(
            Overlay.buildCard(this, m) { finish() },
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        )
        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({ finish() }, secs * 1000L)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        try {
            unregisterReceiver(unlocked)
        } catch (_: Exception) {
        }
        LockScreen.cancel(applicationContext)
        super.onDestroy()
    }
}
