package com.openascend.app.util

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Reads untrusted user-picked documents with a hard size cap to avoid OOM.
 */
object BoundedStreams {
    const val MAX_NARRATIVE_PACK_BYTES = 1 * 1024 * 1024
    const val MAX_CHRONICLE_SHARE_BYTES = 256 * 1024
    const val MAX_BACKUP_BYTES = 32 * 1024 * 1024

    fun readTextLimited(stream: InputStream, maxBytes: Int): String {
        val out = ByteArrayOutputStream(minOf(maxBytes, 64 * 1024))
        val chunk = ByteArray(8 * 1024)
        var total = 0
        while (true) {
            val n = stream.read(chunk)
            if (n < 0) break
            total += n
            if (total > maxBytes) {
                throw IOException("File exceeds size limit of $maxBytes bytes")
            }
            out.write(chunk, 0, n)
        }
        return out.toString(Charsets.UTF_8.name())
    }
}
