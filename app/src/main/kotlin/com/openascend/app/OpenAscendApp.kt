package com.openascend.app

import android.app.Application
import com.openascend.app.notifications.ReminderChannels
import com.openascend.app.notifications.ReminderWorker
import com.openascend.app.security.DebugStrictMode
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OpenAscendApp : Application() {
    override fun onCreate() {
        DebugStrictMode.install()
        super.onCreate()
        ReminderChannels.ensure(this)
        ReminderWorker.schedule(this)
    }
}
