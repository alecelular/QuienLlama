package com.quienllama

import android.content.Context
import android.os.Handler
import android.os.Looper

class Message(val title: String, val body: String, val ambiguous: Boolean)

object Announcer {

    /**
     * Con el teléfono bloqueado, el cartel se pide un instante después de responder al sistema:
     * así queda por encima de la pantalla de llamada y no tapado por ella.
     */
    const val LOCK_DELAY_MS = 1500L

    fun resolve(ctx: Context, number: String): MatchResult =
        SuffixMatcher.match(number, ContactCache.get(ctx), Prefs.minDigits(ctx))

    /** Texto a mostrar, o null si no hay coincidencia (en ese caso no se muestra nada). */
    fun format(r: MatchResult, incoming: String, ambiguityMode: Int): Message? = when (r) {
        is MatchResult.None -> null
        is MatchResult.Single ->
            Message(r.name, incoming, ambiguous = false)
        is MatchResult.Ambiguous -> when (ambiguityMode) {
            // Primer nombre (orden alfabético, ya viene ordenado) y cuántos más coinciden.
            Prefs.AMB_FIRST -> Message(
                r.names.first(),
                "$incoming\n⚠ 1ª de ${r.names.size} coincidencias",
                ambiguous = true
            )
            Prefs.AMB_NONE -> Message(
                "⚠ Varios contactos coinciden (${r.names.size})",
                "$incoming\nCoinciden en ${r.level} dígitos",
                ambiguous = true
            )
            else -> Message(
                "⚠ Varios contactos coinciden (${r.names.size})",
                // "Ana ×2" cuando el mismo nombre está repetido: contacto duplicado.
                r.names.groupingBy { it }.eachCount().entries.joinToString("\n") { (n, k) ->
                    if (k > 1) "$n  ×$k" else n
                } + "\nRevisá la agenda: posible duplicado",
                ambiguous = true
            )
        }
    }

    /** Resuelve el número, muestra el cartel y devuelve lo mostrado (o null). */
    fun announce(ctx: Context, number: String): Message? {
        val app = ctx.applicationContext
        val msg = format(resolve(app, number), number, Prefs.ambiguityMode(app)) ?: return null
        val secs = Prefs.overlaySeconds(app)

        if (LockScreen.isLocked(app)) {
            Handler(Looper.getMainLooper()).postDelayed({
                // Si no se pudo pedir pantalla completa (sin permiso), queda el intento flotante.
                if (!LockScreen.show(app, msg, secs)) Overlay.show(app, msg, secs, overLockScreen = true)
            }, LOCK_DELAY_MS)
        } else {
            Overlay.show(app, msg, secs)
        }
        return msg
    }
}
