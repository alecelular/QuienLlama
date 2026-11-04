package com.quienllama

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SuffixMatcherTest {

    private fun e(id: Long, name: String, number: String) =
        ContactEntry(id, name, SuffixMatcher.digitsOf(number))

    private val min = 10

    @Test
    fun claudeConPrefijo_contactoGuardadoConPrefijo_esUnico() {
        val list = listOf(e(1, "Juan", "+54 9 11 1234-5678"))
        val r = SuffixMatcher.match("+5491112345678", list, min)
        assertEquals(MatchResult.Single("Juan", 13), r)
    }

    @Test
    fun movistarConCero_contactoGuardadoConPrefijo_seIdentifica() {
        val list = listOf(e(1, "Juan", "+54 9 11 1234-5678"))
        val r = SuffixMatcher.match("01112345678", list, min)
        assertEquals(MatchResult.Single("Juan", 10), r)
    }

    @Test
    fun claudeConPrefijo_conDuplicadoSinPrefijo_noEsAmbiguo() {
        val list = listOf(
            e(1, "Juan", "+54 9 11 1234-5678"),
            e(2, "Juan copia", "11 1234-5678"),
        )
        val r = SuffixMatcher.match("+5491112345678", list, min)
        assertEquals(MatchResult.Single("Juan", 13), r)
    }

    @Test
    fun movistarConCero_conDuplicadoSinPrefijo_esAmbiguoYListaNombres() {
        val list = listOf(
            e(1, "Juan", "+54 9 11 1234-5678"),
            e(2, "Juan copia", "11 1234-5678"),
        )
        val r = SuffixMatcher.match("01112345678", list, min)
        assertEquals(MatchResult.Ambiguous(listOf("Juan", "Juan copia"), 10), r)
    }

    @Test
    fun duplicadoConMismoNombre_seSigueInformandoComoAmbiguo() {
        val list = listOf(
            e(1, "Ana", "+54 9 351 555-1111"),
            e(2, "Ana", "351 555-1111"),
        )
        val r = SuffixMatcher.match("03515551111", list, min)
        assertEquals(MatchResult.Ambiguous(listOf("Ana", "Ana"), 10), r)
    }

    @Test
    fun mismoContactoConDosNumeros_noEsAmbiguo() {
        val list = listOf(
            e(1, "Luis", "+54 9 11 4444-0000"),
            e(1, "Luis", "11 4444-0000"),
        )
        val r = SuffixMatcher.match("01144440000", list, min)
        assertEquals(MatchResult.Single("Luis", 10), r)
    }

    @Test
    fun nuncaBajaDeNivelSiYaEncontroUnaMasLarga() {
        // El segundo contacto solo coincidiría en 10 dígitos, pero el primero ya coincide en 13.
        val list = listOf(
            e(1, "Exacto", "+54 9 11 1234-5678"),
            e(2, "Corto", "11 1234-5678"),
        )
        val r = SuffixMatcher.match("+5491112345678", list, min)
        assertTrue(r is MatchResult.Single && r.name == "Exacto")
    }

    @Test
    fun sinCoincidenciaPorDebajoDelMinimo() {
        val list = listOf(e(1, "Otro", "+54 9 11 9999-5678"))
        // Coinciden los últimos 4 dígitos, pero el mínimo es 10.
        val r = SuffixMatcher.match("01112345678", list, min)
        assertEquals(MatchResult.None, r)
    }

    @Test
    fun numeroCortoSoloPorIgualdadExacta() {
        val list = listOf(
            e(1, "Largo", "+54 9 11 1234-0147"),
            e(2, "Corto", "147"),
        )
        val r = SuffixMatcher.match("147", list, min)
        assertEquals(MatchResult.Single("Corto", 3), r)
    }

    @Test
    fun numeroVacioONoNumerico() {
        assertEquals(MatchResult.None, SuffixMatcher.match("", emptyList(), min))
        assertEquals(MatchResult.None, SuffixMatcher.match("abc", emptyList(), min))
    }
}
