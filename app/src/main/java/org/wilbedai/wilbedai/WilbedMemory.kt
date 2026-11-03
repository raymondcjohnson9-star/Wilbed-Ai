package org.wilbedai.wilbedai

import android.content.Context

/** Stores facts the user explicitly asks Wilbed to remember. */
class WilbedMemory(context: Context) {
    private val preferences = context.getSharedPreferences("wilbed_memory", Context.MODE_PRIVATE)

    fun remember(fact: String): Boolean {
        val cleanFact = fact.trim().take(240)
        if (cleanFact.isBlank()) return false
        val saved = preferences.getStringSet(KEY_FACTS, emptySet())?.toMutableSet() ?: mutableSetOf()
        saved.add(cleanFact)
        preferences.edit().putStringSet(KEY_FACTS, saved).apply()
        return true
    }

    fun all(): List<String> = preferences.getStringSet(KEY_FACTS, emptySet())?.toList()?.sorted() ?: emptyList()

    fun clear() { preferences.edit().remove(KEY_FACTS).apply() }

    companion object { private const val KEY_FACTS = "explicit_facts" }
}
