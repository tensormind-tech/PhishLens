package com.phishlens.app.viewmodel

import com.phishlens.app.data.AnalysisResult
import com.phishlens.app.security.CybersecurityTip

/**
 * UI State container for PhishLens scanning lifecycle.
 */
data class ScanUiState(
    val inputText: String = "",
    val isLoading: Boolean = false,
    val loadingMessage: String = "",
    val analysisResult: AnalysisResult? = null,
    val aiExplanation: String? = null,
    val securityTip: CybersecurityTip? = null,
    val errorMessage: String? = null
)
