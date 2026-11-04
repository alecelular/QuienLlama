package com.quienllama

/** Un número de la agenda. Un mismo contacto puede tener varias entradas (varios números). */
class ContactEntry(val contactId: Long, val name: String, val digits: String)

sealed class MatchResult {
    /** Ningún contacto coincide. */
    object None : MatchResult()

    /** Un único contacto coincide en [level] dígitos. */
    data class Single(val name: String, val level: Int) : MatchResult()

    /**
     * Más de un contacto distinto coincide en el mismo nivel.
     * [names] trae un nombre por contacto (puede repetirse si hay contactos duplicados).
     */
    data class Ambiguous(val names: List<String>, val level: Int) : MatchResult()
}

/**
 * Busca por "sufijo más largo":
 *  1. Prueba con todos los dígitos del número entrante.
 *  2. Si no hay coincidencia, quita un dígito por la izquierda y repite.
 *  3. Se detiene al llegar a [minDigits].
 *  4. En cuanto un nivel tiene coincidencias: un contacto -> identificado; varios -> ambigüedad.
 *     Nunca baja a un nivel más corto después de haber encontrado algo.
 *
 * Un contacto coincide si SUS dígitos terminan igual que el sufijo probado.
 * (La comparación es en un solo sentido, por eso un contacto guardado sin prefijo
 * no entra cuando llega el número completo con +549.)
 * Los contactos se cuentan por contactId: dos números de la misma persona no son ambigüedad.
 */
object SuffixMatcher {

    fun digitsOf(s: String): String = s.filter { it in '0'..'9' }

    fun match(incoming: String, entries: List<ContactEntry>, minDigits: Int): MatchResult {
        val d = digitsOf(incoming)
        if (d.isEmpty()) return MatchResult.None
        val min = minDigits.coerceAtLeast(1)

        // Número más corto que el mínimo (ej. 147, 4 dígitos): solo igualdad exacta.
        if (d.length < min) {
            return resolve(entries.filter { it.digits == d }, d.length)
        }

        for (level in d.length downTo min) {
            val suffix = d.substring(d.length - level)
            val found = entries.filter { it.digits.endsWith(suffix) }
            if (found.isNotEmpty()) return resolve(found, level)
        }
        return MatchResult.None
    }

    private fun resolve(found: List<ContactEntry>, level: Int): MatchResult {
        if (found.isEmpty()) return MatchResult.None
        val byContact = found.groupBy { it.contactId }
        if (byContact.size == 1) {
            return MatchResult.Single(found.first().name, level)
        }
        val names = byContact.values.map { it.first().name }.sortedBy { it.lowercase() }
        return MatchResult.Ambiguous(names, level)
    }
}
