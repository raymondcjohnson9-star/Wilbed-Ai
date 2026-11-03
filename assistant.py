from datetime import datetime
import re


class WilbedBrain:
    """A small offline command engine for Wilbed AI.

    This is the first working layer of the assistant. It handles simple
    commands locally and can later be connected to an online AI service.
    """

    def get_response(self, message):
        text = message.strip()
        lowered = text.lower()

        if not text:
            return "I am listening. What would you like me to do?"

        if re.search(r"\b(hello|hi|hey)\b", lowered):
            return "Hello. Wilbed is online and ready to help."

        if "your name" in lowered or "who are you" in lowered:
            return "I am Wilbed AI, your personal assistant."

        if "what can you do" in lowered or lowered in {"help", "commands"}:
            return (
                "I can answer basic questions offline for now. Try asking "
                "for the time, date, my name, or say hello. Voice and phone "
                "controls will be added in later stages."
            )

        if "time" in lowered:
            return "The current time is " + datetime.now().strftime("%I:%M %p") + "."

        if "date" in lowered or "today" in lowered:
            return "Today's date is " + datetime.now().strftime("%A, %d %B %Y") + "."

        if re.search(r"\b(thank you|thanks)\b", lowered):
            return "You're welcome. I am here to help."

        if re.search(r"\b(bye|goodbye)\b", lowered):
            return "Goodbye. Wilbed will be ready when you return."

        return (
            "I understand your message, but I do not have an online AI "
            "connection yet. I can currently answer simple offline commands. "
            "Try asking me for the time, date, or help."
        )


brain = WilbedBrain()


def get_response(message):
    """Return Wilbed's reply for a text message."""
    return brain.get_response(message)
