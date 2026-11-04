package com.quienllama

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
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

    fun show(ctx: Context, msg: Message, seconds: Int) {
        val app = ctx.applicationContext
        if (!Settings.canDrawOverlays(app)) return
        handler.post {
            dismiss()
            val density = app.resources.displayMetrics.density
            fun dp(v: Int) = (v * density).toInt()

            val box = LinearLayout(app).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(18), dp(14), dp(18), dp(14))
                background = GradientDrawable().apply {
                    cornerRadius = dp(16).toFloat()
                    setColor(if (msg.ambiguous) 0xF2B26A00.toInt() else 0xF2263238.toInt())
                }
                setOnClickListener { dismiss() }
            }
            box.addView(TextView(app).apply {
                text = msg.title
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            })
            box.addView(TextView(app).apply {
                text = msg.body
                setTextColor(Color.WHITE)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
                setPadding(0, dp(4), 0, 0)
            })

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP
                x = 0
                y = dp(Prefs.overlayTop(app))   // se configura en la pantalla de la app
            }
            // Margen lateral: se logra con un contenedor transparente.
            val holder = LinearLayout(app).apply {
                setPadding(dp(12), 0, dp(12), 0)
                addView(box, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ))
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
