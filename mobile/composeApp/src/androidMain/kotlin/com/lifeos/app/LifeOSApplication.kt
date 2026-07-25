package com.lifeos.app

import android.app.Application
import com.lifeos.app.core.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class LifeOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@LifeOSApplication)
        }
    }
}
