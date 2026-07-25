package com.lifeos.app.features.planner.data.remote

import com.lifeos.app.core.network.ApiConfig
import com.lifeos.app.core.network.ApiErrorResponse
import com.lifeos.app.core.network.ApiException
import com.lifeos.app.core.network.dataOrThrow
import com.lifeos.app.features.planner.data.dto.CreateTaskRequestDto
import com.lifeos.app.features.planner.data.dto.PlannerDashboardDto
import com.lifeos.app.features.planner.data.dto.TaskDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Calls the real `/api/v1/planner` endpoints, per docs/15-api-design.md.
 * Every route on `PlannerController` requires a bearer token
 * (`JwtAuthGuard` at the controller level), so — unlike
 * [com.lifeos.app.features.auth.data.remote.AuthRemoteDataSource], which
 * deliberately avoids it — this class uses the shared *authenticated*
 * `HttpClient` (`core/di/NetworkModule.kt`'s unqualified binding, with the
 * `Auth` plugin already installed): there is no circular dependency here,
 * since Planner never supplies `AuthTokenProvider`.
 */
class PlannerRemoteDataSource(private val httpClient: HttpClient) {

    suspend fun getDashboard(): PlannerDashboardDto =
        httpClient.get(endpoint("planner/dashboard")).dataOrThrow()

    suspend fun getTask(taskId: String): TaskDto =
        httpClient.get(endpoint("planner/tasks/$taskId")).dataOrThrow()

    suspend fun createTask(request: CreateTaskRequestDto): TaskDto {
        val response = httpClient.post(endpoint("planner/tasks")) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        return response.dataOrThrow()
    }

    suspend fun deleteTask(taskId: String) {
        httpClient.delete(endpoint("planner/tasks/$taskId")).throwIfError()
    }

    suspend fun markComplete(taskId: String): TaskDto =
        httpClient.patch(endpoint("planner/tasks/$taskId/complete")).dataOrThrow()

    suspend fun markIncomplete(taskId: String): TaskDto =
        httpClient.patch(endpoint("planner/tasks/$taskId/incomplete")).dataOrThrow()

    /**
     * `DELETE /planner/tasks/:id` replies `204 No Content` — no JSON body,
     * so [com.lifeos.app.core.network.dataOrThrow] (which always tries to
     * decode a `{data: ...}` envelope on success) doesn't fit. Duplicates
     * its few-line error branch locally rather than changing the shared
     * helper, per this iteration's "stay inside the Planner data layer"
     * scope.
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

    private fun endpoint(path: String): String = "${ApiConfig.BASE_URL}/$path"
}
