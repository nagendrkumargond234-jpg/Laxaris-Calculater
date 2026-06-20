package com.example

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val isTablet = screenWidth > 600.dp

    var showHistory by remember { mutableStateOf(false) }

    val historyList by viewModel.history.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Main Calculator Section
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Calculator Header Branding & Modes
                CalculatorHeader(
                    isDegreeMode = viewModel.isDegreeMode,
                    onModeToggle = { viewModel.onButtonPress("DEG") },
                    onHistoryToggle = { showHistory = !showHistory },
                    isSciExpanded = viewModel.isSciExpanded,
                    onSciToggle = { viewModel.toggleSciExpanded() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Calculation Display Area
                DisplayArea(
                    expression = viewModel.expression,
                    result = viewModel.result,
                    isDegreeMode = viewModel.isDegreeMode,
                    memoryValue = viewModel.memoryValue,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Memory operations row
                MemoryRow(
                    memoryValue = viewModel.memoryValue,
                    onMemoryClear = { viewModel.handleMemoryClear() },
                    onMemoryRecall = { viewModel.handleMemoryRecall() },
                    onMemoryPlus = { viewModel.handleMemoryPlus() },
                    onMemoryMinus = { viewModel.handleMemoryMinus() },
                    onMemoryStore = { viewModel.handleMemoryStore() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Functional Controls & Buttons Layout
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Quick Scientific Toggle (for phone factor)
                    if (!isTablet) {
                        QuickScientificRow(
                            isExpanded = viewModel.isSciExpanded,
                            onToggle = { viewModel.toggleSciExpanded() }
                        )

                        AnimatedVisibility(
                            visible = viewModel.isSciExpanded,
                            enter = expandVertically(animationSpec = spring()) + fadeIn(),
                            exit = shrinkVertically(animationSpec = spring()) + fadeOut()
                        ) {
                            ScientificKeypad(
                                onKeyPress = { viewModel.onButtonPress(it) },
                                isDegreeMode = viewModel.isDegreeMode
                            )
                        }
                    } else {
                        // Always display Scientific Row/Side panel on larger displays
                        Card(
                            modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "SCIENTIFIC PRESETS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                                )
                                ScientificKeypad(
                                    onKeyPress = { viewModel.onButtonPress(it) },
                                    isDegreeMode = viewModel.isDegreeMode
                                )
                            }
                        }
                    }

                    // Base Grid Buttons (C, Backspace, Ops, Numbers)
                    BaseKeypad(
                        onKeyPress = { viewModel.onButtonPress(it) }
                    )
                }
            }

            // History slide-out or side panel
            if (showHistory || isTablet) {
                Card(
                    modifier = Modifier
                        .weight(if (isTablet) 0.8f else 1f)
                        .fillMaxHeight()
                        .padding(end = 16.dp, top = 16.dp, bottom = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    HistoryDrawerContent(
                        history = historyList,
                        onSelectItem = {
                            viewModel.selectHistoryItem(it)
                            if (!isTablet) showHistory = false
                        },
                        onClearHistory = { viewModel.clearHistory() },
                        onClose = { showHistory = false },
                        isCloseable = !isTablet
                    )
                }
            }
        }
    }
}

@Composable
fun CalculatorHeader(
    isDegreeMode: Boolean,
    onModeToggle: () -> Unit,
    onHistoryToggle: () -> Unit,
    isSciExpanded: Boolean,
    onSciToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SMART CALC",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "LUXURY EDITION",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Deg/Rad switch pill
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onModeToggle() }
                    .testTag("mode_toggle"),
                color = MaterialTheme.colorScheme.secondary,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (isDegreeMode) MaterialTheme.colorScheme.primary else Color.Gray,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isDegreeMode) "DEG" else "RAD",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            // History Button Icon
            IconButton(
                onClick = onHistoryToggle,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
                    .testTag("history_button")
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Show history",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun DisplayArea(
    expression: String,
    result: String,
    isDegreeMode: Boolean,
    memoryValue: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            // Degree / Rad Indicator top left of the screen (decorative + informative)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isDegreeMode) "☉ DEGREE" else "⚛︎ RADIAN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                    )
                    if (memoryValue != 0.0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        SuggestionChip(
                            onClick = { /* No-op, just informative */ },
                            label = {
                                Text(
                                    "M = ${formatMemoryDisplay(memoryValue)}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                labelColor = MaterialTheme.colorScheme.primary
                            ),
                            border = null,
                            modifier = Modifier.height(20.dp)
                        )
                    }
                }
                if (result.isNotEmpty() && result != "Error") {
                    Text(
                        text = "Result Ready",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Expression String Display
            Text(
                text = expression.ifEmpty { "0" },
                fontSize = if (expression.length > 20) 22.sp else 28.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.End,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth().testTag("expression_display")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Evaluation Result Display
            Text(
                text = result,
                fontSize = if (result.length > 12) 36.sp else 46.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.End,
                maxLines = 1,
                color = if (result == "Error") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.fillMaxWidth().testTag("result_display")
            )
        }
    }
}

@Composable
fun QuickScientificRow(
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("science_toggle"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Science,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Scientific Functions",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun ScientificKeypad(
    onKeyPress: (String) -> Unit,
    isDegreeMode: Boolean
) {
    val keys = listOf(
        listOf("log", "ln", "√x", "x²"),
        listOf("xʸ", "π", "e", "!")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in keys) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (key in row) {
                    ScientificButton(
                        text = key,
                        onClick = { onKeyPress(key) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun BaseKeypad(
    onKeyPress: (String) -> Unit
) {
    val layout = listOf(
        listOf("C", "⌫", "(", ")"),
        listOf("7", "8", "9", "÷"),
        listOf("4", "5", "6", "×"),
        listOf("1", "2", "3", "-"),
        listOf("0", ".", "%", "+"),
        listOf("sin", "cos", "tan", "=")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (row in layout) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (key in row) {
                    CalcButton(
                        text = key,
                        onClick = { onKeyPress(key) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = when (text) {
        "=" -> MaterialTheme.colorScheme.primary
        "C", "⌫" -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        "+", "-", "×", "÷" -> MaterialTheme.colorScheme.secondary
        "sin", "cos", "tan" -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when (text) {
        "=" -> Color.White
        "C", "⌫" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onBackground
    }

    val buttonShape = RoundedCornerShape(24.dp)

    Surface(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1.2f)
            .shadow(1.dp, buttonShape)
            .testTag("btn_$text"),
        shape = buttonShape,
        color = containerColor,
        contentColor = contentColor,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontSize = if (text.length > 2) 16.sp else 21.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScientificButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(16.dp)

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(42.dp)
            .shadow(1.dp, buttonShape)
            .testTag("btn_$text"),
        shape = buttonShape,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
        contentColor = MaterialTheme.colorScheme.onBackground,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

@Composable
fun HistoryDrawerContent(
    history: List<HistoryItem>,
    onSelectItem: (HistoryItem) -> Unit,
    onClearHistory: () -> Unit,
    onClose: () -> Unit,
    isCloseable: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Calculation History",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row {
                if (history.isNotEmpty()) {
                    IconButton(
                        onClick = onClearHistory,
                        modifier = Modifier.testTag("clear_history_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear history",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (isCloseable) {
                    TextButton(onClick = onClose) {
                        Text("Close", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No history items yet",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillWithSafePadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectItem(item) }
                            .testTag("history_item_${item.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = item.expression,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.result,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryRow(
    memoryValue: Double,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryPlus: () -> Unit,
    onMemoryMinus: () -> Unit,
    onMemoryStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (memoryValue != 0.0) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "M",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (memoryValue != 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemoryButton(text = "MC", onClick = onMemoryClear)
                MemoryButton(text = "MR", onClick = onMemoryRecall)
                MemoryButton(text = "M+", onClick = onMemoryPlus)
                MemoryButton(text = "M-", onClick = onMemoryMinus)
                MemoryButton(text = "MS", onClick = onMemoryStore)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(12.dp)
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(52.dp)
            .height(34.dp)
            .testTag("btn_mem_$text"),
        shape = buttonShape,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f),
        contentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        }
    }
}

private fun formatMemoryDisplay(value: Double): String {
    val df = java.text.DecimalFormat("#.####")
    return df.format(value)
}

// Simple extension functions to bypass layout api issues safely
private fun Modifier.fillWithSafePadding() = this.fillMaxWidth().fillMaxHeight()
