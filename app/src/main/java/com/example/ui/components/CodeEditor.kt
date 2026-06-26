package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PythonKeywords = listOf(
    "False", "None", "True", "and", "as", "assert", "async",
    "await", "break", "class", "continue", "def", "del", "elif",
    "else", "except", "finally", "for", "from", "global", "if",
    "import", "in", "is", "lambda", "nonlocal", "not", "or",
    "pass", "raise", "return", "try", "while", "with", "yield"
)

val PythonBuiltins = listOf(
    "print", "len", "range", "str", "int", "float", "list", "dict", "set", "tuple", "bool"
)

class PythonSyntaxTransformation(
    private val keywordColor: Color,
    private val builtinColor: Color,
    private val stringColor: Color,
    private val commentColor: Color,
    private val defaultColor: Color
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        return TransformedText(
            highlightPython(text.text),
            OffsetMapping.Identity
        )
    }

    private fun highlightPython(text: String): AnnotatedString {
        return buildAnnotatedString {
            withStyle(SpanStyle(color = defaultColor)) {
                append(text)
            }
            
            // Highlight Strings
            val stringPattern = "\"\"\"(.*?)\"\"\"|'''(.*?)'''|\"(.*?)\"|'(.*?)'".toRegex(RegexOption.DOT_MATCHES_ALL)
            stringPattern.findAll(text).forEach { match ->
                addStyle(SpanStyle(color = stringColor), match.range.first, match.range.last + 1)
            }

            // Highlight Comments
            val commentPattern = "#(.*)".toRegex()
            commentPattern.findAll(text).forEach { match ->
                addStyle(SpanStyle(color = commentColor), match.range.first, match.range.last + 1)
            }

            // Highlight Keywords & Builtins (simplified, word boundaries)
            val words = text.split(Regex("\\b"))
            var currentIndex = 0
            for (word in words) {
                if (PythonKeywords.contains(word)) {
                    addStyle(SpanStyle(color = keywordColor), currentIndex, currentIndex + word.length)
                } else if (PythonBuiltins.contains(word)) {
                    addStyle(SpanStyle(color = builtinColor), currentIndex, currentIndex + word.length)
                }
                currentIndex += word.length
            }
        }
    }
}

@Composable
fun CodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val keywordColor = MaterialTheme.colorScheme.primary
    val builtinColor = MaterialTheme.colorScheme.secondary
    val stringColor = Color(0xFF4CAF50)
    val commentColor = Color(0xFF9E9E9E)
    val textColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        BasicTextField(
            value = code,
            onValueChange = onCodeChange,
            modifier = Modifier.fillMaxSize(),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = textColor
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            visualTransformation = PythonSyntaxTransformation(
                keywordColor = keywordColor,
                builtinColor = builtinColor,
                stringColor = stringColor,
                commentColor = commentColor,
                defaultColor = textColor
            )
        )
    }
}
