package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.DecimalFormat

data class HistoryItem(
    val id: Long,
    val expression: String,
    val result: String
)

class CalculatorViewModel : ViewModel() {

    var expression by mutableStateOf("")
        private set

    var result by mutableStateOf("")
        private set

    var isDegreeMode by mutableStateOf(true)
        private set

    var isSciExpanded by mutableStateOf(false)
        private set

    private val _history = MutableStateFlow<List<HistoryItem>>(emptyList())
    val history: StateFlow<List<HistoryItem>> = _history.asStateFlow()

    private var nextHistoryId = 1L

    var memoryValue by mutableStateOf(0.0)
        private set

    fun handleMemoryClear() {
        memoryValue = 0.0
    }

    fun handleMemoryRecall() {
        val formattedMem = formatResult(memoryValue)
        if (expression.isEmpty()) {
            expression = formattedMem
        } else {
            val lastChar = expression.trim().lastOrNull() ?: ' '
            if (lastChar.isDigit() || lastChar == '.' || lastChar == 'π' || lastChar == 'e' || lastChar == ')') {
                expression += "+$formattedMem"
            } else {
                expression += formattedMem
            }
        }
    }

    fun handleMemoryPlus() {
        val valueToUse = getValToUseForMemory()
        if (valueToUse != null) {
            memoryValue += valueToUse
        }
    }

    fun handleMemoryMinus() {
        val valueToUse = getValToUseForMemory()
        if (valueToUse != null) {
            memoryValue -= valueToUse
        }
    }

    fun handleMemoryStore() {
        val valueToUse = getValToUseForMemory()
        if (valueToUse != null) {
            memoryValue = valueToUse
        }
    }

    private fun getValToUseForMemory(): Double? {
        if (result.isNotEmpty() && result != "Error" && result != "Overflow") {
            return result.toDoubleOrNull()
        }
        if (expression.isNotEmpty()) {
            return try {
                val engine = CalculatorEngine(isDegreeMode)
                engine.evaluate(expression)
            } catch (e: Exception) {
                null
            }
        }
        return null
    }

    fun onButtonPress(btn: String) {
        when (btn) {
            "C" -> {
                expression = ""
                result = ""
            }
            "⌫" -> {
                if (expression.isNotEmpty()) {
                    // Try to delete full multi-character function names at once if applicable
                    val prefixToDelete = getFunctionPrefixToDelete(expression)
                    expression = if (prefixToDelete != null) {
                        expression.substring(0, expression.length - prefixToDelete.length)
                    } else {
                        expression.substring(0, expression.length - 1)
                    }
                }
            }
            "DEG", "RAD" -> {
                isDegreeMode = !isDegreeMode
            }
            "=" -> {
                evaluateExpression()
            }
            "sin", "cos", "tan", "log", "ln" -> {
                expression += "$btn("
            }
            "√x" -> {
                expression += "√("
            }
            "x²" -> {
                expression += "^2"
            }
            "xʸ" -> {
                expression += "^"
            }
            "!" -> {
                expression += "!"
            }
            else -> {
                // For direct inputs (digits, standard operators, constants π, e, %, brackets, dot)
                expression += btn
            }
        }
    }

    private fun getFunctionPrefixToDelete(expr: String): String? {
        val funcs = listOf("sin(", "cos(", "tan(", "log(", "ln(", "sqrt(")
        for (f in funcs) {
            if (expr.endsWith(f)) {
                return f
            }
        }
        return null
    }

    fun selectHistoryItem(item: HistoryItem) {
        expression = item.expression
        result = item.result
    }

    fun clearHistory() {
        _history.value = emptyList()
    }

    fun toggleSciExpanded() {
        isSciExpanded = !isSciExpanded
    }

    private fun evaluateExpression() {
        if (expression.isEmpty()) {
            result = ""
            return
        }

        try {
            val engine = CalculatorEngine(isDegreeMode)
            val evaluated = engine.evaluate(expression)
            val formatted = formatResult(evaluated)
            result = formatted

            // Add to history
            val newItem = HistoryItem(
                id = nextHistoryId++,
                expression = expression,
                result = formatted
            )
            _history.value = listOf(newItem) + _history.value
        } catch (e: Exception) {
            result = "Error"
        }
    }

    private fun formatResult(value: Double): String {
        if (value.isNaN()) return "Error"
        if (value.isInfinite()) return "Overflow"

        // Handle very small numbers as 0
        if (kotlin.math.abs(value) < 1e-12) return "0"

        return try {
            val df = DecimalFormat("#.##########")
            val formatted = df.format(value)
            if (formatted == "-0") "0" else formatted
        } catch (e: Exception) {
            value.toString()
        }
    }
}
