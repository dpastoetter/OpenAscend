package com.openascend.app.health

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.health.connect.client.HealthConnectClient

/**
 * Opens Health Connect when available; falls back to app details settings.
 */
interface HealthConnectBridge {
    fun openHealthConnectManagement(context: Context)
}

class HealthConnectBridgeImpl @javax.inject.Inject constructor() : HealthConnectBridge {

    override fun openHealthConnectManagement(context: Context) {
        val pkg = context.packageName
        runCatching {
            val intent = if (Build.VERSION.SDK_INT >= 34) {
                Intent("androidx.health.ACTION_MANAGE_HEALTH_PERMISSIONS").apply {
                    setPackage("com.google.android.apps.healthdata")
                    putExtra(Intent.EXTRA_PACKAGE_NAME, pkg)
                }
            } else {
                Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }.onFailure {
            openAppDetails(context, pkg)
        }
    }

    private fun openAppDetails(context: Context, pkg: String) {
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", pkg, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(i)
    }
}
