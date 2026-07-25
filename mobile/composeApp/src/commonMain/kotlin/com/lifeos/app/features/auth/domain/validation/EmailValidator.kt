package com.lifeos.app.features.auth.domain.validation

/**
 * Business rule for what counts as a valid email — used identically by
 * Login, Register, and Forgot Password so the three screens can never
 * silently drift apart on what "valid" means.
 */
object EmailValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validate(email: String): ValidationResult = when {
        email.isBlank() -> ValidationResult.Invalid(ValidationError.EMPTY)
        !EMAIL_REGEX.matches(email) -> ValidationResult.Invalid(ValidationError.INVALID_EMAIL_FORMAT)
        else -> ValidationResult.Valid
    }
}
