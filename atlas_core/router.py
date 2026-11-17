"""First local request router for ATLAS."""
import re
from .math_engine import CalculationError, calculate, format_result

_PREFIX = re.compile(r"^(?:calculate|compute|work\\s+out|what\\s+is|what's)\\s+", re.IGNORECASE)
_ALLOWED = re.compile(r"^[0-9\\s+\\-*/().%]+$")

def classify_request(message):
    text = (message or "").strip()
    low = text.lower()
    if not text:
        return "empty"
    if _PREFIX.match(text) or (len(text) < 100 and _ALLOWED.fullmatch(text)):
        return "calculation"
    if any(word in low for word in ("teach me", "lesson", "learn python", "learn programming")):
        return "programming_learning"
    if any(word in low for word in ("remind me", "make a plan", "organise", "organize")):
        return "planning_request"
    return "general"

def route_message(message):
    intent = classify_request(message)
    if intent == "empty":
        return "I'm ready. What would you like me to help with?"
    if intent == "calculation":
        expression = _PREFIX.sub("", message.strip(), count=1).rstrip(" ?=.")
        if not _ALLOWED.fullmatch(expression):
            return "I can calculate arithmetic expressions. Please write the numbers and operators clearly."
        try:
            result = calculate(expression)
            return f"Result: {format_result(result)}\\n\\nCalculation: {expression}\\nMethod: evaluated using ATLAS's arithmetic tool."
        except CalculationError as exc:
            return f"I couldn't complete that calculation: {exc}"
    try:
        from assistant import get_response as legacy_get_response
        return legacy_get_response(message)
    except Exception:
        return "That capability is not connected yet. The ATLAS foundation is being built step by step."

def get_response(message):
    return route_message(message)
