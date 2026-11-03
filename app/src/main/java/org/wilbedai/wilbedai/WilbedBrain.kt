package org.wilbedai.wilbedai

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class Lesson(val title: String, val explanation: String, val example: String, val task: String)

class WilbedBrain {
    private val courses = mapOf(
        "python" to listOf(
            Lesson("First program", "print() displays text.", "print(\"Hello, world!\")", "Print your name."),
            Lesson("Variables", "Variables store values. Python infers the type.", "name = \"Asta\"\nscore = 10\nprint(name, score)", "Create a variable for your favourite game and print it."),
            Lesson("If and else", "if checks a condition; else handles the other case.", "score = 75\nif score >= 50:\n    print(\"Passed\")\nelse:\n    print(\"Try again\")", "Check whether a score is at least 50."),
            Lesson("For loops", "A for loop repeats code.", "for number in range(1, 6):\n    print(number)", "Print numbers from 1 to 10 with a loop."),
            Lesson("Functions", "def creates a reusable function.", "def greet(name):\n    print(\"Hello\", name)\n\ngreet(\"Asta\")", "Create a welcome function that accepts a name.")
        ),
        "kotlin" to listOf(
            Lesson("First program", "fun main() is where a Kotlin program starts. println() displays text.", "fun main() {\n    println(\"Hello, world!\")\n}", "Print your name using println()."),
            Lesson("Variables", "val creates a read-only variable; var can be reassigned.", "val game = \"Minecraft\"\nvar score = 10\nscore = 20\nprintln(game)\nprintln(score)", "Create a val for your favourite game and a var for a score."),
            Lesson("If and else", "if checks a condition; else handles the other case.", "val score = 75\nif (score >= 50) {\n    println(\"Passed\")\n} else {\n    println(\"Try again\")\n}", "Check whether a score is at least 50."),
            Lesson("For loops", "A for loop repeats instructions. 1..5 includes both ends.", "for (number in 1..5) {\n    println(number)\n}", "Print numbers from 1 to 10 with a loop."),
            Lesson("Functions", "fun defines reusable code; parameters pass information in.", "fun greet(name: String) {\n    println(\"Hello, \$name\")\n}\ngreet(\"Asta\")", "Create a welcome function that accepts a String name.")
        ),
        "java" to listOf(
            Lesson("First program", "Java code lives in a class. main() is the entry point.", "public class Main {\n  public static void main(String[] args) {\n    System.out.println(\"Hello, world!\");\n  }\n}", "Change the message so it prints your name."),
            Lesson("Variables", "String stores text and int stores whole numbers.", "String game = \"Minecraft\";\nint score = 10;\nSystem.out.println(game + \" \" + score);", "Create a String for your game and an int for your score.")
        ),
        "javascript" to listOf(
            Lesson("First program", "console.log() writes a message to the console.", "console.log(\"Hello, world!\");", "Print your name using console.log()."),
            Lesson("Variables", "Use const for values you do not reassign and let for values that can change.", "const game = \"Minecraft\";\nlet score = 10;\nscore = 20;\nconsole.log(game, score);", "Create a const for your game and a let for your score.")
        ),
        "c++" to listOf(
            Lesson("First program", "iostream provides console input and output. main() starts the program.", "#include <iostream>\nusing namespace std;\nint main() {\n  cout << \"Hello, world!\" << endl;\n  return 0;\n}", "Change the message so it prints your name."),
            Lesson("Variables", "string stores text and int stores whole numbers.", "string game = \"Minecraft\";\nint score = 10;\ncout << game << \" \" << score;", "Create a string for your game and an int for your score.")
        ),
        "html" to listOf(
            Lesson("First web page", "HTML describes the structure and content of a web page.", "<!doctype html>\n<html>\n<body>\n  <h1>Hello, world!</h1>\n  <p>My first page.</p>\n</body>\n</html>", "Change the heading to your name and add a paragraph."),
            Lesson("Headings and paragraphs", "Use h1 to h6 for headings and p for paragraphs.", "<h1>My website</h1>\n<h2>About me</h2>\n<p>I am learning web development.</p>", "Create a heading and two paragraphs.")
        ),
        "css" to listOf(
            Lesson("First CSS rule", "CSS controls how HTML elements look.", "h1 {\n  color: purple;\n  font-size: 32px;\n}", "Write a CSS rule that makes paragraphs blue."),
            Lesson("Selectors", "A class selector starts with a dot; an ID selector starts with #.", ".card {\n  background-color: lightblue;\n  padding: 16px;\n}", "Create a .profile class with a background colour and padding.")
        )
    )
    private var language: String? = null
    private var lessonIndex = 0

    private fun courseMenu() = "Choose a language to learn:\n\nPython • Kotlin • Java • JavaScript • C++ • HTML • CSS\n\nType 'teach me Kotlin' or another language to begin."

    private fun lessonReply(): String {
        val selected = language ?: return courseMenu()
        val lessons = courses[selected] ?: return courseMenu()
        val lesson = lessons[lessonIndex]
        return "${selected.uppercase(Locale.ROOT)} LESSON ${lessonIndex + 1} OF ${lessons.size}: ${lesson.title}\n\n" +
            "EXPLANATION\n${lesson.explanation}\n\nEXAMPLE\n${lesson.example}\n\n" +
            "YOUR PRACTICE TASK\n${lesson.task}\n\nTry the task and send me your code. Type 'next lesson' to continue or 'change language' to choose another course."
    }

    private fun explainTopic(topic: String): String {
        val selected = language ?: return "Which language should I use for the example? You can also ask me to explain it in general."
        val lowerTopic = topic.lowercase(Locale.ROOT)
        val explanation = when {
            "variable" in lowerTopic -> "A variable is a named place where a program stores a value. You can use its name to read the value later."
            "string" in lowerTopic || "text" in lowerTopic -> "A string is text stored by a program. It is usually written inside quotation marks."
            "loop" in lowerTopic -> "A loop repeats instructions. It saves you from writing the same instructions many times."
            "function" in lowerTopic || "method" in lowerTopic -> "A function is a reusable block of code. It can receive information called parameters and may return a result."
            "condition" in lowerTopic || "if statement" in lowerTopic -> "A condition helps a program make a decision. An if block runs when its condition is true; else can handle another case."
            "list" in lowerTopic || "array" in lowerTopic -> "A list or array keeps several values together. You can access an item by its position."
            "boolean" in lowerTopic || "bool" in lowerTopic -> "A Boolean value is either true or false. It is often used in conditions."
            "class" in lowerTopic || "object" in lowerTopic -> "A class is a blueprint for creating objects. Objects can hold data and provide functions that work with that data."
            "input" in lowerTopic || "output" in lowerTopic || "print" in lowerTopic -> "Input is information a program receives. Output is information it displays or sends back."
            else -> return "Tell me which part of " + topic + " you want explained, or paste a small example and I'll break it down."
        }
        val example = when (selected) {
            "python" -> when {
                "variable" in lowerTopic -> "player = \"Alex\"\nscore = 10"
                "string" in lowerTopic || "text" in lowerTopic -> "message = \"Hello, Alex!\"\nprint(message)"
                "loop" in lowerTopic -> "for number in range(1, 4):\n    print(number)"
                "function" in lowerTopic || "method" in lowerTopic -> "def greet(name):\n    return \"Hello, \" + name"
                "condition" in lowerTopic || "if statement" in lowerTopic -> "score = 70\nif score >= 50:\n    print(\"Passed\")\nelse:\n    print(\"Try again\")"
                "list" in lowerTopic || "array" in lowerTopic -> "games = [\"Chess\", \"Football\"]\nprint(games[0])"
                else -> "is_ready = True\nprint(is_ready)"
            }
            "kotlin" -> when {
                "variable" in lowerTopic -> "val player = \"Alex\"\nvar score = 10"
                "string" in lowerTopic || "text" in lowerTopic -> "val message = \"Hello, Alex!\"\nprintln(message)"
                "loop" in lowerTopic -> "for (number in 1..3) {\n    println(number)\n}"
                "function" in lowerTopic || "method" in lowerTopic -> "fun greet(name: String): String {\n    return \"Hello, \$name\"\n}"
                "condition" in lowerTopic || "if statement" in lowerTopic -> "val score = 70\nif (score >= 50) {\n    println(\"Passed\")\n} else {\n    println(\"Try again\")\n}"
                "list" in lowerTopic || "array" in lowerTopic -> "val games = listOf(\"Chess\", \"Football\")\nprintln(games[0])"
                else -> "val isReady = true\nprintln(isReady)"
            }
            "java" -> "String player = \"Alex\";\nint score = 10;\nSystem.out.println(player + \" \" + score);"
            "javascript" -> "const player = \"Alex\";\nlet score = 10;\nconsole.log(player, score);"
            "c++" -> "#include <iostream>\nusing namespace std;\nint main() {\n    string player = \"Alex\";\n    int score = 10;\n    cout << player << \" \" << score;\n    return 0;\n}"
            "html" -> "<h1>Hello, Alex!</h1>\n<p>I am learning HTML.</p>"
            "css" -> "p {\n    color: blue;\n    font-size: 18px;\n}"
            else -> "score = 10"
        }
        return selected.uppercase(Locale.ROOT) + ": " + topic + "\n\n" + explanation + "\n\nEXAMPLE\n" + example + "\n\nTry changing one value and tell me what you think will happen."
    }

    private fun practiceReply(): String {
        val selected = language ?: return "Tell me which language you are practising, for example: 'Give me a Python exercise'."
        val task = courses[selected]?.getOrNull(lessonIndex)?.task
            ?: "Write a small program that stores a value and displays it."
        return "Let's practise " + selected.uppercase(Locale.ROOT) + "!\n\nCHALLENGE\n" + task +
            "\n\nTry it yourself first. Send me your code and I will review it, explain likely mistakes, and guide you with hints."
    }

    fun respond(message: String): String {
        val text = message.trim()
        val lower = text.lowercase(Locale.ROOT)
        if (text.isBlank()) return "I'm ready. What would you like to explore?"

        val chosen = when {
            "javascript" in lower || Regex("\\bjs\\b").containsMatchIn(lower) -> "javascript"
            Regex("c\\s*\\+\\s*\\+|\\bcpp\\b").containsMatchIn(lower) -> "c++"
            else -> courses.keys.firstOrNull { Regex("\\b" + Regex.escape(it) + "\\b").containsMatchIn(lower) }
        }
        val learningIntent = listOf("teach", "learn", "course", "lesson", "study", "start learning", "want to learn", "beginner", "from scratch", "from the beginning")
            .any { it in lower }

        if (chosen != null && (learningIntent || lower.startsWith("start ") || lower.startsWith("i want"))) {
            language = chosen
            lessonIndex = 0
            return "Great! I'll teach you " + chosen.uppercase(Locale.ROOT) +
                " from the basics, one step at a time. Each lesson has an explanation, an example, and a task.\n\n" + lessonReply()
        }

        if (lower in listOf("teach me programming", "learn programming", "programming tutor", "teach me to code", "learn to code", "start coding")) {
            language = null
            return "I can teach Python, Kotlin, Java, JavaScript, C++, HTML, CSS, and more. Just say: 'Teach me Python from scratch'."
        }

        if (lower in listOf("change language", "choose language", "other language", "switch language")) {
            language = null
            return "Which language would you like to learn next? Just ask me to teach it."
        }

        if (lower in listOf("next lesson", "next", "continue lesson", "continue")) {
            val selected = language ?: return "Which language would you like to learn? For example: 'Teach me Kotlin'."
            val total = courses[selected]?.size ?: 0
            if (lessonIndex < total - 1) {
                lessonIndex++
                return lessonReply()
            }
            return "You completed this beginner " + selected.uppercase(Locale.ROOT) +
                " course. Ask me for practice, a deeper explanation, or another language."
        }

        if (listOf("repeat lesson", "explain again", "show lesson again", "i don't understand", "i dont understand").any { it in lower }) {
            return if (language != null) "Let's go through it again.\n\n" + lessonReply()
            else "That's okay. Tell me the topic or paste the part you don't understand."
        }

        if (listOf("give me an exercise", "give me a task", "practice", "practise", "quiz me", "assignment").any { it in lower }) return practiceReply()

        if (listOf("check my code", "review my code", "debug this", "fix my code", "what is wrong with my code").any { it in lower }) {
            return "Paste your code and tell me what you expected it to do. I'll inspect it, point out likely mistakes, explain why they happen, and suggest a correction. This offline version can review code but cannot run or compile it."
        }

        val topicWords = listOf("variable", "string", "text", "loop", "function", "method", "condition", "if statement", "list", "array", "boolean", "bool", "class", "object", "input", "output", "print")
        val topic = topicWords.firstOrNull { it in lower }
        if (topic != null && listOf("explain", "what is", "how does", "teach me", "help me understand", "tell me about").any { it in lower }) {
            return explainTopic(topic)
        }

        if (lower == "help" || "what can you do" in lower) {
            return "You can talk to me naturally. I can teach programming languages step by step, explain coding topics with examples, give exercises, review code you paste, and continue or repeat lessons.\n\nTry: 'Teach me Kotlin', 'Explain functions', or 'Give me a Python exercise'."
        }
        if (Regex("\\b(hello|hi|hey|good morning|good afternoon|good evening)\\b").containsMatchIn(lower)) {
            return "Hello! What would you like to work on today? Ask a question or tell me which programming language you want to learn."
        }
        if ("your name" in lower || "who are you" in lower) return "I'm Wilbed AI, your personal assistant and programming tutor."
        if ("time" in lower) return "The current time is " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()) + "."
        if ("date" in lower || "today" in lower) return "Today's date is " + SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date()) + "."
        if ("thank you" in lower || "thanks" in lower) return "You're welcome! Keep experimenting and asking questions."
        if ("bye" in lower || "goodbye" in lower) return "Goodbye! You can continue learning whenever you return."

        return if (language != null) {
            "Could you add a little more detail or paste an example? We're currently learning " +
                language!!.uppercase(Locale.ROOT) + ", so I can relate the explanation to it."
        } else {
            "I don't have enough information to give a reliable answer while working offline. Try asking me to teach a language, explain a coding topic, or review code. An online AI connection can be added in a later upgrade for broader, more flexible conversations."
        }
    }
}
