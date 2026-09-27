package com.sunrack.bluebase

import android.app.Application
import com.sunrack.bluebase.core.di.AppContainer

class BluebaseApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.sessionManager.initialize()
    }
}
