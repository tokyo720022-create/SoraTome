package io.github.tokyo720022create.soratome.plugin.runtime.install

import io.github.tokyo720022create.soratome.plugin.runtime.error.PluginException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL

/** Downloads a file over https, with a size limit. Call it from a background thread. */
object PluginDownloader {

    private const val MAX_REDIRECTS = 3

    fun download(url: String, maxBytes: Int): ByteArray {
        var target = parseHttps(url)
        for (attempt in 0..MAX_REDIRECTS) {
            val connection = target.openConnection() as HttpURLConnection
            try {
                connection.instanceFollowRedirects = false
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.setRequestProperty("User-Agent", "SoraTome/0.1")

                val code = try {
                    connection.responseCode
                } catch (e: IOException) {
                    throw PluginException("Could not connect to ${target.host}", e)
                }
                when {
                    code in 200..299 -> {
                        return try {
                            LimitedReader.read(connection.inputStream, maxBytes, "The download is too large")
                        } catch (e: IOException) {
                            throw PluginException("The download was interrupted", e)
                        }
                    }
                    code in 300..399 -> {
                        val location = connection.getHeaderField("Location")
                            ?: throw PluginException("The link redirects nowhere")
                        target = parseHttps(URL(target, location).toString())
                    }
                    else -> throw PluginException("HTTP $code from ${target.host}")
                }
            } finally {
                connection.disconnect()
            }
        }
        throw PluginException("Too many redirects")
    }

    private fun parseHttps(url: String): URL {
        val parsed = try {
            URL(url.trim())
        } catch (e: MalformedURLException) {
            throw PluginException("This is not a valid link")
        }
        if (parsed.protocol != "https") throw PluginException("Only https links are supported")
        return parsed
    }
}
