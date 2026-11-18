package org.wilbedai.wilbedai

import java.util.Locale

/**
 * Transparent English-first intent parser for offline ATLAS.
 * Selects only allow-listed local tools; it never executes arbitrary code or commands.
 */
object AtlasTaskPlanner {
    enum class Tool {
        CALCULATOR, MEMORY, CLOCK, CALENDAR, PROGRAMMING_TUTOR,
        HELP, CONFIRMATION, OFFLINE_GUIDANCE
    }

    data class Plan(
        val tool: Tool,
        val intent: String,
        val requiresConfirmation: Boolean = false,
        val clarificationQuestion: String? = null
    )

    fun plan(message: String): Plan {
        val text = normalise(message)
        if (text.isBlank()) {
            return Plan(Tool.OFFLINE_GUIDANCE, "empty_request",
                clarificationQuestion = "What would you like ATLAS to do?")
        }

        if (matchesAny(text, "hi", "hello", "hey", "good morning", "good afternoon", "good evening")) {
            return Plan(Tool.HELP, "greeting")
        }

        // Destructive memory operations always require a separate confirmation command.
        if (text in setOf("confirm forget all memories", "confirm clear memories", "yes clear all my saved memories")) {
            return Plan(Tool.CONFIRMATION, "confirm_memory_deletion")
        }
        if (matchesAny(text, "forget all memories", "delete all memories", "clear my memories",
                "clear all my memories", "forget everything you remember about me",
                "delete everything you remember")) {
            return Plan(Tool.CONFIRMATION, "request_memory_deletion_confirmation", true)
        }

        if (startsWithAny(text, "remember ", "remember that ", "remember this ",
                "please remember ", "please remember that ", "save this ",
                "save that ", "make a note that ", "note that ")) {
            val content = removeLeadingPhrase(text, listOf(
                "please remember that ", "please remember ", "remember that ",
                "remember this ", "remember ", "save this ", "save that ",
                "make a note that ", "note that "
            )).trim(' ', ':', '.', '!')
            return if (content.isBlank()) {
                Plan(Tool.MEMORY, "save_explicit_memory",
                    clarificationQuestion = "What information should I save?")
            } else Plan(Tool.MEMORY, "save_explicit_memory")
        }
        if (matchesAny(text, "what do you remember", "what do you remember about me",
                "show my memories", "list my memories", "read my saved notes",
                "show saved notes")) {
            return Plan(Tool.MEMORY, "list_explicit_memories")
        }

        if (isCalculationRequest(text, message)) {
            return if (AtlasRequestRouter.calculationReply(message) != null) {
                Plan(Tool.CALCULATOR, "calculate")
            } else {
                Plan(Tool.CALCULATOR, "calculate",
                    clarificationQuestion = "Please say the numbers and operation, for example: add 8 and 5.")
            }
        }

        if (matchesAny(text, "what time is it", "whats the time", "tell me the time",
                "current time", "time now", "check the time", "what is the time") ||
            Regex("\\b(time|clock)\\b").containsMatchIn(text)) {
            return Plan(Tool.CLOCK, "current_time")
        }

        if (matchesAny(text, "whats todays date", "what is todays date", "todays date",
                "what date is it", "what day is it", "tell me the date", "current date",
                "date today", "what is the date today") ||
            Regex("\\b(what day|what date|todays date|date today)\\b").containsMatchIn(text)) {
            return Plan(Tool.CALENDAR, "current_date")
        }

        if (matchesAny(text, "help", "what can you do", "show your abilities", "list your tools")) {
            return Plan(Tool.HELP, "capabilities")
        }

        if (containsAny(text, "teach me", "help me learn", "explain", "lesson",
                "programming", "write code", "debug", "python", "kotlin", "javascript")) {
            return Plan(Tool.PROGRAMMING_TUTOR, "programming_learning")
        }

        return Plan(Tool.OFFLINE_GUIDANCE, "needs_broader_language_model",
            clarificationQuestion = if (text.split(' ').size <= 3)
                "Could you explain what you want me to do?" else null)
    }

    private fun normalise(message: String): String =
        message.lowercase(Locale.US)
            .replace("’", "'")
            .replace("'", "")
            .replace(Regex("[^a-z0-9+*/().%'-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .replace(Regex("^(hey|hello|please|atlas)[, ]+"), "")

    private fun startsWithAny(text: String, vararg phrases: String): Boolean =
        phrases.any { text.startsWith(it) }

    private fun matchesAny(text: String, vararg phrases: String): Boolean =
        phrases.any { text == it || text.startsWith("$it ") }

    private fun containsAny(text: String, vararg phrases: String): Boolean =
        phrases.any { text.contains(it) }

    private fun removeLeadingPhrase(text: String, phrases: List<String>): String {
        val phrase = phrases.firstOrNull { text.startsWith(it) } ?: return text
        return text.removePrefix(phrase)
    }

    private fun isCalculationRequest(normalised: String, original: String): Boolean {
        if (AtlasRequestRouter.calculationReply(original) != null) return true
        val operationWords = listOf("calculate", "compute", "work out", "add ",
            "subtract ", "multiply ", "divide ", "plus ", "minus ", "times ",
            "multiplied by ", "divided by ", "how much is")
        val hasOperation = operationWords.any { normalised.contains(it) }
        val hasNumber = normalised.any { it.isDigit() } ||
            Regex("\\b(zero|one|two|three|four|five|six|seven|eight|nine|ten|eleven|twelve)\\b")
                .containsMatchIn(normalised)
        return hasOperation && hasNumber
    }
}
