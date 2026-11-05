package com.quienllama

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import java.util.concurrent.atomic.AtomicBoolean

/**
 * EXPERIMENTAL (apagado por defecto). Completa el campo "nombre guardado" del registro de llamadas
 * en las llamadas que el sistema dejó sin nombre y que la app sí identifica con un único contacto.
 * Si el marcador del teléfono muestra ese campo, el nombre aparece en su lista de recientes.
 * Nunca toca llamadas que ya tienen nombre, ni las ambiguas.
 */
object CallLogNames {

    private val running = AtomicBoolean(false)
    private val registered = AtomicBoolean(false)

    fun hasPermissions(ctx: Context): Boolean =
        ctx.checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED &&
            ctx.checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED

    /** Avisa cuando cambia el registro (termina una llamada). Es seguro llamarlo varias veces. */
    fun register(app: Application) {
        if (!Prefs.writeLogNames(app) || !hasPermissions(app) || !registered.compareAndSet(false, true)) return
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                fillAsync(app)
            }
        }
        try {
            app.contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI, true, observer)
        } catch (_: SecurityException) {
            registered.set(false)
        }
    }

    fun fillAsync(ctx: Context) {
        val app = ctx.applicationContext
        if (!Prefs.writeLogNames(app) || !hasPermissions(app)) return
        if (!running.compareAndSet(false, true)) return
        Thread {
            try {
                fill(app)
            } catch (_: Exception) {
                // Experimento: ante cualquier error no se hace nada más.
            } finally {
                running.set(false)
            }
        }.start()
    }

    private fun fill(app: Context) {
        val cr = app.contentResolver
        val args = Bundle().apply {
            putInt(ContentResolver.QUERY_ARG_LIMIT, 200)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(CallLog.Calls.DATE))
            putInt(
                ContentResolver.QUERY_ARG_SORT_DIRECTION,
                ContentResolver.QUERY_SORT_DIRECTION_DESCENDING
            )
        }
        val projection = arrayOf(CallLog.Calls._ID, CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME)
        val updates = ArrayList<Pair<Long, String>>()
        cr.query(CallLog.Calls.CONTENT_URI, projection, args, null)?.use { c ->
            while (c.moveToNext()) {
                if (!c.getString(2).isNullOrBlank()) continue
                val number = c.getString(1) ?: continue
                if (number.isBlank()) continue
                val r = Announcer.resolve(app, number)
                if (r is MatchResult.Single) updates.add(c.getLong(0) to r.name)
            }
        }
        for ((id, name) in updates) {
            val v = ContentValues().apply { put(CallLog.Calls.CACHED_NAME, name) }
            cr.update(CallLog.Calls.CONTENT_URI, v, "${CallLog.Calls._ID}=?", arrayOf(id.toString()))
        }
    }
}
