package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CodeEditor
import com.example.viewmodel.IdeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IdeScreen(modifier: Modifier = Modifier, viewModel: IdeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Python IDE") },
                actions = {
                    IconButton(onClick = { viewModel.setShowApiKeyDialog(true) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "API Key Settings")
                    }
                    IconButton(onClick = { viewModel.suggestCode() }) {
                        Icon(Icons.Filled.AutoFixHigh, contentDescription = "AI Suggestion")
                    }
                    IconButton(onClick = { viewModel.runCode() }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = "Run Code")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Code Editor
            CodeEditor(
                code = uiState.code,
                onCodeChange = { viewModel.updateCode(it) },
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxWidth()
            )

            HorizontalDivider()

            // Terminal / Output
            Column(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terminal",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (uiState.isRunning || uiState.isSuggesting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = uiState.output,
                        color = if (uiState.isError) Color.Red else Color.Green,
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }

    if (uiState.showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.setShowApiKeyDialog(false) },
            title = { Text("Gemini API Key") },
            text = {
                OutlinedTextField(
                    value = uiState.apiKey,
                    onValueChange = { viewModel.updateApiKey(it) },
                    label = { Text("API Key") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.setShowApiKeyDialog(false) }) {
                    Text("Save")
                }
            }
        )
    }
}
