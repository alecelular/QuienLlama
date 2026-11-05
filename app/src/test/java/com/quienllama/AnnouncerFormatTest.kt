package com.quienllama

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AnnouncerFormatTest {

    private val amb = MatchResult.Ambiguous(listOf("Ana", "Ana", "Beto"), 10)
    private val number = "01112345678"

    @Test
    fun sinCoincidencia_noMuestraNada() {
        assertNull(Announcer.format(MatchResult.None, number, Prefs.AMB_ALL))
    }

    @Test
    fun contactoUnico_ignoraElModoDeAmbiguedad() {
        for (mode in listOf(Prefs.AMB_ALL, Prefs.AMB_FIRST, Prefs.AMB_NONE)) {
            val m = Announcer.format(MatchResult.Single("Juan", 13), number, mode)!!
            assertEquals("Juan", m.title)
            assertEquals(false, m.ambiguous)
        }
    }

    @Test
    fun ambiguo_todos_listaNombresConRepetidos() {
        val m = Announcer.format(amb, number, Prefs.AMB_ALL)!!
        assertTrue(m.body.contains("Ana  ×2"))
        assertTrue(m.body.contains("Beto"))
        assertTrue(m.ambiguous)
    }

    @Test
    fun ambiguo_primero_muestraElPrimerNombreYCuantosSon() {
        val m = Announcer.format(amb, number, Prefs.AMB_FIRST)!!
        assertEquals("Ana", m.title)
        assertTrue(m.body.contains(number))
        assertTrue(m.body.contains("1ª de 3"))
        assertTrue(m.ambiguous)
    }

    @Test
    fun ambiguo_ninguno_soloAvisa() {
        val m = Announcer.format(amb, number, Prefs.AMB_NONE)!!
        assertTrue(m.title.contains("Varios contactos coinciden (3)"))
        assertTrue(m.body.contains("10 dígitos"))
        assertTrue(!m.body.contains("Beto"))
    }
}
