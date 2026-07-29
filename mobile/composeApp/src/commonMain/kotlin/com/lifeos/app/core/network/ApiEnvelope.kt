package com.lifeos.app.core.network

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

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
    @Serializable(with = FlexibleMessageSerializer::class)
    val message: String,
    val path: String? = null,
    val timestamp: String? = null,
)

/**
 * NestJS's `ValidationPipe` returns `message` as a `string[]` (one entry per
 * failed validation rule) for request-validation failures, but every other
 * thrown exception (e.g. a plain `UnauthorizedException('...')`) returns a
 * single `string` — both pass through unchanged by
 * `backend/src/common/filters/all-exceptions.filter.ts`. Decoding `message`
 * as a plain `String` previously mismatched the array case: a
 * `SerializationException` that every caller's own `runCatching` swallowed,
 * silently discarding the real validation detail behind a generic
 * status-only fallback message. This accepts either shape and flattens an
 * array into one display string instead.
 */
object FlexibleMessageSerializer : KSerializer<String> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleMessage", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeString()
        return when (val element = jsonDecoder.decodeJsonElement()) {
            is JsonArray -> element.joinToString(separator = "; ") { it.jsonPrimitive.content }
            else -> element.jsonPrimitive.content
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}
