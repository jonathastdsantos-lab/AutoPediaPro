package com.example.data

import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

@JsonClass(generateAdapter = true)
data class GeminiPart(val text: String? = null)

@JsonClass(generateAdapter = true)
data class GeminiContent(val parts: List<GeminiPart>)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(val content: GeminiContent)

@JsonClass(generateAdapter = true)
data class GeminiResponse(val candidates: List<GeminiCandidate>?)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    val api: GeminiApi = retrofit.create(GeminiApi::class.java)

    suspend fun askGemini(prompt: String, systemInstruction: String? = null): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return "Erro: Chave de API do Gemini (GEMINI_API_KEY) não configurada. Configure a sua chave de API nos Segredos (Secrets) do Google AI Studio para usufruir do diagnóstico inteligente com inteligência artificial!"
        }

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            systemInstruction = systemInstruction?.let { GeminiContent(parts = listOf(GeminiPart(text = it))) }
        )

        return try {
            val response = api.generateContent(key, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Desculpe, não consegui analisar este problema. Tente detalhar mais os sintomas do veículo."
        } catch (e: Exception) {
            "Erro ao comunicar com o Gemini: ${e.localizedMessage}. Certifique-se de que sua chave de API do Gemini esteja configurada corretamente no painel de Segredos (Secrets) do AI Studio."
        }
    }
}
