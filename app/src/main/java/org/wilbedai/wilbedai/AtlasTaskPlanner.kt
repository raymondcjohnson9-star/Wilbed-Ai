package org.wilbedai.wilbedai

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Small, transparent task planner for offline ATLAS.
 * It chooses only local, allow-listed tools. It does not execute arbitrary commands.
 */
object AtlasTaskPlanner {
    enum class Tool { CALCULATOR, MEMORY, CLOCK, CALENDAR, PROGRAMMING_TUTOR, HELP, CONFIRMATION, OFFLINE_GUIDANCE }

    data class Plan(
        val tool: Tool,
        val intent: String,
        val requiresConfirmation: Boolean = false
    )

    fun plan(message: String): Plan {
        val text = message.trim()
        val lower = text.lowercase(Locale.ROOT)

        if (lower in setOf("confirm forget all memories", "confirm clear memories")) {
            return Plan(Tool.CONFIRMATION, "confirm_memory_deletion")
        }
        if (lower in setOf("forget all memories", "delete all memories", "clear my memories", "forget everything you remember about me")) {
            return Plan(Tool.CONFIRMATION, "request_memory_deletion_confirmation", true)
        }
        if (lower.startsWith("remember ") || lower.startsWith("remember that ") ||
            lower.startsWith("remember this: ") || lower.startsWith("please remember ")) {
            return Plan(Tool.MEMORY, "save_explicit_memory")
        }
        if (lower in setOf("what do you remember", "what do you remember about me", "show my memories", "list my memories")) {
            return Plan(Tool.MEMORY, "list_explicit_memories")
        }
        if (AtlasRequestRouter.calculationReply(text) != null) return Plan(Tool.CALCULATOR, "calculate")
        if (Regex("\\b(time|clock)\\b").containsMatchIn(lower)) return Plan(Tool.CLOCK, "current_time")
        if (Regex("\\b(date|today|what day)\\b").containsMatchIn(lower)) return Plan(Tool.CALENDAR, "current_date")
        if (listOf("teach", "learn", "lesson", "programming", "code", "python", "kotlin", "javascript").any { it in lower }) {
            return Plan(Tool.PROGRAMMING_TUTOR, "programming_learning")
        }
        if (lower == "help" || "what can you do" in lower) return Plan(Tool.HELP, "capabilities")
        return Plan(Tool.OFFLINE_GUIDANCE, "needs_broader_language_model")
    }
}
