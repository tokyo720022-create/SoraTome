package io.github.tokyo720022create.soratome.plugin.runtime

import java.net.MalformedURLException
import java.net.URL

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
        return parsed
    }
}
