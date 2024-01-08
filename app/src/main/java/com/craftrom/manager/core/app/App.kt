package com.craftrom.manager.core.app

import android.app.Application
import android.content.Context

class App : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        ServiceContext.context = base

        // Pre-heat the shell ASAP
        // Shell.getShell(null) {}
    }
}
