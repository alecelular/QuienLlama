package com.quienllama

import android.telecom.Call
import android.telecom.CallScreeningService

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
                    Announcer.announce(applicationContext, number)
                }
            }
        } catch (_: Exception) {
            // Pase lo que pase, la llamada debe seguir su curso.
        }
        respondToCall(callDetails, CallResponse.Builder().build())
    }
}
