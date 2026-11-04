package com.quienllama

import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ContactCache.register(this)
    }
}
