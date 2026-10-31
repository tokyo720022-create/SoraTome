package io.github.tokyo720022create.soratome.plugin.runtime.network

import io.github.tokyo720022create.soratome.plugin.runtime.error.PluginSecurityException
import java.net.InetAddress
import java.net.MalformedURLException
import java.net.URL
import java.net.UnknownHostException

/**
 * Decides which URLs a plugin may contact, based on the domains in its manifest.
 * A domain entry also allows its subdomains. Only https is accepted.
 */
class NetworkPolicy(allowedDomains: List<String>) {

    private val domains = allowedDomains.map { it.trim().lowercase() }.filter { it.isNotEmpty() }

    fun check(url: String): URL {
        val parsed = try {
            URL(url)
        } catch (e: MalformedURLException) {
            throw PluginSecurityException("Invalid URL: $url")
        }
        if (parsed.protocol != "https") {
            throw PluginSecurityException("Only https URLs are allowed (got ${parsed.protocol})")
        }
        val host = parsed.host.lowercase()
        val allowed = domains.any { domain -> host == domain || host.endsWith(".$domain") }
        if (!allowed) {
            throw PluginSecurityException("This plugin is not allowed to contact $host")
        }
        rejectLocalNetwork(host)
        return parsed
    }

    /** A plugin may only reach public websites, never devices on this phone or the local network. */
    private fun rejectLocalNetwork(host: String) {
        val addresses = try {
            InetAddress.getAllByName(host)
        } catch (e: UnknownHostException) {
            return // The connection attempt will report the problem.
        }
        for (address in addresses) {
            val bytes = address.address
            val uniqueLocal = bytes.size == 16 && (bytes[0].toInt() and 0xFE) == 0xFC
            if (
                address.isAnyLocalAddress || address.isLoopbackAddress ||
                address.isLinkLocalAddress || address.isSiteLocalAddress || uniqueLocal
            ) {
                throw PluginSecurityException("$host points to a private network address, so it is blocked")
            }
        }
    }
}
