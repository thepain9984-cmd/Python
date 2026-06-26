package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.network.Content
import com.example.network.GenerateContentRequest
import com.example.network.NetworkClient
import com.example.network.Part
import com.example.network.PistonFile
import com.example.network.PistonRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.BuildConfig

data class IdeUiState(
    val code: String = "# Write your Python code here\nprint('Hello, Python!')\n",
    val output: String = "",
    val isRunning: Boolean = false,
    val isSuggesting: Boolean = false,
    val isError: Boolean = false,
    val apiKey: String = BuildConfig.GEMINI_API_KEY.takeIf { !it.contains("MY_GEMINI_API_KEY") } ?: "",
    val showApiKeyDialog: Boolean = false
)

class IdeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(IdeUiState())
    val uiState: StateFlow<IdeUiState> = _uiState.asStateFlow()

    fun updateCode(newCode: String) {
        _uiState.update { it.copy(code = newCode) }
    }

    fun runCode() {
        if (_uiState.value.code.isBlank()) return

        _uiState.update { it.copy(isRunning = true, output = "Running...", isError = false) }

        viewModelScope.launch {
            try {
                val request = PistonRequest(
                    files = listOf(PistonFile(content = _uiState.value.code))
                )
                val response = NetworkClient.pistonService.executeCode(request)
                
                if (response.message != null) {
                    _uiState.update { 
                        it.copy(isRunning = false, output = "API Error: ${response.message}", isError = true) 
                    }
                } else if (response.run != null) {
                    val isError = response.run.code != 0
                    val resultText = if (isError) response.run.stderr else response.run.output
                    _uiState.update { 
                        it.copy(
                            isRunning = false, 
                            output = "Exited with code ${response.run.code}\n\n$resultText",
                            isError = isError
                        ) 
                    }
                } else {
                    _uiState.update { 
                        it.copy(isRunning = false, output = "Unknown error occurred.", isError = true) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isRunning = false, output = "Network Error: ${e.message}", isError = true) 
                }
            }
        }
    }

    fun updateApiKey(newKey: String) {
        _uiState.update { it.copy(apiKey = newKey) }
    }

    fun setShowApiKeyDialog(show: Boolean) {
        _uiState.update { it.copy(showApiKeyDialog = show) }
    }

    fun suggestCode() {
        val currentCode = _uiState.value.code
        if (currentCode.isBlank()) return

        val apiKey = _uiState.value.apiKey
        if (apiKey.isBlank()) {
            _uiState.update { 
                it.copy(
                    output = "API Key not found. Please enter it in the settings.",
                    isError = true,
                    showApiKeyDialog = true
                )
            }
            return
        }

        _uiState.update { it.copy(isSuggesting = true, output = "Requesting AI suggestion...", isError = false) }

        viewModelScope.launch {
            try {
                val prompt = "You are a Python coding assistant. " +
                             "Complete or improve the following Python code. " +
                             "Only return the valid Python code without any markdown formatting like ```python. " +
                             "Code:\n$currentCode"

                val request = GenerateContentRequest(
                    contents = listOf(Content(parts = listOf(Part(text = prompt))))
                )

                val response = NetworkClient.geminiService.generateContent(apiKey, request)
                
                if (response.error != null) {
                    _uiState.update { 
                        it.copy(isSuggesting = false, output = "AI Error: ${response.error.message}", isError = true) 
                    }
                } else {
                    var suggestedCode = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
                    // Clean up markdown block if the model ignores the instruction
                    if (suggestedCode.startsWith("```python")) {
                        suggestedCode = suggestedCode.substringAfter("```python").substringBeforeLast("```").trim()
                    } else if (suggestedCode.startsWith("```")) {
                        suggestedCode = suggestedCode.substringAfter("```").substringBeforeLast("```").trim()
                    }

                    _uiState.update { 
                        it.copy(
                            isSuggesting = false, 
                            code = suggestedCode.trim(), 
                            output = "Code updated with AI suggestion.",
                            isError = false
                        ) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isSuggesting = false, output = "AI Network Error: ${e.message}", isError = true) 
                }
            }
        }
    }
}
