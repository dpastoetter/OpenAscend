package com.openascend.app.security

import android.os.StrictMode
import com.openascend.app.BuildConfig

/**
 * Debug-only VM checks for leaked SQLite / closables (Room, streams).
 * No-op in release.
 */
internal object DebugStrictMode {
    fun install() {
        if (!BuildConfig.DEBUG) return
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build(),
        )
    }
}
