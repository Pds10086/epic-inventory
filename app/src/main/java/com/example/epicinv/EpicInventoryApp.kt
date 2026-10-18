package com.example.epicinv

import android.app.Application
import com.example.epicinv.notify.AutoClaimWorker
import com.example.epicinv.notify.Channels
import com.example.epicinv.notify.WeeklyFreeGameWorker

class EpicInventoryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Channels.ensureAll(this)
        WeeklyFreeGameWorker.schedule(this)
        AutoClaimWorker.schedule(this)
    }
}
