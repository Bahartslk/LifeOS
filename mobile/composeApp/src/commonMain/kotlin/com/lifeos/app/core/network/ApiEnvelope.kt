package com.lifeos.app.core.network

import kotlinx.serialization.Serializable

/**
 * Every successful backend response is wrapped `{ "data": ... }` (list
 * endpoints additionally carry a `meta` block, not needed by any endpoint
 * this client calls yet), per docs/15-api-design.md#request-and-response-formats.
 * Remote data sources decode into `ApiEnvelope<T>` and unwrap `.data` rather
 * than each feature re-implementing the same wrapper.
 */
@Serializable
data class ApiEnvelope<T>(val data: T)

/**
 * Every error response uses this consistent envelope, per
 * docs/15-api-design.md#error-handling. Remote data sources parse a
 * non-2xx body into this shape to build a meaningful failure rather than a
 * bare HTTP status code.
 */
@Serializable
data class ApiErrorResponse(
    val statusCode: Int,
    val error: String,
    val message: String,
    val path: String? = null,
    val timestamp: String? = null,
)
