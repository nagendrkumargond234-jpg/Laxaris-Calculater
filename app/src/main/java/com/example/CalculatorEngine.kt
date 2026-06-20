package com.example

import kotlin.math.*

class CalculatorEngine(private val isDegreeMode: Boolean = true) {

    fun evaluate(expression: String): Double {
        val sanitized = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("π", PI.toString())
            .replace("e", E.toString())
            .replace(" ", "")

        if (sanitized.isEmpty()) return 0.0
        return Parser(sanitized, isDegreeMode).parse()
    }

    private class Parser(val input: String, val isDegreeMode: Boolean) {
        var pos = -1
        var ch = ' '

        fun nextChar() {
            pos++
            ch = if (pos < input.length) input[pos] else '\u0000'
        }

        fun eat(charToEat: Char): Boolean {
            while (ch == ' ') nextChar()
            if (ch == charToEat) {
                nextChar()
                return true
            }
            return false
        }

        fun parse(): Double {
            nextChar()
            val x = parseExpression()
            if (pos < input.length) {
                throw IllegalArgumentException("Unexpected character: '$ch'")
            }
            return x
        }

        // Grammar:
        // expression = term | expression `+` term | expression `-` term
        // term = factor | term `*` factor | term `/` factor
        // factor = `+` factor | `-` factor | `(` expression `)` | number | functionName factor | factor `^` factor

        fun parseExpression(): Double {
            var x = parseTerm()
            while (true) {
                if (eat('+')) x += parseTerm() // addition
                else if (eat('-')) x -= parseTerm() // subtraction
                else return x
            }
        }

        fun parseTerm(): Double {
            var x = parseFactor()
            while (true) {
                if (eat('*')) x *= parseFactor() // multiplication
                else if (eat('/')) {
                    val divisor = parseFactor()
                    if (divisor == 0.0) throw ArithmeticException("Division by zero")
                    x /= divisor // division
                }
                else return x
            }
        }

        fun parseFactor(): Double {
            if (eat('+')) return parseFactor() // unary plus
            if (eat('-')) return -parseFactor() // unary minus

            var x: Double
            val startPos = this.pos
            if (eat('(')) { // parentheses
                x = parseExpression()
                if (!eat(')')) throw IllegalArgumentException("Missing closing parenthesis")
                x = parsePostfix(x)
            } else if ((ch in '0'..'9') || ch == '.') { // numbers
                while ((ch in '0'..'9') || ch == '.') nextChar()
                x = input.substring(startPos, this.pos).toDouble()
                x = parsePostfix(x)
            } else if (ch in 'a'..'z' || ch == '√') { // functions
                var func = ""
                if (ch == '√') {
                    func = "sqrt"
                    nextChar()
                } else {
                    while (ch in 'a'..'z') {
                        func += ch
                        nextChar()
                    }
                }
                
                // Parse standard function arguments (which can be implicitly parsed factor)
                val arg = parseFactor()
                x = when (func) {
                    "sin" -> {
                        val valInRad = if (isDegreeMode) Math.toRadians(arg) else arg
                        sin(valInRad)
                    }
                    "cos" -> {
                        val valInRad = if (isDegreeMode) Math.toRadians(arg) else arg
                        cos(valInRad)
                    }
                    "tan" -> {
                        val valInRad = if (isDegreeMode) Math.toRadians(arg) else arg
                        tan(valInRad)
                    }
                    "sqrt" -> {
                        if (arg < 0) throw IllegalArgumentException("Square root of negative number")
                        sqrt(arg)
                    }
                    "log" -> log10(arg)
                    "ln" -> ln(arg)
                    else -> throw IllegalArgumentException("Unknown function: $func")
                }
                x = parsePostfix(x)
            } else {
                throw IllegalArgumentException("Unexpected character: '$ch'")
            }

            // Power logic: ^
            if (eat('^')) {
                val exponent = parseFactor()
                x = x.pow(exponent)
            }

            return x
        }

        private fun parsePostfix(base: Double): Double {
            var res = base
            while (true) {
                if (eat('%')) {
                    res /= 100.0
                } else if (eat('!')) {
                    res = factorial(res)
                } else {
                    break
                }
            }
            return res
        }

        private fun factorial(n: Double): Double {
            if (n < 0.0) throw IllegalArgumentException("Factorial of negative")
            val intVal = n.roundToInt()
            if (abs(n - intVal) > 1e-9) {
                throw IllegalArgumentException("Factorial supports integers only")
            }
            if (intVal > 170) throw IllegalArgumentException("Factorial overflow")
            var result = 1.0
            for (i in 2..intVal) {
                result *= i
            }
            return result
        }
    }
}
