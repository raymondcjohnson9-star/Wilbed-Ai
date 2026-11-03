package org.wilbedai.wilbedai

/**
 * Wilbed's central AI engine.
 *
 * The chat screen talks to this class instead of calling the offline
 * teaching engine directly. This keeps the interface separate from
 * the intelligence implementation and gives us one place to add a
 * secure online AI provider later.
 *
 * Current provider: offline WilbedBrain.
 * No online API or API key is configured yet.
 */
class WilbedAI(
    private val offlineBrain: WilbedBrain = WilbedBrain()
) {
    fun respond(message: String): String {
        return offlineBrain.respond(message)
    }
}
