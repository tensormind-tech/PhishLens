package com.phishlens.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phishlens.app.network.GroqRepository
import com.phishlens.app.ocr.OcrHelper
import com.phishlens.app.security.CybersecurityTips
import com.phishlens.app.security.PhishingAnalyzer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val groqRepository: GroqRepository = GroqRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text, errorMessage = null) }
    }

    /**
     * Executes phishing scan on input text.
     */
    fun analyzeText(text: String, onSuccess: () -> Unit = {}) {
        val targetText = text.ifBlank { _uiState.value.inputText }
        if (targetText.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter or paste text to analyze.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Analyzing risk patterns...",
                    errorMessage = null
                )
            }

            // 1. Offline Phishing Analysis
            val result = PhishingAnalyzer.analyze(targetText)

            _uiState.update {
                it.copy(
                    loadingMessage = "Generating AI security breakdown...",
                    analysisResult = result
                )
            }

            // 2. Fetch AI Explanation (Groq API or local fallback)
            val explanation = groqRepository.getExplanation(targetText, result)

            // 3. Pick random cybersecurity tip
            val tip = CybersecurityTips.getRandomTip()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    inputText = targetText,
                    analysisResult = result,
                    aiExplanation = explanation,
                    securityTip = tip
                )
            }

            onSuccess()
        }
    }

    /**
     * Processes gallery screenshot using ML Kit OCR.
     */
    fun processImageUri(context: Context, uri: Uri, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingMessage = "Extracting text from image...",
                    errorMessage = null
                )
            }

            try {
                val extractedText = OcrHelper.extractTextFromUri(context, uri)
                if (extractedText.isBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "No readable text detected in selected image."
                        )
                    }
                } else {
                    _uiState.update { it.copy(inputText = extractedText) }
                    analyzeText(extractedText, onSuccess)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to extract text from image: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Processes scanned QR code URL.
     */
    fun processQrCode(qrContent: String, onSuccess: () -> Unit) {
        _uiState.update { it.copy(inputText = qrContent) }
        analyzeText(qrContent, onSuccess)
    }

    /**
     * Resets analysis state for analyzing another input.
     */
    fun resetScan() {
        _uiState.update {
            ScanUiState(inputText = "")
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
