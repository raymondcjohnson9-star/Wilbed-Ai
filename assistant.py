from datetime import datetime
import re


class WilbedBrain:
    """Offline assistant brain with a beginner-friendly Kotlin tutor."""

    def __init__(self):
        self.lesson_index = 0
        self.lessons = [
            {
                "title": "Your first Kotlin program",
                "explain": (
                    "A Kotlin program starts running inside fun main(). "
                    "The println() command displays text on the screen."
                ),
                "code": 'fun main() {\n    println("Hello, world!")\n}',
                "task": "Write a program that displays your name using println()."
            },
            {
                "title": "Variables: val and var",
                "explain": (
                    "A variable stores information. Use val when the value "
                    "should not change, and var when you need to change it."
                ),
                "code": 'fun main() {\n    val name = "Asta"\n    var score = 10\n    score = 20\n    println(name)\n    println(score)\n}',
                "task": "Create a val called game containing your favourite game, and a var called score containing 0. Print both."
            },
            {
                "title": "Kotlin data types",
                "explain": (
                    "String stores text, Int stores whole numbers, Double "
                    "stores decimal numbers, and Boolean stores true or false."
                ),
                "code": 'fun main() {\n    val name: String = "Asta"\n    val age: Int = 15\n    val price: Double = 2.5\n    val isLearning: Boolean = true\n    println(name)\n}',
                "task": "Create one String, one Int, and one Boolean variable. Give each a value and print them."
            },
            {
                "title": "Making decisions with if and else",
                "explain": (
                    "An if statement runs code when a condition is true. "
                    "Else runs when the condition is false."
                ),
                "code": 'fun main() {\n    val score = 75\n    if (score >= 50) {\n        println("You passed")\n    } else {\n        println("Try again")\n    }\n}',
                "task": "Create an age variable. Use if and else to print whether the age is 18 or above."
            },
            {
                "title": "Repeating work with a for loop",
                "explain": (
                    "A for loop repeats instructions. The range 1..5 includes "
                    "the numbers 1, 2, 3, 4, and 5."
                ),
                "code": 'fun main() {\n    for (number in 1..5) {\n        println(number)\n    }\n}',
                "task": "Write a for loop that prints the numbers from 1 to 10."
            },
            {
                "title": "Functions",
                "explain": (
                    "A function is a reusable block of code. You define it "
                    "with fun, give it a name, and call it when needed."
                ),
                "code": 'fun greet(name: String) {\n    println("Hello, $name")\n}\n\nfun main() {\n    greet("Asta")\n}',
                "task": "Create a function called welcome that accepts a name (String) and prints a welcome message. Call the function."
            }
        ]

    def lesson_reply(self):
        lesson = self.lessons[self.lesson_index]
        number = self.lesson_index + 1
        return (
            f"KOTLIN LESSON {number} OF {len(self.lessons)}: {lesson['title']}\n\n"
            f"WHAT IT MEANS\n{lesson['explain']}\n\n"
            f"EXAMPLE\n{lesson['code']}\n\n"
            f"YOUR WORK\n{lesson['task']}\n\n"
            "Try the task and send me your code. I will help you review it. "
            "Type 'next lesson' when you are ready to continue."
        )

    def get_response(self, message):
        text = message.strip()
        lowered = text.lower()

        if not text:
            return "I am listening. What would you like me to do?"

        if any(phrase in lowered for phrase in (
            "teach me programming", "learn programming", "programming tutor",
            "teach me to code", "learn to code", "kotlin lesson",
            "teach me kotlin", "learn kotlin", "start coding"
        )):
            self.lesson_index = 0
            return (
                "Absolutely! I can teach you Kotlin step by step. "
                "I will explain one topic, show an example, and then give "
                "you a task to practise.\n\n" + self.lesson_reply()
            )

        if lowered in {"next lesson", "next", "continue lesson", "continue"}:
            if self.lesson_index < len(self.lessons) - 1:
                self.lesson_index += 1
                return self.lesson_reply()
            return (
                "You have reached the end of this beginner Kotlin course! "
                "You can ask me to repeat a lesson or practise a topic."
            )

        if any(phrase in lowered for phrase in (
            "repeat lesson", "explain again", "show lesson again"
        )):
            return self.lesson_reply()

        if any(phrase in lowered for phrase in (
            "check my code", "check my answer", "here is my code",
            "review my code", "is this correct"
        )):
            return (
                "Send your Kotlin code in your next message, and I will help "
                "you review its structure and explain any mistakes. "
                "If you get an error, include the exact error message too."
            )

        if "what can you do" in lowered or lowered in {"help", "commands"}:
            return (
                "I can chat offline, tell you the time and date, and teach "
                "beginner Kotlin programming. Try 'teach me programming', "
                "'next lesson', or 'repeat lesson'."
            )

        if re.search(r"\b(hello|hi|hey)\b", lowered):
            return "Hello! I am Wilbed. Ask me to teach you programming to start a Kotlin lesson."

        if "your name" in lowered or "who are you" in lowered:
            return "I am Wilbed AI, your personal assistant and beginner Kotlin programming tutor."

        if "time" in lowered:
            return "The current time is " + datetime.now().strftime("%I:%M %p") + "."

        if "date" in lowered or "today" in lowered:
            return "Today's date is " + datetime.now().strftime("%A, %d %B %Y") + "."

        if re.search(r"\b(thank you|thanks)\b", lowered):
            return "You're welcome. Keep practising, one small step at a time!"

        if re.search(r"\b(bye|goodbye)\b", lowered):
            return "Goodbye. Your Kotlin lessons will be here when you return."

        return (
            "I am an offline assistant, so I cannot answer every question yet. "
            "I can teach beginner Kotlin: type 'teach me programming' to start, "
            "or 'help' to see what I can do."
        )


brain = WilbedBrain()


def get_response(message):
    """Return Wilbed's reply for a text message."""
    return brain.get_response(message)
