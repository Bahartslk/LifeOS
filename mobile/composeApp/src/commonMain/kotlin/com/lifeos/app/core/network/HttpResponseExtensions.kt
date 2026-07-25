package com.lifeos.app.core.network

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

/**
 * A parsed backend error — [statusCode]/[errorCode] let a repository map
 * specific failures (401, 409, ...) to a domain-specific exception type;
 * anything that doesn't need that distinction can just surface [message].
 */
class ApiException(
    val statusCode: Int,
    val errorCode: String,
    override val message: String,
) : Exception(message)

/**
 * Unwraps a successful response's `{ data: ... }` envelope into [T], or
 * throws [ApiException] built from the error envelope, per
 * docs/15-api-design.md#request-and-response-formats /
 * docs/15-api-design.md#error-handling. Every feature's remote data source
 * calls this instead of re-parsing the envelope itself.
 */
suspend inline fun <reified T> HttpResponse.dataOrThrow(): T {
    if (status.isSuccess()) {
        return body<ApiEnvelope<T>>().data
    }
    val error = runCatching { body<ApiErrorResponse>() }.getOrNull()
    throw ApiException(
        statusCode = status.value,
        errorCode = error?.error ?: status.description,
        message = error?.message ?: "Request failed with status ${status.value}",
    )
}
