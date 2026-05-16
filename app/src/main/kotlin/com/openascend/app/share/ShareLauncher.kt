package com.openascend.app.share

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ShareLauncher {
    suspend fun shareBitmap(
        context: Context,
        bitmap: Bitmap,
        chooserTitle: String,
        shareText: String,
        filePrefix: String = "openascend_share",
    ) {
        withContext(Dispatchers.IO) {
            val file = File(context.cacheDir, "${filePrefix}_${System.currentTimeMillis()}.png")
            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file,
                )
                ShareCompat.IntentBuilder(context)
                    .setType("image/png")
                    .setStream(uri)
                    .setText(shareText)
                    .setChooserTitle(chooserTitle)
                    .apply {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    .startChooser()
            } finally {
                bitmap.recycle()
            }
        }
    }
}
