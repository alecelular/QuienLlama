package com.quienllama

import android.content.Context

object Prefs {
    private fun p(c: Context) =
        c.applicationContext.getSharedPreferences("cfg", Context.MODE_PRIVATE)

    /** Cantidad mínima de dígitos finales a comparar. */
    fun minDigits(c: Context): Int = p(c).getInt("min", 10)

    /** Si hay ambigüedad: true = listar todos los nombres, false = solo aviso. */
    fun listNames(c: Context): Boolean = p(c).getBoolean("list", true)

    /** Segundos que el cartel queda en pantalla (se cierra tocándolo). */
    fun overlaySeconds(c: Context): Int = p(c).getInt("secs", 30)

    /** Distancia del cartel desde el borde superior, en dp. */
    fun overlayTop(c: Context): Int = p(c).getInt("top", 96)

    fun save(c: Context, min: Int, list: Boolean, secs: Int, top: Int) {
        p(c).edit()
            .putInt("min", min.coerceIn(4, 15))
            .putBoolean("list", list)
            .putInt("secs", secs.coerceIn(5, 120))
            .putInt("top", top.coerceIn(0, 800))
            .apply()
    }
}
