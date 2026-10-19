package com.openlive.tv.player

import com.openlive.tv.utilities.Urls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

object StreamProbe {
    suspend fun test(url: String): String = withContext(Dispatchers.IO) {
        require(Urls.secureUrl(url)) { "Use a valid, credential-free HTTPS URL." }
        if (Urls.youtubeId(url) != null) return@withContext "YouTube link recognized. Availability, age and region restrictions are checked by YouTube during playback."
        var target = url
        repeat(5) {
            val connection = URI(target).toURL().openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 10_000; connection.readTimeout = 10_000
                connection.instanceFollowRedirects = false
                connection.setRequestProperty("Range", "bytes=0-1023")
                val code = connection.responseCode
                if (code in 300..399) {
                    target = URI(target).resolve(connection.getHeaderField("Location") ?: error("Redirect without a destination.")).toString()
                    require(Urls.secureUrl(target)) { "Redirect to an insecure or credential-bearing URL was blocked." }
                } else {
                    require(code in 200..299) { "Server returned HTTP $code. The source may be offline, restricted, or expired." }
                    val type = connection.contentType.orEmpty()
                    val sample = connection.inputStream.use { input -> val b = ByteArray(1024); val n = input.read(b); if (n > 0) String(b, 0, n) else "" }
                    return@withContext when {
                        sample.trimStart().startsWith("#EXTM3U") || type.contains("mpegurl", ignoreCase = true) -> "HLS playlist reached. Select HLS, including for extensionless URLs. Codec/playback support still needs a playback test."
                        type.contains("text/html") -> "This appears to be a webpage. Enter an authorized direct media URL instead."
                        else -> "Server reachable ($type). This does not verify playable codecs or stream availability."
                    }
                }
            } finally { connection.disconnect() }
        }
        error("Too many redirects.")
    }
}
