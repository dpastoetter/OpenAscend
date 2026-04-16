package com.openascend.app.util

import android.content.Context
import java.io.File
import java.io.IOException

/**
 * Resolves user-controlled relative paths strictly under [Context.getFilesDir] to block
 * `..` and symlink escapes (e.g. tampered Room rows on a rooted device).
 */
object SafeUserFiles {

    fun resolveUnderFilesDir(context: Context, relativePath: String): File? {
        if (relativePath.isBlank()) return null
        if (relativePath.contains("..")) return null
        return try {
            val base = context.filesDir.canonicalFile
            val candidate = File(context.filesDir, relativePath).canonicalFile
            val basePath = base.path.trimEnd('/') + "/"
            val candPath = candidate.path
            if (candPath != base.path && !candPath.startsWith(basePath)) {
                null
            } else {
                candidate.takeIf { it.exists() && it.isFile }
            }
        } catch (_: IOException) {
            null
        }
    }
}
