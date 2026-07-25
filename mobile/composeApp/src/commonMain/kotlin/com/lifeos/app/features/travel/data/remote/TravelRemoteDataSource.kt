package com.lifeos.app.features.travel.data.remote

import com.lifeos.app.core.network.ApiConfig
import com.lifeos.app.core.network.ApiErrorResponse
import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.network.dataOrThrow
import com.lifeos.app.features.travel.data.dto.CreateTripRequestDto
import com.lifeos.app.features.travel.data.dto.ItineraryItemDto
import com.lifeos.app.features.travel.data.dto.PaginatedTripsDto
import com.lifeos.app.features.travel.data.dto.TravelDashboardDto
import com.lifeos.app.features.travel.data.dto.TripDto
import com.lifeos.app.features.travel.domain.model.UpdateTripRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Calls the real `/api/v1/travel` endpoints, per docs/15-api-design.md.
 * Every route on `TravelController` requires a bearer token (`JwtAuthGuard`
 * at the controller level), so — like [com.lifeos.app.features.planner.data.remote.PlannerRemoteDataSource]
 * and unlike [com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource] —
 * this class uses the shared *authenticated* `HttpClient`.
 */
class TravelRemoteDataSource(private val httpClient: HttpClient) {

    suspend fun getDashboard(): TravelDashboardDto =
        httpClient.get(endpoint("travel/dashboard")).dataOrThrow()

    /**
     * Every `CANCELLED` trip, looped across pages until exhausted — `GET
     * /travel/dashboard` never returns them (see [TravelDashboardDto]'s
     * KDoc), so this is the only way Travel List's "past trips" bucket can
     * include a cancelled trip at all.
     *
     * Decodes the response directly as [PaginatedTripsDto], not via
     * [dataOrThrow] — `GET /travel/trips`'s body is already `{ data: [...],
     * meta: {...} }` (the backend's `TransformResponseInterceptor` passes a
     * paginated response through unwrapped, since it already looks
     * enveloped), so treating it as `{ data: PaginatedTripsDto }` and
     * unwrapping a second time would look for a nested object where the
     * array itself already *is* `data`.
     */
    suspend fun getCancelledTrips(): List<TripDto> {
        val allTrips = mutableListOf<TripDto>()
        var cursor: String? = null
        do {
            val response = httpClient.get(endpoint("travel/trips")) {
                parameter("status", "CANCELLED")
                parameter("limit", PAGE_SIZE)
                cursor?.let { parameter("cursor", it) }
            }
            val page: PaginatedTripsDto = response.bodyOrThrow()
            allTrips += page.data
            cursor = page.meta.nextCursor
        } while (cursor != null)
        return allTrips
    }

    suspend fun getTrip(tripId: String): TripDto =
        httpClient.get(endpoint("travel/trips/$tripId")).dataOrThrow()

    suspend fun getItinerary(tripId: String): List<ItineraryItemDto> =
        httpClient.get(endpoint("travel/trips/$tripId/itinerary")).dataOrThrow()

    suspend fun createTrip(request: CreateTripRequestDto): TripDto {
        val response = httpClient.post(endpoint("travel/trips")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.dataOrThrow()
    }

    /**
     * Builds the PATCH body as a plain [kotlinx.serialization.json.JsonObject]
     * containing only [request]'s non-null fields, rather than a
     * `@Serializable` data class with nullable properties — the latter would
     * serialize every untouched field as an explicit `"field": null "`
     * (`HttpClientFactory.json` doesn't set `explicitNulls = false`, and
     * changing that shared config for one feature's PATCH shape isn't worth
     * the blast radius), which the backend would treat as "set this
     * required field to null" for a `@IsOptional()` DTO field, not "leave
     * unchanged".
     */
    suspend fun updateTrip(tripId: String, request: UpdateTripRequest): TripDto {
        val body = buildJsonObject {
            request.title?.let { put("title", it) }
            request.description?.let { put("description", it) }
            request.destination?.let { put("destination", it) }
            request.country?.let { put("country", it) }
            request.startDate?.let { put("startDate", it.toString()) }
            request.endDate?.let { put("endDate", it.toString()) }
            request.coverImageUrl?.let { put("coverImageUrl", it) }
            request.status?.let { put("status", it.name) }
        }
        val response = httpClient.patch(endpoint("travel/trips/$tripId")) {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        return response.dataOrThrow()
    }

    suspend fun deleteTrip(tripId: String) {
        httpClient.delete(endpoint("travel/trips/$tripId")).throwIfError()
    }

    /**
     * `DELETE /travel/trips/:id` replies `204 No Content` — see
     * [com.lifeos.app.features.planner.data.remote.PlannerRemoteDataSource.throwIfError]'s
     * identical KDoc for why this is duplicated locally rather than
     * extending the shared `dataOrThrow` helper.
     */
    private suspend fun HttpResponse.throwIfError() {
        if (status.isSuccess()) return
        val error = runCatching { body<ApiErrorResponse>() }.getOrNull()
        throw ApiException(
            statusCode = status.value,
            errorCode = error?.error ?: status.description,
            message = error?.message ?: "Request failed with status ${status.value}",
        )
    }

    /**
     * Like [dataOrThrow], but for a response that's already the full
     * `{ data, meta }` shape on the wire (a paginated list) rather than one
     * `dataOrThrow` needs to unwrap a `{ data: T }` envelope from — see
     * [getCancelledTrips]'s KDoc.
     */
    private suspend inline fun <reified T> HttpResponse.bodyOrThrow(): T {
        if (status.isSuccess()) return body()
        val error = runCatching { body<ApiErrorResponse>() }.getOrNull()
        throw ApiException(
            statusCode = status.value,
            errorCode = error?.error ?: status.description,
            message = error?.message ?: "Request failed with status ${status.value}",
        )
    }

    private fun endpoint(path: String): String = "${ApiConfig.BASE_URL}/$path"

    private companion object {
        const val PAGE_SIZE = 100
    }
}
