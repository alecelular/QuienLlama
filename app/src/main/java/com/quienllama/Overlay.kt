package com.quienllama

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView

/** Cartel flotante sobre la pantalla de llamada. Se cierra al tocarlo o tras unos segundos. */
object Overlay {

    private val handler = Handler(Looper.getMainLooper())
    private var view: View? = null
    private var wm: WindowManager? = null
    private val autoHide = Runnable { dismiss() }

    /** Arma el cartel (mismo aspecto para el flotante y para el de pantalla bloqueada). */
    fun buildCard(ctx: Context, msg: Message, onTap: () -> Unit): View {
        val density = ctx.resources.displayMetrics.density
        fun dp(v: Int) = (v * density).toInt()

        val box = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(14), dp(18), dp(14))
            background = GradientDrawable().apply {
                cornerRadius = dp(16).toFloat()
                setColor(if (msg.ambiguous) 0xF2B26A00.toInt() else 0xF2263238.toInt())
            }
            setOnClickListener { onTap() }
        }
        box.addView(TextView(ctx).apply {
            text = msg.title
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            setTypeface(typeface, Typeface.BOLD)
        })
        box.addView(TextView(ctx).apply {
            text = msg.body
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            setPadding(0, dp(4), 0, 0)
        })
        // Margen lateral: se logra con un contenedor transparente.
        return LinearLayout(ctx).apply {
            setPadding(dp(12), 0, dp(12), 0)
            addView(box, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ))
        }
    }

    /**
     * [overLockScreen]: pide además que la ventana se vea con el teléfono bloqueado.
     * Android suele ignorarlo para ventanas flotantes; es solo un intento de último recurso.
     */
    @Suppress("DEPRECATION")
    fun show(ctx: Context, msg: Message, seconds: Int, overLockScreen: Boolean = false) {
        val app = ctx.applicationContext
        if (!Settings.canDrawOverlays(app)) return
        handler.post {
            dismiss()
            val density = app.resources.displayMetrics.density
            val holder = buildCard(app, msg) { dismiss() }

            var flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            if (overLockScreen) flags = flags or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                flags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP
                x = 0
                y = (Prefs.overlayTop(app) * density).toInt()   // se configura en la pantalla de la app
            }

            try {
                val manager = app.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                manager.addView(holder, params)
                wm = manager
                view = holder
                handler.postDelayed(autoHide, seconds * 1000L)
            } catch (_: Exception) {
                view = null
            }
        }
    }

    fun dismiss() {
        handler.removeCallbacks(autoHide)
        val v = view ?: return
        try {
            wm?.removeView(v)
        } catch (_: Exception) {
        }
        view = null
    }
}
