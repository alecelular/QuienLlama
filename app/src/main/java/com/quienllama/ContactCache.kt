package com.quienllama

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract

/** Agenda en memoria (solo dígitos). Se recarga sola cuando cambian los contactos. */
object ContactCache {

    @Volatile
    private var entries: List<ContactEntry>? = null

    fun get(ctx: Context): List<ContactEntry> {
        entries?.let { return it }
        return synchronized(this) {
            entries ?: load(ctx.applicationContext).also { entries = it }
        }
    }

    fun invalidate() {
        entries = null
    }

    fun register(app: Application) {
        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                invalidate()
            }
        }
        try {
            app.contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI, true, observer
            )
        } catch (_: SecurityException) {
            // Sin permiso todavía: se recarga al conceder el permiso.
        }
    }

    private fun load(ctx: Context): List<ContactEntry> {
        if (ctx.checkSelfPermission(Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) return emptyList()

        val out = ArrayList<ContactEntry>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )
        ctx.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection, null, null, null
        )?.use { c ->
            val iId = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val iName = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val iNum = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (c.moveToNext()) {
                val digits = SuffixMatcher.digitsOf(c.getString(iNum) ?: "")
                if (digits.isEmpty()) continue
                out.add(ContactEntry(c.getLong(iId), c.getString(iName) ?: digits, digits))
            }
        }
        return out
    }
}
