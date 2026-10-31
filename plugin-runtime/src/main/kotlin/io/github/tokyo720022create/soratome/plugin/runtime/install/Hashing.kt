package io.github.tokyo720022create.soratome.plugin.runtime.install

import java.security.MessageDigest

object Hashing {

    /** SHA-256 of [bytes] as 64 lowercase hex characters. */
    fun sha256Hex(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }
}
