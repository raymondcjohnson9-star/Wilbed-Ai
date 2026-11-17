package org.wilbedai.wilbedai

import java.util.Locale

/** Routes only clearly arithmetic requests to the deterministic calculator. */
object AtlasRequestRouter {
    private val prefixes = listOf("calculate ", "compute ", "work out ", "what is ", "what's ")

    fun calculationReply(message: String): String? {
        val clean = message.trim()
        val lower = clean.lowercase(Locale.ROOT)
        val prefix = prefixes.firstOrNull { lower.startsWith(it) }
        val expression = if (prefix != null) clean.substring(prefix.length).trim().trimEnd('?', '.', '=') else clean
        val looksArithmetic = expression.isNotBlank() &&
            expression.any { it.isDigit() } &&
            expression.all { it.isDigit() || it.isWhitespace() || it in "+-*/().%" }
        if (!looksArithmetic) return null

        return try {
            val result = AtlasMathEngine.calculate(expression)
            val displayed = if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
            "Result: $displayed\n\nCalculation: $expression\nMethod: evaluated using ATLAS's local arithmetic engine."
        } catch (error: IllegalArgumentException) {
            "I couldn't complete that calculation: ${error.message ?: "Please check the expression."}"
        }
    }
}
