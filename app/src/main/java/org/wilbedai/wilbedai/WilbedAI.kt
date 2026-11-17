package org.wilbedai.wilbedai

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** ATLAS offline task coordinator. Requests are routed to explicit local tools. */
class WilbedAI(context: Context) {
    private val offlineBrain = WilbedBrain()
    private val memory = WilbedMemory(context.applicationContext)

    fun memories(): List<String> = memory.all()

    fun respond(message: String): String {
        val clean = message.trim()
        if (clean.isBlank()) return "ATLAS is ready. Give a voice or console command."

        val plan = AtlasTaskPlanner.plan(clean)
        return when (plan.tool) {
            AtlasTaskPlanner.Tool.CALCULATOR ->
                AtlasRequestRouter.calculationReply(clean)
                    ?: "I couldn't interpret that calculation. Try: Calculate (125 * 48) + 20."

            AtlasTaskPlanner.Tool.MEMORY -> handleMemory(clean)

            AtlasTaskPlanner.Tool.CONFIRMATION -> {
                if (plan.intent == "confirm_memory_deletion") {
                    memory.clear()
                    "Confirmed. Your locally saved ATLAS memories have been cleared."
                } else {
                    "This will permanently clear the memories you explicitly saved on this device. " +
                        "To proceed, issue the exact command: Confirm forget all memories."
                }
            }

            AtlasTaskPlanner.Tool.CLOCK ->
                "The current time is " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) + "."

            AtlasTaskPlanner.Tool.CALENDAR ->
                "Today's date is " + SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date()) + "."

            AtlasTaskPlanner.Tool.PROGRAMMING_TUTOR -> offlineBrain.respond(clean)

            AtlasTaskPlanner.Tool.HELP ->
                "ATLAS offline tools currently include arithmetic, explicit on-device memory, date and time, and structured programming lessons. " +
                    "Research, document processing, phone actions, and broader natural-language reasoning are separate modules still to be built. " +
                    "ATLAS will ask for confirmation before consequential actions."

            AtlasTaskPlanner.Tool.OFFLINE_GUIDANCE ->
                "I have received your request, but the current offline intelligence does not yet understand that task reliably. " +
                    "The next upgrade is a local language-model reasoning layer connected to approved tools. " +
                    "For now, try a calculation, a date or time request, a memory command, or a programming lesson."
        }
    }

    private fun handleMemory(message: String): String {
        val lower = message.lowercase(Locale.ROOT)
        val prefixes = listOf("please remember that ", "please remember ", "remember that ", "remember this: ", "remember ")
        val prefix = prefixes.firstOrNull { lower.startsWith(it) }

        if (prefix != null) {
            val fact = message.substring(prefix.length).trim()
            if (fact.isBlank()) return "Tell ATLAS what you want it to remember."
            val saved = memory.remember(fact)
            return if (saved) "Memory saved on this device: \"$fact\"."
            else "I couldn't save an empty memory."
        }

        val saved = memory.all()
        return if (saved.isEmpty()) {
            "There are no saved memories. Use: Remember that my preferred language is Python."
        } else {
            "SAVED MEMORIES (" + saved.size + ")\n\n" +
                saved.mapIndexed { index, fact -> (index + 1).toString() + ". " + fact }.joinToString("\n")
        }
    }
}
