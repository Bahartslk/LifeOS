package com.lifeos.app.features.ai.domain.model

/**
 * The backend answered `429`: the AI provider's quota is used up (on the
 * Gemini free tier this is usually the daily limit). The UI shows a clear
 * "try again later" message instead of a generic error.
 */
class AiQuotaExceededException(cause: Throwable? = null) : Exception("AI quota exceeded.", cause)
