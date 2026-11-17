package org.wilbedai.wilbedai

/**
 * Small deterministic arithmetic evaluator.
 * It parses a limited arithmetic grammar and never executes user-supplied code.
 */
object AtlasMathEngine {
    fun calculate(expression: String): Double {
        require(expression.isNotBlank()) { "Enter a calculation." }
        require(expression.length <= 160) { "That calculation is too long." }
        val parser = Parser(expression)
        val result = parser.parse()
        require(result.isFinite() && kotlin.math.abs(result) <= 1.0e100) {
            "The result is outside the supported range."
        }
        return result
    }

    private class Parser(private val input: String) {
        private var position = 0

        fun parse(): Double {
            val result = expression()
            skipSpaces()
            require(position == input.length) { "I couldn't understand the calculation. Check the operators." }
            return result
        }

        private fun expression(): Double {
            var value = term()
            while (true) {
                skipSpaces()
                value = when {
                    take('+') -> value + term()
                    take('-') -> value - term()
                    else -> return value
                }
            }
        }

        private fun term(): Double {
            var value = factor()
            while (true) {
                skipSpaces()
                value = when {
                    take('*') -> value * factor()
                    take('/') -> {
                        val divisor = factor()
                        require(divisor != 0.0) { "Division by zero is not allowed." }
                        value / divisor
                    }
                    take('%') -> {
                        val divisor = factor()
                        require(divisor != 0.0) { "Division by zero is not allowed." }
                        value % divisor
                    }
                    else -> return value
                }
            }
        }

        private fun factor(): Double {
            skipSpaces()
            if (take('+')) return factor()
            if (take('-')) return -factor()
            if (take('(')) {
                val value = expression()
                skipSpaces()
                require(take(')')) { "A closing parenthesis is missing." }
                return value
            }
            return number()
        }

        private fun number(): Double {
            skipSpaces()
            val start = position
            var decimalSeen = false
            while (position < input.length) {
                val char = input[position]
                when {
                    char.isDigit() -> position++
                    char == '.' && !decimalSeen -> {
                        decimalSeen = true
                        position++
                    }
                    else -> break
                }
            }
            require(position > start && input.substring(start, position) != ".") {
                "A number is missing from the calculation."
            }
            return input.substring(start, position).toDouble()
        }

        private fun skipSpaces() {
            while (position < input.length && input[position].isWhitespace()) position++
        }

        private fun take(char: Char): Boolean {
            if (position < input.length && input[position] == char) {
                position++
                return true
            }
            return false
        }
    }
}
