package com.lifeos.app.features.ai.data.remote

import com.lifeos.app.core.network.ApiConfig
import com.lifeos.app.core.network.dataOrThrow
import com.lifeos.app.features.ai.data.dto.DailyOverviewResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.JsonObject

/**
 * Calls the backend's `POST /api/v1/daily-brief/overview`, which reads the
 * signed-in user's own Planner and Travel data on the server and answers with
 * the whole Daily Brief in one AI call. It never changes any task or trip.
 *
 * One request instead of one per part keeps the card within the AI provider's
 * per-minute quota.
 *
 * Uses the shared authenticated `HttpClient`, like every other feature.
 * The AI call gets a longer timeout than [ApiConfig.REQUEST_TIMEOUT_MILLIS]:
 * the backend may retry a provider and fall back to another one before
 * answering, which can take longer than a normal request.
 */
class DailyBriefRemoteDataSource(private val httpClient: HttpClient) {

    /** The endpoint takes an optional `{ notes }` body; mobile sends none. */
    suspend fun getOverview(): DailyOverviewResponseDto =
        httpClient.post("${ApiConfig.BASE_URL}/daily-brief/overview") {
            contentType(ContentType.Application.Json)
            setBody(JsonObject(emptyMap()))
            timeout { requestTimeoutMillis = AI_REQUEST_TIMEOUT_MILLIS }
        }.dataOrThrow()

    private companion object {
        const val AI_REQUEST_TIMEOUT_MILLIS: Long = 60_000
    }
}
