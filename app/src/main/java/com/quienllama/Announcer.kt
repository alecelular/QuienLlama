package com.quienllama

import android.content.Context

class Message(val title: String, val body: String, val ambiguous: Boolean)

object Announcer {

    fun resolve(ctx: Context, number: String): MatchResult =
        SuffixMatcher.match(number, ContactCache.get(ctx), Prefs.minDigits(ctx))

    /** Texto a mostrar, o null si no hay coincidencia (en ese caso no se muestra nada). */
    fun format(r: MatchResult, incoming: String, listNames: Boolean): Message? = when (r) {
        is MatchResult.None -> null
        is MatchResult.Single ->
            Message(r.name, incoming, ambiguous = false)
        is MatchResult.Ambiguous -> {
            val title = "⚠ Varios contactos coinciden (${r.names.size})"
            val body = if (listNames) {
                // "Ana ×2" cuando el mismo nombre está repetido: contacto duplicado.
                r.names.groupingBy { it }.eachCount().entries.joinToString("\n") { (n, k) ->
                    if (k > 1) "$n  ×$k" else n
                } + "\nRevisá la agenda: posible duplicado"
            } else {
                "$incoming\nCoinciden en ${r.level} dígitos"
            }
            Message(title, body, ambiguous = true)
        }
    }

    /** Resuelve el número, muestra el cartel y devuelve lo mostrado (o null). */
    fun announce(ctx: Context, number: String): Message? {
        val msg = format(resolve(ctx, number), number, Prefs.listNames(ctx)) ?: return null
        Overlay.show(ctx, msg, Prefs.overlaySeconds(ctx))
        return msg
    }
}
