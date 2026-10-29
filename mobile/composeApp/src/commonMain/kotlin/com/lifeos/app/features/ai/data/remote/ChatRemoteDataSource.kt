package com.lifeos.app.features.ai.data.remote

import com.lifeos.app.core.network.ApiConfig
import com.lifeos.app.core.network.dataOrThrow
import com.lifeos.app.features.ai.data.dto.ChatRequestDto
import com.lifeos.app.features.ai.data.dto.ChatResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

/**
 * Calls the backend's stateless `POST /api/v1/ai/chat` with the shared
 * authenticated client. Like the Daily Brief, the AI call gets a longer
 * timeout than a normal request, since the backend may retry and fall back
 * between Gemini models before answering.
 */
class ChatRemoteDataSource(private val httpClient: HttpClient) {

    suspend fun sendMessage(request: ChatRequestDto): ChatResponseDto =
        httpClient.post("${ApiConfig.BASE_URL}/ai/chat") {
            contentType(ContentType.Application.Json)
            setBody(request)
            timeout { requestTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS }
        }.dataOrThrow()

    private companion object {
        const val AI_REQUEST_TIMEOUT_MILLIS: Long = 60_000
    }
}
