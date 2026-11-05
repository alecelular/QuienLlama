package com.quienllama

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

/**
 * El sistema nos avisa de cada llamada entrante. No bloqueamos nada:
 * solo mostramos el cartel con el nombre y dejamos pasar la llamada.
 */
class CallerService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        try {
            if (callDetails.callDirection == Call.Details.DIRECTION_INCOMING) {
                val number = callDetails.handle?.schemeSpecificPart
                if (!number.isNullOrBlank()) {
                    val msg = Announcer.announce(applicationContext, number)
                    // Para diagnosticar con "adb logcat -s QuienLlama"
                    Log.d(TAG, "llamada entrante: coincidencia=${msg?.title}")
                } else {
                    Log.d(TAG, "llamada entrante sin número")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "error al anunciar", e)
            // Pase lo que pase, la llamada debe seguir su curso.
        }
        respondToCall(callDetails, CallResponse.Builder().build())
    }

    private companion object {
        const val TAG = "QuienLlama"
    }
}
