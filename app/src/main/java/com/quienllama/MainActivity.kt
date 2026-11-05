package com.quienllama

import android.Manifest
import android.app.Activity
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var tvContacts: TextView
    private lateinit var tvOverlay: TextView
    private lateinit var tvRole: TextView
    private lateinit var tvNotif: TextView
    private lateinit var etMin: EditText
    private lateinit var etSecs: EditText
    private lateinit var etTop: EditText
    private lateinit var rgAmb: RadioGroup
    private lateinit var etTest: EditText
    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvContacts = findViewById(R.id.tvContacts)
        tvOverlay = findViewById(R.id.tvOverlay)
        tvRole = findViewById(R.id.tvRole)
        tvNotif = findViewById(R.id.tvNotif)
        etMin = findViewById(R.id.etMin)
        etSecs = findViewById(R.id.etSecs)
        etTop = findViewById(R.id.etTop)
        rgAmb = findViewById(R.id.rgAmb)
        etTest = findViewById(R.id.etTest)
        tvResult = findViewById(R.id.tvResult)

        findViewById<Button>(R.id.btnContacts).setOnClickListener {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), REQ_CONTACTS)
        }
        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }
        findViewById<Button>(R.id.btnRole).setOnClickListener {
            val rm = getSystemService(RoleManager::class.java)
            if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                startActivityForResult(
                    rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING), REQ_ROLE
                )
            } else {
                Toast.makeText(this, "Este teléfono no ofrece ese rol", Toast.LENGTH_LONG).show()
            }
        }
        findViewById<Button>(R.id.btnNotif).setOnClickListener { askNotifications() }
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            val min = etMin.text.toString().toIntOrNull() ?: Prefs.minDigits(this)
            val secs = etSecs.text.toString().toIntOrNull() ?: Prefs.overlaySeconds(this)
            val top = etTop.text.toString().toIntOrNull() ?: Prefs.overlayTop(this)
            val amb = when (rgAmb.checkedRadioButtonId) {
                R.id.rbFirst -> Prefs.AMB_FIRST
                R.id.rbNone -> Prefs.AMB_NONE
                else -> Prefs.AMB_ALL
            }
            Prefs.save(this, min, amb, secs, top)
            loadPrefs()
            Toast.makeText(this, "Guardado", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnTest).setOnClickListener {
            val number = etTest.text.toString()
            val msg = Announcer.announce(this, number)
            tvResult.text = when {
                msg != null -> "${msg.title}\n${msg.body}"
                else -> "Sin coincidencia (o número vacío)."
            }
            if (msg != null && !Settings.canDrawOverlays(this)) {
                tvResult.append("\n\n(Falta el permiso de mostrar sobre otras apps: no se vio el cartel.)")
            }
        }

        // Solo abre la página en el navegador: la app no tiene permiso de Internet.
        findViewById<Button>(R.id.btnCafe).setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(CAFECITO_URL)))
            } catch (_: Exception) {
                Toast.makeText(this, CAFECITO_URL, Toast.LENGTH_LONG).show()
            }
        }
        findViewById<TextView>(R.id.tvVersion).text =
            "Quién llama " + packageManager.getPackageInfo(packageName, 0).versionName +
                " · © 2026 Alejandro F. Fernández (@alecelular)"

        loadPrefs()
    }

    override fun onResume() {
        super.onResume()
        ContactCache.invalidate()
        refreshStatus()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ContactCache.invalidate()
        refreshStatus()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        @Suppress("DEPRECATION")
        super.onActivityResult(requestCode, resultCode, data)
        refreshStatus()
    }

    private fun loadPrefs() {
        etMin.setText(Prefs.minDigits(this).toString())
        etSecs.setText(Prefs.overlaySeconds(this).toString())
        etTop.setText(Prefs.overlayTop(this).toString())
        rgAmb.check(
            when (Prefs.ambiguityMode(this)) {
                Prefs.AMB_FIRST -> R.id.rbFirst
                Prefs.AMB_NONE -> R.id.rbNone
                else -> R.id.rbAll
            }
        )
    }

    /**
     * Primero pide el permiso de notificaciones (Android 13+); si ya se pidió y sigue sin estar,
     * o si falta el de pantalla completa (Android 14+), abre la pantalla de Ajustes que corresponde.
     */
    private fun askNotifications() {
        val nm = getSystemService(NotificationManager::class.java)
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        when {
            needsPermission && !Prefs.notifAsked(this) -> {
                Prefs.setNotifAsked(this)
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), REQ_NOTIF)
            }
            !nm.areNotificationsEnabled() -> startActivity(
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            )
            Build.VERSION.SDK_INT >= 34 && !nm.canUseFullScreenIntent() -> startActivity(
                Intent(
                    Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,
                    Uri.parse("package:$packageName")
                )
            )
            else -> Toast.makeText(this, "Ya está permitido", Toast.LENGTH_SHORT).show()
        }
    }

    private fun refreshStatus() {
        val contactsOk = checkSelfPermission(Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED
        val overlayOk = Settings.canDrawOverlays(this)
        val roleOk = getSystemService(RoleManager::class.java)
            .isRoleHeld(RoleManager.ROLE_CALL_SCREENING)

        tvContacts.text = (if (contactsOk) "✔" else "✘") + " Leer contactos"
        tvOverlay.text = (if (overlayOk) "✔" else "✘") + " Mostrar sobre otras apps"
        tvRole.text = (if (roleOk) "✔" else "✘") + " App de identificación de llamadas"
        tvNotif.text = (if (LockScreen.canShow(this)) "✔" else "✘") +
            " Aviso con el teléfono bloqueado (notificaciones y pantalla completa)"
    }

    private companion object {
        const val REQ_CONTACTS = 1
        const val REQ_ROLE = 2
        const val REQ_NOTIF = 3
        const val CAFECITO_URL = "https://cafecito.app/rsp148"
    }
}
