package io.github.tokyo720022create.soratome.plugin.runtime.install

import io.github.tokyo720022create.soratome.plugin.runtime.error.PluginException
import java.io.ByteArrayOutputStream
import java.io.InputStream

/** Reads a stream into memory, refusing to read more than a set number of bytes. */
object LimitedReader {

    fun read(stream: InputStream, maxBytes: Int, tooLargeMessage: String): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(16 * 1024)
        var total = 0
        while (true) {
            val read = stream.read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) throw PluginException(tooLargeMessage)
            output.write(buffer, 0, read)
        }
        return output.toByteArray()
    }
}
