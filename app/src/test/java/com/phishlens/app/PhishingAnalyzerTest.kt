package com.phishlens.app

import com.phishlens.app.security.PhishingAnalyzer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhishingAnalyzerTest {

    @Test
    fun analyze_sampleScamMessage_flaggedAsHighRisk() {
        val scamMessage = "Your SBI account is suspended. Verify immediately at bit.ly/sbi-verify"
        val result = PhishingAnalyzer.analyze(scamMessage)

        assertEquals("High Risk", result.label)
        assertTrue(result.score >= 65)
        assertTrue(result.suspiciousPhrases.contains("suspended"))
        assertTrue(result.suspiciousPhrases.contains("verify"))
        assertTrue(result.suspiciousPhrases.contains("bit.ly"))
    }

    @Test
    fun analyze_sampleSafeMessage_flaggedAsSafe() {
        val safeMessage = "Your order #12345 has been shipped and will arrive tomorrow."
        val result = PhishingAnalyzer.analyze(safeMessage)

        assertEquals("Safe", result.label)
        assertTrue(result.score < 30)
        assertTrue(result.suspiciousPhrases.isEmpty())
    }

    @Test
    fun analyze_ipAddressUrl_addsHeuristicWeight() {
        val ipUrlMessage = "Log in now at http://192.168.1.100/bank/login to keep access."
        val result = PhishingAnalyzer.analyze(ipUrlMessage)

        assertTrue(result.score >= 45)
        assertTrue(result.reasons.any { it.contains("IP address") })
    }

    @Test
    fun analyze_atSymbolInUrl_detectsObfuscationTrick() {
        val atSymbolMessage = "Check updates at http://mybank.com@scammer-domain.net/login"
        val result = PhishingAnalyzer.analyze(atSymbolMessage)

        assertTrue(result.suspiciousPhrases.any { it.contains("@") })
        assertTrue(result.reasons.any { it.contains("'@' symbol") })
    }

    @Test
    fun analyze_suspiciousTld_detected() {
        val tldMessage = "Claim your prize at http://secure-login-update.xyz/gift"
        val result = PhishingAnalyzer.analyze(tldMessage)

        assertTrue(result.suspiciousPhrases.contains(".xyz"))
        assertTrue(result.reasons.any { it.contains(".xyz") })
    }

    @Test
    fun analyze_scoreIsCappedAt100() {
        val extremeScam = "URGENT URGENT!! Your account is suspended! Verify password, otp, update account immediately at bit.ly/1234567890 with gift card at http://192.168.1.1@evil.xyz"
        val result = PhishingAnalyzer.analyze(extremeScam)

        assertEquals(100, result.score)
        assertEquals("High Risk", result.label)
    }

    @Test
    fun analyze_blankInput_returnsZeroScore() {
        val result = PhishingAnalyzer.analyze("   ")
        assertEquals(0, result.score)
        assertEquals("Safe", result.label)
    }
}
