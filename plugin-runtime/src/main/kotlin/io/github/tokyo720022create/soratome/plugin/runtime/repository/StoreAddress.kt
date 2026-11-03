package io.github.tokyo720022create.soratome.plugin.runtime.repository

import java.net.MalformedURLException
import java.net.URL

/**
 * Turns the address a user pastes into the address of the real file. Page addresses on GitHub
 * (a file, a folder or a whole repository) are mapped to the raw files they hold.
 */
object StoreAddress {

    private val githubHosts = setOf("github.com", "www.github.com")

    /** A GitHub page for a file becomes its raw address. Any other address is returned unchanged. */
    fun toRaw(input: String): String {
        val text = input.trim()
        val url = parse(text) ?: return text
        if (url.host !in githubHosts) return text
        val parts = segments(url)
        if (parts.size >= 5 && parts[2] == "blob") {
            val path = parts.drop(4).joinToString("/")
            return "https://raw.githubusercontent.com/${parts[0]}/${parts[1]}/${parts[3]}/$path"
        }
        return text
    }

    /**
     * The addresses to try, in order, when adding a store. A GitHub repository or folder page
     * maps to the places an `index.json` is usually kept.
     */
    fun candidates(input: String): List<String> {
        val text = input.trim()
        val raw = toRaw(text)
        if (raw != text) return listOf(raw)

        val url = parse(text) ?: return listOf(text)
        if (url.host !in githubHosts) return listOf(text)
        val parts = segments(url)
        if (parts.size < 2) return listOf(text)

        val owner = parts[0]
        val repo = parts[1].removeSuffix(".git")
        val onFolder = parts.size >= 4 && parts[2] == "tree"
        val branches = if (onFolder) listOf(parts[3]) else listOf("main", "master")
        val folder = if (onFolder) parts.drop(4).joinToString("/") else ""
        val folders = if (folder.isNotEmpty()) listOf(folder) else listOf("", "plugin-repo")

        val result = ArrayList<String>()
        for (branch in branches) {
            for (directory in folders) {
                val prefix = if (directory.isEmpty()) "" else "$directory/"
                result.add("https://raw.githubusercontent.com/$owner/$repo/$branch/${prefix}index.json")
            }
        }
        return result
    }

    private fun parse(text: String): URL? =
        try {
            URL(text)
        } catch (e: MalformedURLException) {
            null
        }

    private fun segments(url: URL): List<String> = url.path.split('/').filter { it.isNotEmpty() }
}
