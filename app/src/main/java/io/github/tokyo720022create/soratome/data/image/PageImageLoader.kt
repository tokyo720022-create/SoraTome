package io.github.tokyo720022create.soratome.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.util.LruCache
import io.github.tokyo720022create.soratome.plugin.runtime.network.NetworkPolicy
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Downloads and decodes page and cover images.
 *
 * - Every address, and every redirect, is checked against the plugin's allowed domains.
 * - Images are only kept in memory (a size-limited cache). Nothing is written to disk.
 * - Large images are decoded at a reduced size so long strips do not exhaust memory.
 */
class PageImageLoader {

    private val cache = object : LruCache<String, Bitmap>(defaultCacheBytes()) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.allocationByteCount
    }
    private val gate = Semaphore(MAX_PARALLEL_DOWNLOADS)
    private val reporter = ImageLoadReporter()

    /**
     * @param maxWidthPx Widest the decoded image needs to be.
     * @param reportUrl Where to send the load report, or null to send none.
     */
    suspend fun load(
        url: String,
        policy: NetworkPolicy,
        maxWidthPx: Int,
        reportUrl: String? = null,
    ): Bitmap {
        cache.get(url)?.let { return it }

        return gate.withPermit {
            cache.get(url)?.let { return@withPermit it }

            val started = SystemClock.elapsedRealtime()
            var downloadedBytes = 0
            var cachedByServer = false
            try {
                val downloaded = withContext(Dispatchers.IO) { download(url, policy) }
                downloadedBytes = downloaded.bytes.size
                cachedByServer = downloaded.cachedByServer
                val bitmap = withContext(Dispatchers.Default) { decode(downloaded.bytes, maxWidthPx) }
                cache.put(url, bitmap)
                if (reportUrl != null) {
                    reporter.report(
                        reportUrl, policy, url, true, downloadedBytes,
                        SystemClock.elapsedRealtime() - started, cachedByServer,
                    )
                }
                bitmap
            } catch (e: CancellationException) {
                throw e
            } catch (e: OutOfMemoryError) {
                cache.evictAll()
                throw PageLoadException("Not enough memory to show this page", cause = e)
            } catch (e: IOException) {
                reportFailure(reportUrl, policy, url, downloadedBytes, started, cachedByServer)
                throw PageLoadException("Could not connect to ${hostPort(url)}", cause = e)
            } catch (e: PageLoadException) {
                reportFailure(reportUrl, policy, url, downloadedBytes, started, cachedByServer)
                throw e
            }
        }
    }

    private fun reportFailure(
        reportUrl: String?,
        policy: NetworkPolicy,
        url: String,
        bytes: Int,
        started: Long,
        cached: Boolean,
    ) {
        if (reportUrl != null) {
            reporter.report(reportUrl, policy, url, false, bytes, SystemClock.elapsedRealtime() - started, cached)
        }
    }

    private class Downloaded(val bytes: ByteArray, val cachedByServer: Boolean)

    private fun download(url: String, policy: NetworkPolicy): Downloaded {
        var target: URL = policy.check(url)
        for (attempt in 0..MAX_REDIRECTS) {
            val connection = target.openConnection() as HttpURLConnection
            try {
                connection.instanceFollowRedirects = false
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.setRequestProperty("User-Agent", "SoraTome/0.1")

                val code = connection.responseCode
                when {
                    code in 200..299 -> {
                        val hit = connection.getHeaderField("X-Cache")
                            ?.startsWith("HIT", ignoreCase = true) == true
                        return Downloaded(readLimited(connection.inputStream), hit)
                    }
                    code in 300..399 -> {
                        val location = connection.getHeaderField("Location")
                            ?: throw PageLoadException("Redirect without a Location header", code)
                        target = policy.check(URL(target, location).toString())
                    }
                    else -> throw PageLoadException(describeHttp(code, target), code)
                }
            } finally {
                connection.disconnect()
            }
        }
        throw PageLoadException("Too many redirects")
    }

    private fun readLimited(stream: InputStream): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0
        stream.use { input ->
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                total += read
                if (total > MAX_IMAGE_BYTES) throw PageLoadException("Image is too large")
                output.write(buffer, 0, read)
            }
        }
        return output.toByteArray()
    }

    private fun decode(bytes: ByteArray, maxWidthPx: Int): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            throw PageLoadException("The server did not return an image")
        }

        var sample = 1
        while (
            bounds.outWidth / sample > maxWidthPx ||
            (bounds.outWidth / sample).toLong() * (bounds.outHeight / sample) > MAX_PIXELS
        ) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: throw PageLoadException("The server did not return an image")
    }

    private fun describeHttp(code: Int, target: URL): String = when (code) {
        403 -> "Refused by ${hostPort(target)} (403)"
        404 -> "Not found on ${hostPort(target)} (404). The address may have expired"
        429 -> "Rate limited by ${hostPort(target)} (429)"
        else -> "HTTP $code from ${hostPort(target)}"
    }

    private fun hostPort(url: String): String =
        try {
            hostPort(URL(url))
        } catch (e: Exception) {
            "the image server"
        }

    private fun hostPort(url: URL): String =
        if (url.port == -1) url.host else "${url.host}:${url.port}"

    private companion object {
        const val MAX_PARALLEL_DOWNLOADS = 4
        const val MAX_REDIRECTS = 3
        const val MAX_IMAGE_BYTES = 25 * 1024 * 1024
        const val MAX_PIXELS = 12_000_000L

        fun defaultCacheBytes(): Int = (Runtime.getRuntime().maxMemory() / 8).toInt()
    }
}
