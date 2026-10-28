package com.lifeos.app.features.ai.data.remote

import com.lifeos.app.core.network.ApiConfig
import com.lifeos.app.core.network.dataOrThrow
import com.lifeos.app.features.ai.data.dto.DailyMotivationResponseDto
import com.lifeos.app.features.ai.data.dto.DailyPrioritiesResponseDto
import com.lifeos.app.features.ai.data.dto.DailySummaryResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject

/**
 * Calls the backend's Daily Brief endpoints (`/api/v1/daily-brief/...`),
 * which read the signed-in user's own Planner and Travel data on the server
 * and answer with AI suggestions. They never change any task or trip.
 *
 * Uses the shared authenticated `HttpClient`, like every other feature.
 * AI calls get a longer timeout than [ApiConfig.REQUEST_TIMEOUT_MILLIS]:
 * the backend may retry a provider and fall back to another one before
 * answering, which can take longer than a normal request.
 */
class DailyBriefRemoteDataSource(private val httpClient: HttpClient) {

    suspend fun getSummary(): DailySummaryResponseDto = postCapability("daily-brief/summary").dataOrThrow()

    suspend fun getPriorities(): DailyPrioritiesResponseDto =
        postCapability("daily-brief/priorities").dataOrThrow()

    suspend fun getMotivation(): DailyMotivationResponseDto =
        postCapability("daily-brief/motivation").dataOrThrow()

    /** Every Daily Brief endpoint takes the same optional `{ notes }` body; mobile sends none. */
    private suspend fun postCapability(path: String): HttpResponse =
        httpClient.post("${ApiConfig.BASE_URL}/$path") {
            contentType(ContentType.Application.Json)
            setBody(JsonObject(emptyMap()))
            timeout { requestTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS }
        }

    private companion object {
        const val AI_REQUEST_TIMEOUT_MILLIS: Long = 60_000
    }
}
