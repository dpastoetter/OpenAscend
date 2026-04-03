package com.openascend.app.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileAvatarImporter @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    suspend fun importFrom(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        val bmp = decodeSampled(context, uri, MAX_SIDE) ?: return@withContext false
        try {
            val dir = File(context.filesDir, AVATAR_DIR).apply { mkdirs() }
            val out = File(dir, AVATAR_FILE)
            FileOutputStream(out).use { fos ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, fos)) {
                    return@withContext false
                }
            }
            true
        } finally {
            bmp.recycle()
        }
    }

    fun deleteStoredFile() {
        File(context.filesDir, RELATIVE_PATH).delete()
    }

    companion object {
        private const val AVATAR_DIR = "avatars"
        private const val AVATAR_FILE = "profile.jpg"
        const val RELATIVE_PATH = "$AVATAR_DIR/$AVATAR_FILE"
        private const val MAX_SIDE = 720
        private const val JPEG_QUALITY = 88
    }
}

private fun decodeSampled(context: Context, uri: Uri, maxSide: Int): Bitmap? {
    val resolver = context.contentResolver
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    val opts = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(bounds.outWidth, bounds.outHeight, maxSide)
    }
    return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
}

private fun calculateInSampleSize(width: Int, height: Int, maxSide: Int): Int {
    var inSampleSize = 1
    val maxDim = maxOf(width, height)
    while (maxDim / inSampleSize > maxSide * 2) {
        inSampleSize *= 2
    }
    return inSampleSize
}
