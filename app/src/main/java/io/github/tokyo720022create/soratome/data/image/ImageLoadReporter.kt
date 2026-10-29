package io.github.tokyo720022create.soratome.data.image

import io.github.tokyo720022create.soratome.plugin.runtime.network.NetworkPolicy
import java.net.HttpURLConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Sends the "image load report" some image networks ask clients to send:
 * the image address, whether it loaded, its size and how long it took.
 * No account or device data is included. Failures here are ignored.
 */
class ImageLoadReporter {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun report(
        reportUrl: String,
        policy: NetworkPolicy,
        imageUrl: String,
        success: Boolean,
        bytes: Int,
        durationMs: Long,
        cached: Boolean,
    ) {
        scope.launch {
            try {
                val target = policy.check(reportUrl)
                val connection = target.openConnection() as HttpURLConnection
                try {
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.instanceFollowRedirects = false
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 10_000
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.setRequestProperty("User-Agent", "SoraTome/0.1")
                    val body = JSONObject()
                        .put("url", imageUrl)
                        .put("success", success)
                        .put("bytes", bytes)
                        .put("duration", durationMs)
                        .put("cached", cached)
                        .toString()
                    connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                    connection.responseCode
                } finally {
                    connection.disconnect()
                }
            } catch (e: Exception) {
                // Reports are best effort.
            }
        }
    }
}
