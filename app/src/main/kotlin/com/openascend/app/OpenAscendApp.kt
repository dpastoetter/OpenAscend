package com.openascend.app

import android.app.Application
import com.openascend.app.notifications.ReminderChannels
import com.openascend.app.notifications.ReminderWorker
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenAscendApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderChannels.ensure(this)
        ReminderWorker.schedule(this)
    }
}
