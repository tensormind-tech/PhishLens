package com.phishlens.app.security

import kotlin.random.Random

/**
 * Repository of bite-sized cybersecurity concepts and practical tips.
 */
object CybersecurityTips {

    private val TIPS = listOf(
        CybersecurityTip(
            title = "Never Click Shortened Links",
            description = "Scammers use URL shorteners like bit.ly to mask deceptive destinations. Always expand short links using an inspection tool or avoid clicking them directly.",
            category = "Link Safety"
        ),
        CybersecurityTip(
            title = "Verify Sender Domains",
            description = "Check the full email address or phone number, not just the display name. Legitimate organizations write from official domain names, not generic webmail accounts.",
            category = "Email Verification"
        ),
        CybersecurityTip(
            title = "Beware of Manufactured Urgency",
            description = "Phishing scams rely heavily on fear and artificial urgency ('Account suspended in 24 hours'). Take a breath and independently call the institution directly.",
            category = "Social Engineering"
        ),
        CybersecurityTip(
            title = "Enable Two-Factor Authentication (2FA)",
            description = "Always use App-based 2FA or hardware security keys on critical bank and email accounts so credentials stolen by phishing cannot grant instant access.",
            category = "Account Security"
        ),
        CybersecurityTip(
            title = "Inspect QR Code Targets",
            description = "Before opening a URL from a scanned QR code (Quishing), review the domain name carefully for misspellings or suspicious TLD extensions.",
            category = "QR Code Safety"
        ),
        CybersecurityTip(
            title = "Never Share OTPs or Passwords",
            description = "No legitimate bank or customer service representative will ever contact you asking for your One-Time Password (OTP) or account password.",
            category = "Credential Protection"
        ),
        CybersecurityTip(
            title = "Watch Out for Gift Card Requests",
            description = "If an urgent message asks you to pay a debt, fine, or fee using gift cards or crypto transfers, it is 100% a fraudulent scam.",
            category = "Financial Protection"
        )
    )

    fun getRandomTip(): CybersecurityTip {
        val index = Random.nextInt(TIPS.size)
        return TIPS[index]
    }
}

data class CybersecurityTip(
    val title: String,
    val description: String,
    val category: String
)
