package me.nickotato.simplePolls.utils

object DurationParser {
    private val tokenRegex = Regex("(\\d+)\\s*([dhm])", RegexOption.IGNORE_CASE)
    private const val SECONDS_PER_MINUTE = 60L
    private const val SECONDS_PER_HOUR = 3600L
    private const val SECONDS_PER_DAY = 86400L

    /**
     * Parses a human-friendly duration string into total seconds.
     *
     * Supported units (case-insensitive):
     * - d = days
     * - h = hours
     * - m = minutes
     *
     * The input may contain any combination of units in any order, with extra whitespace.
     * Duplicate units are allowed and are summed (e.g., "1h 2h" -> 3h).
     *
     * Backwards compatibility: a plain number (e.g., "2") is treated as hours.
     */
    fun parseDuration(input: String): Long {
        val help = "Use formats like 5m, 2h, 1d 2h 5m."
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            throw IllegalArgumentException("Duration cannot be empty. $help")
        }

        // Backwards compatibility: duration provided as plain hours.
        if (trimmed.matches(Regex("\\d+"))) {
            val hours = trimmed.toLong()
            if (hours <= 0) throw IllegalArgumentException("Duration must be greater than zero. $help")
            return hours * SECONDS_PER_HOUR
        }

        val normalized = trimmed.lowercase()
        val matches = tokenRegex.findAll(normalized).toList()
        if (matches.isEmpty()) {
            throw IllegalArgumentException("Invalid duration. $help")
        }

        // Validate that we only have valid tokens and whitespace.
        val leftover = tokenRegex.replace(normalized, "").replace("\\s+".toRegex(), "")
        if (leftover.isNotEmpty()) {
            throw IllegalArgumentException("Invalid duration. $help")
        }

        var totalSeconds = 0L
        for (match in matches) {
            val value = match.groupValues[1].toLong()
            val unit = match.groupValues[2].lowercase()

            if (value <= 0) {
                throw IllegalArgumentException("Duration values must be greater than zero. $help")
            }

            val addSeconds = when (unit) {
                "d" -> value * SECONDS_PER_DAY
                "h" -> value * SECONDS_PER_HOUR
                "m" -> value * SECONDS_PER_MINUTE
                else -> throw IllegalArgumentException("Invalid duration. $help")
            }
            totalSeconds += addSeconds
        }

        if (totalSeconds <= 0) {
            throw IllegalArgumentException("Duration must be greater than zero. $help")
        }

        return totalSeconds
    }

    fun formatDuration(totalSeconds: Long): String {
        if (totalSeconds <= 0) return "0s"

        var remaining = totalSeconds
        val days = remaining / SECONDS_PER_DAY
        remaining %= SECONDS_PER_DAY
        val hours = remaining / SECONDS_PER_HOUR
        remaining %= SECONDS_PER_HOUR
        val minutes = remaining / SECONDS_PER_MINUTE
        val seconds = remaining % SECONDS_PER_MINUTE

        val parts = mutableListOf<String>()
        if (days > 0) parts.add("${days}d")
        if (hours > 0) parts.add("${hours}h")
        if (minutes > 0) parts.add("${minutes}m")
        if (seconds > 0) parts.add("${seconds}s")

        return parts.joinToString(" ").ifEmpty { "0s" }
    }
}
