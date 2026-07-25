package com.lifeos.app.features.auth.domain.validation

/**
 * The outcome of validating a single field. Deliberately carries an
 * [ValidationError] enum, not a display string — the domain layer has no
 * opinion on Turkish copy; the presentation layer maps each [ValidationError]
 * to its Turkish message (see `features/auth/presentation/AuthStrings.kt`).
 *
 * Terms-acceptance is not one of these: it's a single checkbox boolean, not
 * a parsed/formatted field, so `RegisterViewModel` checks it directly
 * against `AuthStrings.TERMS_NOT_ACCEPTED_ERROR` rather than round-tripping
 * through this field-validation pipeline.
 */
sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val error: ValidationError) : ValidationResult
}

enum class ValidationError {
    EMPTY,
    INVALID_EMAIL_FORMAT,
    PASSWORD_TOO_SHORT,
    PASSWORDS_DO_NOT_MATCH,
    NAME_TOO_SHORT,
}

inline fun ValidationResult.errorOrNull(): ValidationError? =
    (this as? ValidationResult.Invalid)?.error

/**
 * Maps a failed validation straight to its display message, or `null` if
 * valid — the one line every screen's submit handler needs, shared so
 * Login/Register/Forgot Password don't each reimplement the same
 * `as? Invalid` cast.
 */
inline fun ValidationResult.errorMessage(mapper: (ValidationError) -> String): String? =
    errorOrNull()?.let(mapper)
