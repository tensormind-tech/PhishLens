package com.phishlens.app.security

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.TextView
import com.phishlens.app.network.GroqRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BrowserPhishingProtectionService : AccessibilityService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val groqRepository = GroqRepository()

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    private var lastCheckedUrl: String? = null
    private var isScanning = false

    companion object {
        private const val TAG = "PhishLensUniversal"
        // Universal URL pattern matching domains and web links across all browsers
        private val URL_PATTERN = Regex("""(https?://[^\s]+|www\.[^\s]+|[a-zA-Z0-9.\-]+\.(com|org|net|io|in|co|xyz|top|work|click|info|biz|site|online|app|dev|me|cc|tk|ml|ga|cf|gq|zip|mov)(/[^\s]*)?)""", RegexOption.IGNORE_CASE)
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        Log.d(TAG, "Universal Browser Phishing Protection Service Started")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val rootNode = rootInActiveWindow ?: return
        val rawUrl = extractUrlFromWindow(rootNode)

        if (!rawUrl.isNullOrBlank()) {
            val normalizedUrl = formatUrl(rawUrl)

            if (normalizedUrl != lastCheckedUrl && !isScanning) {
                lastCheckedUrl = normalizedUrl
                isScanning = true
                Log.d(TAG, "Universal link detected in browser/webview: $normalizedUrl")

                serviceScope.launch {
                    try {
                        val result = PhishingAnalyzer.analyze(normalizedUrl)
                        Log.d(TAG, "Phishing score for $normalizedUrl: score=${result.score}, label=${result.label}")

                        if (result.score >= 60) {
                            val aiExplanation = groqRepository.getExplanation(normalizedUrl, result)

                            // 1. Send High-Priority System Notification
                            PhishingNotificationHelper.sendPhishingWarningNotification(
                                context = applicationContext,
                                url = normalizedUrl,
                                score = result.score,
                                label = result.label,
                                groqSummary = aiExplanation
                            )

                            // 2. Display Real-Time In-Browser Overlay Warning
                            withContext(Dispatchers.Main) {
                                showBrowserOverlayWarning(normalizedUrl, result.score, result.label, aiExplanation)
                            }
                        } else {
                            withContext(Dispatchers.Main) {
                                dismissOverlay()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error evaluating universal browser link: ${e.localizedMessage}", e)
                    } finally {
                        isScanning = false
                    }
                }
            }
        }
    }

    private fun showBrowserOverlayWarning(
        url: String,
        score: Int,
        label: String,
        aiExplanation: String
    ) {
        dismissOverlay()

        try {
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = 80
            }

            val container = android.widget.LinearLayout(this).apply {
                orientation = android.widget.LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#F40F172A"))
                setPadding(40, 40, 40, 40)
            }

            val titleView = TextView(this).apply {
                text = "🚨 HIGH RISK PHISHING SCAM WARNING ($label - $score/100)"
                setTextColor(Color.parseColor("#EF4444"))
                textSize = 18f
                setTypeface(null, android.graphics.Typeface.BOLD)
            }

            val urlView = TextView(this).apply {
                text = "Blocked Link: $url"
                setTextColor(Color.WHITE)
                textSize = 14f
                setPadding(0, 10, 0, 10)
            }

            val explanationView = TextView(this).apply {
                text = "🤖 Groq AI Security Breakdown:\n$aiExplanation"
                setTextColor(Color.parseColor("#E2E8F0"))
                textSize = 13f
                setPadding(0, 10, 0, 20)
            }

            val blockButton = Button(this).apply {
                text = "🛑 BLOCK & RETURN TO SAFETY"
                setBackgroundColor(Color.parseColor("#EF4444"))
                setTextColor(Color.WHITE)
                setOnClickListener {
                    performGlobalAction(GLOBAL_ACTION_BACK)
                    dismissOverlay()
                }
            }

            val dismissButton = Button(this).apply {
                text = "Dismiss Warning"
                setBackgroundColor(Color.TRANSPARENT)
                setTextColor(Color.LTGRAY)
                setOnClickListener {
                    dismissOverlay()
                }
            }

            container.addView(titleView)
            container.addView(urlView)
            container.addView(explanationView)
            container.addView(blockButton)
            container.addView(dismissButton)

            overlayView = container
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying universal window overlay: ${e.localizedMessage}", e)
        }
    }

    private fun dismissOverlay() {
        try {
            if (overlayView != null) {
                windowManager?.removeView(overlayView)
                overlayView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {
        dismissOverlay()
    }

    override fun onDestroy() {
        super.onDestroy()
        dismissOverlay()
        serviceScope.cancel()
    }

    /**
     * Universally extracts URL/Web links from any Android browser or WebView node.
     */
    private fun extractUrlFromWindow(node: AccessibilityNodeInfo?): String? {
        if (node == null) return null

        val text = node.text?.toString()?.trim() ?: ""
        val contentDesc = node.contentDescription?.toString()?.trim() ?: ""

        if (text.isNotBlank()) {
            val matchedUrl = findValidWebUrl(text)
            if (matchedUrl != null) return matchedUrl
        }

        if (contentDesc.isNotBlank()) {
            val matchedUrl = findValidWebUrl(contentDesc)
            if (matchedUrl != null) return matchedUrl
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            val result = extractUrlFromWindow(child)
            if (result != null) {
                return result
            }
        }

        return null
    }

    private fun findValidWebUrl(input: String): String? {
        val match = URL_PATTERN.find(input)
        return match?.value
    }

    private fun formatUrl(raw: String): String {
        return if (!raw.startsWith("http://") && !raw.startsWith("https://")) {
            "https://$raw"
        } else {
            raw
        }
    }
}
