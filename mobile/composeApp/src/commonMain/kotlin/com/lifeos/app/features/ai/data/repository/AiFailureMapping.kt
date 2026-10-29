package com.lifeos.app.features.ai.data.repository

import com.lifeos.app.core.network.ApiException
import com.lifeos.app.features.ai.domain.model.AiQuotaExceededException
import com.lifeos.app.features.ai.domain.model.ChatDailyLimitReachedException

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val CHAT_DAILY_LIMIT_REACHED_CODE = "CHAT_DAILY_LIMIT_REACHED"

/**
 * Maps the backend's two kinds of `429` for every AI call: the user's daily
 * chat limit (`code: "CHAT_DAILY_LIMIT_REACHED"`) and the AI provider's own
 * quota (no code). Anything else passes through unchanged.
 */
internal fun Exception.toAiFailure(): Exception = when {
    this !is ApiException || statusCode != HTTP_TOO_MANY_REQUESTS -> this
    code == CHAT_DAILY_LIMIT_REACHED_CODE -> ChatDailyLimitReachedException(this)
    else -> AiQuotaExceededException(this)
}
