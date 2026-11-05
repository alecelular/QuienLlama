package com.quienllama

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Con el teléfono bloqueado, Android no dibuja ventanas flotantes sobre la pantalla de bloqueo.
 * La vía permitida es una notificación de "pantalla completa" (la misma técnica que usan las
 * apps de llamadas): el sistema abre [LockScreenActivity], que sí puede verse sobre el bloqueo.
 */
object LockScreen {

    private const val CHANNEL = "caller_lock"
    private const val NOTIF_ID = 4001

    const val EXTRA_TITLE = "title"
    const val EXTRA_BODY = "body"
    const val EXTRA_AMBIGUOUS = "ambiguous"
    const val EXTRA_SECONDS = "seconds"

    fun isLocked(ctx: Context): Boolean =
        ctx.getSystemService(KeyguardManager::class.java)?.isKeyguardLocked == true

    /** ¿Están dados los permisos para que el aviso aparezca sobre el bloqueo? */
    fun canShow(ctx: Context): Boolean {
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return false
        if (!nm.areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < 34 || nm.canUseFullScreenIntent()
    }

    /** Publica el aviso. Devuelve false si faltan permisos (entonces no se pudo mostrar). */
    fun show(ctx: Context, msg: Message, seconds: Int): Boolean {
        if (!canShow(ctx)) return false
        val nm = ctx.getSystemService(NotificationManager::class.java) ?: return false

        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Quién llama (pantalla bloqueada)", NotificationManager.IMPORTANCE_HIGH).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                setSound(null, null)
                enableVibration(false)
            }
        )

        val intent = Intent(ctx, LockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(EXTRA_TITLE, msg.title)
            putExtra(EXTRA_BODY, msg.body)
            putExtra(EXTRA_AMBIGUOUS, msg.ambiguous)
            putExtra(EXTRA_SECONDS, seconds)
        }
        val pi = PendingIntent.getActivity(
            ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val n = Notification.Builder(ctx, CHANNEL)
            .setSmallIcon(android.R.drawable.sym_action_call)
            .setContentTitle(msg.title)
            .setContentText(msg.body.replace("\n", " · "))
            .setStyle(Notification.BigTextStyle().bigText(msg.body))
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setTimeoutAfter(seconds * 1000L)
            .setContentIntent(pi)
            .setFullScreenIntent(pi, true)
            .build()
        nm.notify(NOTIF_ID, n)
        return true
    }

    fun cancel(ctx: Context) {
        ctx.getSystemService(NotificationManager::class.java)?.cancel(NOTIF_ID)
    }
}
