package org.wilbedai.wilbedai

import android.content.Context
import java.util.Locale

/**
 * Wilbed central assistant engine. Explicit memories are stored privately on this device.
 * The offline tutor remains the response engine for general questions.
 */
class WilbedAI(context: Context) {
    private val offlineBrain = WilbedBrain()
    private val memory = WilbedMemory(context.applicationContext)

    fun memories(): List<String> = memory.all()

    fun respond(message: String): String {
        val clean = message.trim()
        val lower = clean.lowercase(Locale.ROOT)
        val prefixes = listOf("remember that ", "remember this: ", "remember ", "please remember that ", "please remember ")
        val prefix = prefixes.firstOrNull { lower.startsWith(it) }

        if (prefix != null) {
            val fact = clean.substring(prefix.length).trim()
            if (fact.isBlank()) return "Tell me what you would like me to remember. For example: Remember that my favourite programming language is Kotlin."
            memory.remember(fact)
            return "I have saved this on your device: \"" + fact + "\"\n\nAsk what I remember to review your saved memories, or say Forget all memories to clear them."
        }

        if (lower in listOf("what do you remember", "what do you remember about me", "show my memories", "list my memories")) {
            val saved = memory.all()
            return if (saved.isEmpty()) {
                "I do not have any saved memories yet. Say Remember that... followed by something you want me to keep."
            } else {
                "Here are the things you asked me to remember:\n\n" + saved.mapIndexed { index, fact -> "${index + 1}. $fact" }.joinToString("\n") + "\n\nThese memories are stored privately on this device."
            }
        }

        if (lower in listOf("forget all memories", "delete all memories", "clear my memories", "forget everything you remember about me")) {
            memory.clear()
            return "All memories saved by Wilbed have been cleared from this device."
        }

        return offlineBrain.respond(clean)
    }
}
