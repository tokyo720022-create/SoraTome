package io.github.tokyo720022create.soratome.plugin.runtime.network

import io.github.tokyo720022create.soratome.plugin.runtime.error.PluginException
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Network access offered to plugins. Every request, and every redirect, goes through [policy]. */
internal class HostHttp(private val policy: NetworkPolicy) {

    suspend fun get(url: String): String = withContext(Dispatchers.IO) {
        var target: URL = policy.check(url)
        for (attempt in 0..MAX_REDIRECTS) {
            val connection = target.openConnection() as HttpURLConnection
            try {
                connection.instanceFollowRedirects = false
                connection.connectTimeout = CONNECT_TIMEOUT_MS
                connection.readTimeout = READ_TIMEOUT_MS
                connection.setRequestProperty("User-Agent", "SoraTome/0.1")

                val code = connection.responseCode
                when {
                    code in 200..299 -> return@withContext readLimited(connection.inputStream)
                    code in 300..399 -> {
                        val location = connection.getHeaderField("Location")
                            ?: throw PluginException("Redirect without a Location header")
                        target = policy.check(URL(target, location).toString())
                    }
                    else -> throw PluginException("HTTP $code from ${target.host}")
                }
            } finally {
                connection.disconnect()
            }
        }
        throw PluginException("Too many redirects")
    }

    private fun readLimited(stream: InputStream): String {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var total = 0
        stream.use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                if (total > MAX_BODY_BYTES) throw PluginException("Response is too large")
                output.write(buffer, 0, read)
            }
        }
        return String(output.toByteArray(), Charsets.UTF_8)
    }

    private companion object {
        const val MAX_REDIRECTS = 5
        const val MAX_BODY_BYTES = 5 * 1024 * 1024
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 20_000
    }
}
