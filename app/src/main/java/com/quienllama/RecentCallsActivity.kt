package com.quienllama

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CallLog
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import java.text.DateFormat
import java.util.Date

/**
 * Últimas llamadas con el mismo criterio de coincidencia que el cartel
 * (el marcador del sistema no se puede modificar desde otra app).
 */
class RecentCallsActivity : Activity() {

    private class Row(val title: String, val subtitle: String)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Últimas llamadas"

        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            showMessage("Falta el permiso para leer el registro de llamadas.")
            return
        }

        val rows = try {
            load()
        } catch (e: Exception) {
            showMessage("No se pudo leer el registro de llamadas.")
            return
        }
        if (rows.isEmpty()) {
            showMessage("No hay llamadas en el registro.")
            return
        }

        val list = ListView(this)
        list.adapter = object : ArrayAdapter<Row>(
            this, android.R.layout.simple_list_item_2, android.R.id.text1, rows
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val v = super.getView(position, convertView, parent)
                val row = getItem(position)!!
                v.findViewById<TextView>(android.R.id.text1).text = row.title
                v.findViewById<TextView>(android.R.id.text2).text = row.subtitle
                return v
            }
        }
        setContentView(list)

        // Aprovecha para completar nombres en el registro si el experimento está activado.
        CallLogNames.fillAsync(this)
    }

    private fun showMessage(text: String) {
        setContentView(TextView(this).apply {
            this.text = text
            textSize = 16f
            val pad = (16 * resources.displayMetrics.density).toInt()
            setPadding(pad, pad, pad, pad)
        })
    }

    private fun load(): List<Row> {
        val mode = Prefs.ambiguityMode(this)
        val df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        val args = Bundle().apply {
            putInt(ContentResolver.QUERY_ARG_LIMIT, MAX_ROWS)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(CallLog.Calls.DATE))
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
        }
        val projection = arrayOf(
            CallLog.Calls.NUMBER, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.CACHED_NAME
        )
        val out = ArrayList<Row>()
        contentResolver.query(CallLog.Calls.CONTENT_URI, projection, args, null)?.use { c ->
            while (c.moveToNext()) {
                val number = c.getString(0) ?: ""
                val type = typeLabel(c.getInt(1))
                val whenText = df.format(Date(c.getLong(2)))
                val cached = c.getString(3)
                val tail = "$type · $whenText"

                if (number.isBlank()) {
                    out.add(Row("Número privado", tail))
                    continue
                }
                val msg = Announcer.format(Announcer.resolve(this, number), number, mode)
                out.add(
                    when {
                        msg != null -> Row(msg.title, msg.body + "\n" + tail)
                        !cached.isNullOrBlank() -> Row(cached, number + "\n" + tail)
                        else -> Row(number, tail)
                    }
                )
            }
        }
        return out
    }

    private fun typeLabel(t: Int) = when (t) {
        CallLog.Calls.INCOMING_TYPE -> "Entrante"
        CallLog.Calls.OUTGOING_TYPE -> "Saliente"
        CallLog.Calls.MISSED_TYPE -> "Perdida"
        CallLog.Calls.REJECTED_TYPE -> "Rechazada"
        CallLog.Calls.BLOCKED_TYPE -> "Bloqueada"
        CallLog.Calls.VOICEMAIL_TYPE -> "Buzón de voz"
        else -> "Llamada"
    }

    private companion object {
        const val MAX_ROWS = 100
    }
}
