package io.github.tokyo720022create.soratome.plugin.runtime.install

object PluginVersion {

    /** Compares dotted versions such as 1.2.10 and 1.3. Returns negative, zero or positive. */
    fun compare(a: String, b: String): Int {
        val left = parts(a)
        val right = parts(b)
        for (index in 0 until maxOf(left.size, right.size)) {
            val x = left.getOrElse(index) { 0 }
            val y = right.getOrElse(index) { 0 }
            if (x != y) return x.compareTo(y)
        }
        return 0
    }

    private fun parts(version: String): List<Int> =
        version.split('.', '-', '+').mapNotNull { piece ->
            piece.takeWhile { it.isDigit() }.toIntOrNull()
        }
}
