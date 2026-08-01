package com.phishlens.app.security

import com.phishlens.app.data.AnalysisResult

/**
 * PhishingAnalyzer provides offline phishing detection using weighted keywords
 * and comprehensive URL heuristics.
 */
object PhishingAnalyzer {

    // Trusted legitimate domains that should never be flagged as phishing
    private val TRUSTED_DOMAINS = setOf(
        "google.com", "google.co.in", "youtube.com", "wikipedia.org", "github.com",
        "stackoverflow.com", "amazon.com", "facebook.com", "instagram.com", "microsoft.com",
        "apple.com", "linkedin.com", "reddit.com", "twitter.com", "x.com", "android.com"
    )

    // Weighted keywords with their risk contributions
    private val KEYWORD_WEIGHTS = mapOf(
        "verify" to 15,
        "urgent" to 20,
        "suspended" to 25,
        "click here" to 15,
        "update account" to 20,
        "password" to 20,
        "otp" to 15,
        "gift card" to 30,
        "bit.ly" to 25,
        "tinyurl" to 25,
        "action required" to 20,
        "immediately" to 15,
        "security alert" to 15,
        "locked" to 20,
        "claim refund" to 25,
        "winner" to 25,
        "lottery" to 30
    )

    // Suspicious Top Level Domains frequently abused in phishing
    private val SUSPICIOUS_TLDS = setOf(
        ".xyz", ".top", ".work", ".click", ".tk", ".ml", ".ga", ".cf", ".gq",
        ".zip", ".mov", ".buzz", ".space", ".fit", ".rest", ".online", ".site"
    )

    // Common URL shorteners
    private val SHORTENED_DOMAINS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "goo.gl", "is.gd", "buff.ly", "ow.ly", "tiny.cc"
    )

    // Regex to detect IPv4 address in URL domain (e.g. http://192.168.1.1/login)
    private val IP_URL_REGEX = Regex("""https?://\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}(:\d+)?(/|\b)""")

    // Regex to match URLs in general text
    private val URL_REGEX = Regex("""(https?://[^\s]+|www\.[^\s]+|[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,6}/[^\s]*)""")

    /**
     * Analyzes the given input text for phishing indicators.
     */
    fun analyze(text: String): AnalysisResult {
        if (text.isBlank()) {
            return AnalysisResult(
                score = 0,
                suspiciousPhrases = emptyList(),
                reasons = listOf("No text provided for analysis."),
                label = "Safe"
            )
        }

        val lowercaseText = text.lowercase()

        // 0. Trusted Domain Exemption Check
        for (domain in TRUSTED_DOMAINS) {
            if (lowercaseText.contains(domain)) {
                return AnalysisResult(
                    score = 0,
                    suspiciousPhrases = emptyList(),
                    reasons = listOf("Verified trusted domain: $domain"),
                    label = "Safe"
                )
            }
        }

        var totalScore = 0
        val detectedPhrases = mutableSetOf<String>()
        val detectedReasons = mutableListOf<String>()

        // 1. Keyword Weighted Analysis
        for ((keyword, weight) in KEYWORD_WEIGHTS) {
            if (lowercaseText.contains(keyword)) {
                totalScore += weight
                detectedPhrases.add(keyword)
                detectedReasons.add("Contains high-risk trigger word/phrase: '$keyword' (+$weight pts)")
            }
        }

        // 2. URL Extraction & Heuristic Analysis
        val extractedUrls = URL_REGEX.findAll(text).map { it.value }.toList()

        for (rawUrl in extractedUrls) {
            val lowerUrl = rawUrl.lowercase()

            // Check URL shortener
            for (shortener in SHORTENED_DOMAINS) {
                if (lowerUrl.contains(shortener)) {
                    if (!detectedPhrases.contains(shortener)) {
                        totalScore += 25
                        detectedPhrases.add(shortener)
                        detectedReasons.add("Uses URL shortener '$shortener' to hide actual destination (+25 pts)")
                    }
                }
            }

            // Check IP address format
            if (IP_URL_REGEX.containsMatchIn(rawUrl)) {
                totalScore += 30
                detectedPhrases.add(rawUrl)
                detectedReasons.add("Uses IP address instead of domain name in URL (+30 pts)")
            }

            // Check @ symbol in URL (userinfo credential obfuscation)
            if (rawUrl.contains("@")) {
                totalScore += 35
                detectedPhrases.add(rawUrl)
                detectedReasons.add("Contains '@' symbol in URL (potential domain redirection trick) (+35 pts)")
            }

            // Check suspicious TLDs
            for (tld in SUSPICIOUS_TLDS) {
                if (lowerUrl.contains(tld)) {
                    totalScore += 25
                    detectedPhrases.add(tld)
                    detectedReasons.add("Uses high-risk suspicious top-level domain '$tld' (+25 pts)")
                    break
                }
            }

            // Check excessive digits in URL domain/path (e.g. bank1892374.com)
            val digitCount = rawUrl.count { it.isDigit() }
            if (digitCount >= 7) {
                totalScore += 20
                detectedPhrases.add(rawUrl)
                detectedReasons.add("URL contains excessive digits ($digitCount digits, common in scam domains) (+20 pts)")
            }
        }

        // 3. Additional Phishing Heuristics
        // Excessive capitalization / urgent punctuation
        val uppercaseLetters = text.count { it.isUpperCase() }
        val totalLetters = text.count { it.isLetter() }
        if (totalLetters > 10 && (uppercaseLetters.toDouble() / totalLetters) > 0.4) {
            totalScore += 15
            detectedReasons.add("Excessive uppercase lettering used to manufacture panic (+15 pts)")
        }

        val exclamationCount = text.count { it == '!' }
        if (exclamationCount >= 3) {
            totalScore += 10
            detectedReasons.add("Multiple exclamation marks used to create artificial urgency (+10 pts)")
        }

        // Normalize score between 0 and 100
        val finalScore = totalScore.coerceIn(0, 100)

        // Assign risk label
        val label = when {
            finalScore < 30 -> "Safe"
            finalScore <= 65 -> "Suspicious"
            else -> "High Risk"
        }

        if (detectedReasons.isEmpty()) {
            detectedReasons.add("No obvious phishing indicators or suspicious patterns detected.")
        }

        return AnalysisResult(
            score = finalScore,
            suspiciousPhrases = detectedPhrases.toList(),
            reasons = detectedReasons,
            label = label
        )
    }
}
