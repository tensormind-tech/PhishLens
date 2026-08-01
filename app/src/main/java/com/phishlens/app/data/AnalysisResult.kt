package com.phishlens.app.data

/**
 * Data class representing the result of a phishing analysis scan.
 */
data class AnalysisResult(
    val score: Int,
    val suspiciousPhrases: List<String>,
    val reasons: List<String>,
    val label: String
)
