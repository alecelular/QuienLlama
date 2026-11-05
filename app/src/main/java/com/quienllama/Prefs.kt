package com.quienllama

import android.content.Context

object Prefs {
    /** Ambigüedad: mostrar los nombres de todos los contactos que coinciden. */
    const val AMB_ALL = 0

    /** Ambigüedad: mostrar solo el primero (orden alfabético) e indicar que hay más. */
    const val AMB_FIRST = 1

    /** Ambigüedad: no mostrar nombres, solo avisar que hay varios. */
    const val AMB_NONE = 2

    private fun p(c: Context) =
        c.applicationContext.getSharedPreferences("cfg", Context.MODE_PRIVATE)

    /** Cantidad mínima de dígitos finales a comparar. */
    fun minDigits(c: Context): Int = p(c).getInt("min", 10)

    /**
     * Qué mostrar si hay ambigüedad (AMB_ALL, AMB_FIRST o AMB_NONE).
     * Si solo existe el ajuste viejo (true = listar, false = solo aviso), se traduce.
     */
    fun ambiguityMode(c: Context): Int {
        val sp = p(c)
        if (sp.contains("amb")) return sp.getInt("amb", AMB_ALL).coerceIn(AMB_ALL, AMB_NONE)
        return if (sp.getBoolean("list", true)) AMB_ALL else AMB_NONE
    }

    /** Segundos que el cartel queda en pantalla (se cierra tocándolo). */
    fun overlaySeconds(c: Context): Int = p(c).getInt("secs", 30)

    /** Distancia del cartel desde el borde superior, en dp. */
    fun overlayTop(c: Context): Int = p(c).getInt("top", 96)

    /** Si ya se pidió el permiso de notificaciones (para saber cuándo mandar a Ajustes). */
    fun notifAsked(c: Context): Boolean = p(c).getBoolean("notifAsked", false)

    fun setNotifAsked(c: Context) {
        p(c).edit().putBoolean("notifAsked", true).apply()
    }

    fun save(c: Context, min: Int, amb: Int, secs: Int, top: Int) {
        p(c).edit()
            .putInt("min", min.coerceIn(4, 15))
            .putInt("amb", amb.coerceIn(AMB_ALL, AMB_NONE))
            .remove("list")
            .putInt("secs", secs.coerceIn(5, 120))
            .putInt("top", top.coerceIn(0, 800))
            .apply()
    }
}
