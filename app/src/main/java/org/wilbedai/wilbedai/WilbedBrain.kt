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

    fun respond(message: String): String {
        val text = message.trim()
        val lower = text.lowercase(Locale.ROOT)
        if (text.isBlank()) return "I am listening. What would you like me to do?"
        val wantsCourse = listOf("teach", "learn", "course", "lesson", "study", "start").any { it in lower }
        val chosen = when {
            "javascript" in lower || Regex("\\bjs\\b").containsMatchIn(lower) -> "javascript"
            Regex("c\\s*\\+\\s*\\+|\\bcpp\\b").containsMatchIn(lower) -> "c++"
            else -> courses.keys.firstOrNull { it in lower }
        }
        if (chosen != null && wantsCourse) {
            language = chosen
            lessonIndex = 0
            return "Great! Let's learn ${chosen.uppercase(Locale.ROOT)} step by step.\n\n${lessonReply()}"
        }
        if (lower in listOf("teach me programming", "learn programming", "programming tutor", "teach me to code", "learn to code", "start coding")) {
            language = null
            return "I can teach several programming languages, one step at a time.\n\n${courseMenu()}"
        }
        if (lower in listOf("change language", "choose language", "other language", "programming languages")) {
            language = null
            return courseMenu()
        }
        if (lower in listOf("next lesson", "next", "continue lesson", "continue")) {
            val selected = language ?: return "Choose a language first.\n\n${courseMenu()}"
            val total = courses[selected]?.size ?: 0
            if (lessonIndex < total - 1) { lessonIndex++; return lessonReply() }
            return "You completed this beginner ${selected.uppercase(Locale.ROOT)} course. Choose another language or ask to practise a topic."
        }
        if (listOf("repeat lesson", "explain again", "show lesson again").any { it in lower }) return lessonReply()
        if (lower == "help" || "what can you do" in lower) return "I can chat offline, tell you the time and date, and teach Python, Kotlin, Java, JavaScript, C++, HTML, and CSS. Type 'teach me programming' to choose."
        if (Regex("\\b(hello|hi|hey)\\b").containsMatchIn(lower)) return "Hello! I am Wilbed. Type 'teach me programming' to choose a language to learn."
        if ("your name" in lower || "who are you" in lower) return "I am Wilbed AI, your personal assistant and multi-language programming tutor."
        if ("time" in lower) return "The current time is ${SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())}."
        if ("date" in lower || "today" in lower) return "Today's date is ${SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())}."
        if ("thank you" in lower || "thanks" in lower) return "You're welcome. Keep practising, one step at a time!"
        if ("bye" in lower || "goodbye" in lower) return "Goodbye. Your lessons will be here when you return."
        return "I am Wilbed, running in offline mode. I can help with greetings, time, date, and beginner programming lessons. Type 'help' to see what I can do."
    }
}
