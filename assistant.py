from datetime import datetime
import re


class WilbedBrain:
    """Offline assistant with beginner courses in several programming languages."""

    def __init__(self):
        self.language = None
        self.lesson_index = 0
        self.courses = {
            "python": [
                ("First program", "print() displays text.", 'print("Hello, world!")', "Print your name."),
                ("Variables", "Variables store values. Python works out the type from the value.", 'name = "Asta"\nscore = 10\nprint(name, score)', "Create a variable for your favourite game and print it."),
                ("If and else", "if checks a condition; else handles the other case.", 'score = 75\nif score >= 50:\n    print("Passed")\nelse:\n    print("Try again")', "Check whether a score is at least 50."),
                ("For loops", "A for loop repeats code. range(1, 6) gives 1 to 5.", 'for number in range(1, 6):\n    print(number)', "Print numbers from 1 to 10 with a loop."),
                ("Functions", "def creates a reusable function.", 'def greet(name):\n    print("Hello", name)\n\ngreet("Asta")', "Create a welcome function that accepts a name.")
            ],
            "kotlin": [
                ("First program", "fun main() is where a Kotlin program starts. println() displays text.", 'fun main() {\n    println("Hello, world!")\n}', "Print your name using println()."),
                ("Variables", "val creates a read-only variable; var can be reassigned.", 'fun main() {\n    val game = "Minecraft"\n    var score = 10\n    score = 20\n    println(game)\n    println(score)\n}', "Create a val for your favourite game and a var for a score."),
                ("If and else", "if checks a condition; else handles the other case.", 'fun main() {\n    val score = 75\n    if (score >= 50) {\n        println("Passed")\n    } else {\n        println("Try again")\n    }\n}', "Check whether a score is at least 50."),
                ("For loops", "A for loop repeats instructions. 1..5 includes both ends.", 'fun main() {\n    for (number in 1..5) {\n        println(number)\n    }\n}', "Print numbers from 1 to 10 with a loop."),
                ("Functions", "fun defines reusable code; parameters pass information in.", 'fun greet(name: String) {\n    println("Hello, $name")\n}\nfun main() {\n    greet("Asta")\n}', "Create a welcome function that accepts a String name.")
            ],
            "java": [
                ("First program", "Java code lives in a class. main() is the entry point.", 'public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, world!");\n    }\n}', "Change the message so it prints your name."),
                ("Variables", "String stores text and int stores whole numbers.", 'String game = "Minecraft";\nint score = 10;\nSystem.out.println(game + " " + score);', "Create a String for your game and an int for your score."),
                ("If and else", "if checks a condition; else handles the alternative.", 'int score = 75;\nif (score >= 50) {\n    System.out.println("Passed");\n} else {\n    System.out.println("Try again");\n}', "Check whether a score is at least 50."),
                ("For loops", "A for loop repeats code while its condition is true.", 'for (int n = 1; n <= 5; n++) {\n    System.out.println(n);\n}', "Print numbers from 1 to 10 with a for loop."),
                ("Methods", "A method is a reusable named block of code.", 'static void greet(String name) {\n    System.out.println("Hello " + name);\n}', "Write a method called welcome that accepts a String.")
            ],
            "javascript": [
                ("First program", "console.log() writes a message to the console.", 'console.log("Hello, world!");', "Print your name using console.log()."),
                ("Variables", "Use const for values you do not reassign and let for values that can change.", 'const game = "Minecraft";\nlet score = 10;\nscore = 20;\nconsole.log(game, score);', "Create a const for your game and a let for your score."),
                ("If and else", "if checks a condition; else handles the other case.", 'const score = 75;\nif (score >= 50) {\n  console.log("Passed");\n} else {\n  console.log("Try again");\n}', "Check whether a score is at least 50."),
                ("For loops", "A for loop repeats code while its condition is true.", 'for (let n = 1; n <= 5; n++) {\n  console.log(n);\n}', "Print numbers from 1 to 10 with a for loop."),
                ("Functions", "A function stores reusable instructions.", 'function greet(name) {\n  console.log("Hello " + name);\n}\ngreet("Asta");', "Create a welcome function that accepts a name.")
            ],
            "c++": [
                ("First program", "iostream provides console input and output. main() starts the program.", '#include <iostream>\nusing namespace std;\nint main() {\n    cout << "Hello, world!" << endl;\n    return 0;\n}', "Change the message so it prints your name."),
                ("Variables", "string stores text and int stores whole numbers.", '#include <iostream>\n#include <string>\nusing namespace std;\nint main() {\n    string game = "Minecraft";\n    int score = 10;\n    cout << game << " " << score << endl;\n    return 0;\n}', "Create a string for your game and an int for your score."),
                ("If and else", "if checks a condition; else handles the alternative.", 'int score = 75;\nif (score >= 50) {\n    cout << "Passed";\n} else {\n    cout << "Try again";\n}', "Check whether a score is at least 50."),
                ("For loops", "A for loop repeats code and updates its counter.", 'for (int n = 1; n <= 5; n++) {\n    cout << n << endl;\n}', "Print numbers from 1 to 10 with a for loop."),
                ("Functions", "A function is a reusable block of code.", 'void greet(string name) {\n    cout << "Hello " << name << endl;\n}', "Create a function called welcome that accepts a string.")
            ],
            "html": [
                ("First web page", "HTML describes the structure and content of a web page.", '<!doctype html>\n<html>\n<head><title>My page</title></head>\n<body>\n  <h1>Hello, world!</h1>\n  <p>My first page.</p>\n</body>\n</html>', "Change the heading to your name and add a paragraph."),
                ("Headings and paragraphs", "Use h1 to h6 for headings and p for paragraphs.", '<h1>My website</h1>\n<h2>About me</h2>\n<p>I am learning web development.</p>', "Create a heading and two paragraphs."),
                ("Links and images", "a creates a link. img displays an image and should have alt text.", '<a href="https://example.com">Visit a website</a>\n<img src="photo.jpg" alt="A description of the photo">', "Write a link and an image element with useful alt text.")
            ],
            "css": [
                ("First CSS rule", "CSS controls how HTML elements look. A rule selects an element and sets properties.", 'h1 {\n  color: purple;\n  font-size: 32px;\n}', "Write a CSS rule that makes paragraphs blue."),
                ("Selectors", "A class selector starts with a dot; an ID selector starts with #.", '.card {\n  background-color: lightblue;\n  padding: 16px;\n}\n#title {\n  text-align: center;\n}', "Create a .profile class with a background colour and padding."),
                ("Spacing", "margin adds space outside an element; padding adds space inside it.", '.box {\n  margin: 20px;\n  padding: 12px;\n  border: 1px solid black;\n}', "Create a .box rule with margin, padding, and a border.")
            ]
        }

    def course_menu(self):
        return ("Choose a language to learn:\n\n"
                "• Python\n• Kotlin\n• Java\n• JavaScript\n• C++\n• HTML\n• CSS\n\n"
                "Type, for example, 'teach me Python'. You can switch at any time.")

    def lesson_reply(self):
        lessons = self.courses[self.language]
        title, explanation, code, task = lessons[self.lesson_index]
        return (f"{self.language.upper()} LESSON {self.lesson_index + 1} OF {len(lessons)}: {title}\n\n"
                f"EXPLANATION\n{explanation}\n\nEXAMPLE\n{code}\n\n"
                f"YOUR PRACTICE TASK\n{task}\n\n"
                "Try the task and send me your code. Type 'next lesson' to continue, "
                "or 'change language' to choose another course.")

    def get_response(self, message):
        text = message.strip()
        lowered = text.lower()
        if not text:
            return "I am listening. What would you like me to do?"

        patterns = [
            (r"\bpython\b", "python"), (r"\bkotlin\b", "kotlin"),
            (r"\bjava\b", "java"), (r"\bjavascript\b|\bjs\b", "javascript"),
            (r"\bc\s*\+\s*\+|\bcpp\b", "c++"),
            (r"\bhtml\b", "html"), (r"\bcss\b", "css")
        ]
        wants_course = any(w in lowered for w in ("teach", "learn", "course", "lesson", "study", "start"))
        for pattern, language in patterns:
            if re.search(pattern, lowered) and wants_course:
                self.language = language
                self.lesson_index = 0
                return f"Great! Let's learn {language.upper()} step by step.\n\n" + self.lesson_reply()

        if lowered in {"change language", "choose language", "other language", "programming languages"}:
            self.language = None
            return self.course_menu()

        if any(p in lowered for p in ("teach me programming", "learn programming", "programming tutor", "teach me to code", "learn to code", "start coding")):
            self.language = None
            return "I can teach several languages, one step at a time.\n\n" + self.course_menu()

        if lowered in {"next lesson", "next", "continue lesson", "continue"}:
            if not self.language:
                return "Choose a language first.\n\n" + self.course_menu()
            if self.lesson_index < len(self.courses[self.language]) - 1:
                self.lesson_index += 1
                return self.lesson_reply()
            return f"You completed this beginner {self.language.upper()} course. Choose another language or ask to practise a topic."

        if any(p in lowered for p in ("repeat lesson", "explain again", "show lesson again")):
            return self.lesson_reply() if self.language else self.course_menu()

        if any(p in lowered for p in ("check my code", "check my answer", "here is my code", "review my code", "is this correct")):
            return "Paste your code and tell me which language it uses. I can explain it and help look for common mistakes. Include any error message too."

        if "what can you do" in lowered or lowered in {"help", "commands"}:
            return "I can chat offline, tell you the time and date, and teach beginner Python, Kotlin, Java, JavaScript, C++, HTML, and CSS. Type 'teach me programming' to choose."

        if re.search(r"\b(hello|hi|hey)\b", lowered):
            return "Hello! I am Wilbed. Type 'teach me programming' to choose a language to learn."
        if "your name" in lowered or "who are you" in lowered:
            return "I am Wilbed AI, your personal assistant and multi-language programming tutor."
        if "time" in lowered:
            return "The current time is " + datetime.now().strftime("%I:%M %p") + "."
        if "date" in lowered or "today" in lowered:
            return "Today's date is " + datetime.now().strftime("%A, %d %B %Y") + "."
        if re.search(r"\b(thank you|thanks)\b", lowered):
            return "You're welcome. Keep practising, one step at a time!"
        if re.search(r"\b(bye|goodbye)\b", lowered):
            return "Goodbye. Your lessons will be here when you return."
        return "I am an offline assistant, so I cannot answer every question yet. Type 'teach me programming' to choose a language, or 'help' to see my features."


brain = WilbedBrain()


def get_response(message):
    """Return Wilbed's reply for a text message."""
    return brain.get_response(message)
