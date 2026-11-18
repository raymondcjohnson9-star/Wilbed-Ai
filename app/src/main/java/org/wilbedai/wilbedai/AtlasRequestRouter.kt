package org.wilbedai.wilbedai

import java.util.Locale

/** Routes clear arithmetic requests to a deterministic, non-code-executing calculator. */
object AtlasRequestRouter {
    private val prefixes = listOf(
        "could you calculate ", "can you calculate ", "please calculate ",
        "could you compute ", "can you compute ", "please compute ",
        "can you work out ", "please work out ", "how much is ",
        "calculate ", "compute ", "work out ", "what is ", "what's "
    )

    private val numberWords = mapOf(
        "zero" to "0", "one" to "1", "two" to "2", "three" to "3",
        "four" to "4", "five" to "5", "six" to "6", "seven" to "7",
        "eight" to "8", "nine" to "9", "ten" to "10", "eleven" to "11",
        "twelve" to "12", "thirteen" to "13", "fourteen" to "14",
        "fifteen" to "15", "sixteen" to "16", "seventeen" to "17",
        "eighteen" to "18", "nineteen" to "19", "twenty" to "20"
    )

    fun calculationReply(message: String): String? {
        val clean = message.trim()
        val lower = clean.lowercase(Locale.ROOT)
        val prefix = prefixes.firstOrNull { lower.startsWith(it) }
        var expression = if (prefix != null)
            clean.substring(prefix.length).trim().trimEnd('?', '.', '=') else clean
        expression = naturalLanguageExpression(expression) ?: return null

        val looksArithmetic = expression.isNotBlank() &&
            expression.any { it.isDigit() } &&
            expression.all { it.isDigit() || it.isWhitespace() || it in "+-*/().%" }
        if (!looksArithmetic) return null

        return try {
            val result = AtlasMathEngine.calculate(expression)
            val displayed = if (result % 1.0 == 0.0) result.toLong().toString() else result.toString()
            "Result: $displayed\\n\\nCalculation: $expression\\nMethod: evaluated using ATLAS's local arithmetic engine."
        } catch (error: IllegalArgumentException) {
            "I couldn't complete that calculation: ${error.message ?: "Please check the expression."}"
        }
    }

    /**
     * Converts a deliberately small set of spoken arithmetic patterns into arithmetic syntax.
     * Unsupported wording returns null rather than guessing.
     */
    private fun naturalLanguageExpression(input: String): String? {
        var text = input.lowercase(Locale.ROOT).trim().trimEnd('?', '.', '!')
        val add = Regex("^add (.+) and (.+)$").matchEntire(text)
        val subtract = Regex("^subtract (.+) from (.+)$").matchEntire(text)
        val multiply = Regex("^(?:multiply|times) (.+) by (.+)$").matchEntire(text)
        val divide = Regex("^divide (.+) by (.+)$").matchEntire(text)

        text = when {
            add != null -> "${add.groupValues[1]} + ${add.groupValues[2]}"
            subtract != null -> "${subtract.groupValues[2]} - ${subtract.groupValues[1]}"
            multiply != null -> "${multiply.groupValues[1]} * ${multiply.groupValues[2]}"
            divide != null -> "${divide.groupValues[1]} / ${divide.groupValues[2]}"
            else -> text
        }

        text = text
            .replace(Regex("\\bplus\\b"), "+")
            .replace(Regex("\\bminus\\b"), "-")
            .replace(Regex("\\b(?:times|multiplied by)\\b"), "*")
            .replace(Regex("\\bdivided by\\b"), "/")

        for ((word, digit) in numberWords) {
            text = text.replace(Regex("\\b${word}\\b"), digit)
        }
        return text.takeIf { it.any(Char::isDigit) }
    }
}
