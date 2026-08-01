package com.phishlens.app.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.phishlens.app.BuildConfig
import com.phishlens.app.data.AnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class GroqRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val groqApi: GroqApi by lazy {
        Retrofit.Builder()
            .baseUrl(GroqApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GroqApi::class.java)
    }

    /**
     * Obtains an AI-generated explanation for the given text and analysis result.
     * Automatically falls back to a rule-based explanation if the API key is missing or the call fails.
     */
    suspend fun getExplanation(text: String, analysisResult: AnalysisResult): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GROQ_API_KEY

        if (apiKey.isBlank() || apiKey == "your_key_here" || apiKey.contains("your_key")) {
            return@withContext buildFallbackExplanation(analysisResult)
        }

        try {
            val prompt = """
                You are a friendly cybersecurity expert assistant.
                Analyze this suspicious message or URL: "$text"
                
                Risk Score: ${analysisResult.score}/100 (${analysisResult.label})
                Detected Issues: ${analysisResult.reasons.joinToString("; ")}
                
                Explain in simple English why this message may be a phishing scam. Mention suspicious phrases and give one practical safety tip.
                Keep your answer concise (2 to 3 short paragraphs).
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = "llama-3.1-8b-instant",
                messages = listOf(
                    ChatMessage(role = "system", content = "You are a concise, helpful cybersecurity assistant."),
                    ChatMessage(role = "user", content = prompt)
                ),
                temperature = 0.3,
                maxTokens = 300
            )

            val response = groqApi.getChatCompletion(
                authorization = "Bearer $apiKey",
                request = request
            )

            val explanation = response.choices.firstOrNull()?.message?.content
            if (!explanation.isNullOrBlank()) {
                return@withContext explanation.trim()
            } else {
                return@withContext buildFallbackExplanation(analysisResult)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext buildFallbackExplanation(analysisResult)
        }
    }

    /**
     * Builds a clean local offline rule-based explanation.
     */
    private fun buildFallbackExplanation(result: AnalysisResult): String {
        val sb = StringBuilder()

        when (result.label) {
            "High Risk" -> {
                sb.append("⚠️ **High Risk Warning**: This message exhibits multiple classic phishing indicators and is very likely a scam designed to steal credentials, personal details, or money.\n\n")
            }
            "Suspicious" -> {
                sb.append("⚡ **Suspicious Activity**: This message contains unusual phrases or links that match known social engineering patterns. Exercise caution before clicking or replying.\n\n")
            }
            else -> {
                sb.append("✅ **Low Risk Identified**: No obvious automated phishing indicators were flagged. However, always remain vigilant with unsolicited messages.\n\n")
            }
        }

        sb.append("**Key Findings:**\n")
        result.reasons.forEach { reason ->
            sb.append("• ").append(reason).append("\n")
        }

        if (result.suspiciousPhrases.isNotEmpty()) {
            sb.append("\n**Flagged Phrases/Links:** ")
            sb.append(result.suspiciousPhrases.joinToString(", "))
            sb.append("\n")
        }

        sb.append("\n*Tip*: Never open unverified links or share passwords/OTPs over messaging apps.")

        return sb.toString()
    }
}
