package com.lifeos.app.features.auth.domain.validation

/**
 * Business rule for a valid password, shared by Login, Register, Change
 * Password and Reset Password. Mirrors the backend's length-only policy:
 * 8 to 72 characters (72 is bcrypt's input limit, so the backend rejects
 * longer passwords instead of silently cutting them).
 */
object PasswordValidator {
    const val MIN_LENGTH = 8
    const val MAX_LENGTH = 72

    fun validate(password: String): ValidationResult = when {
        password.isBlank() -> ValidationResult.Invalid(ValidationError.EMPTY)
        password.length < MIN_LENGTH -> ValidationResult.Invalid(ValidationError.PASSWORD_TOO_SHORT)
        password.length > MAX_LENGTH -> ValidationResult.Invalid(ValidationError.PASSWORD_TOO_LONG)
        else -> ValidationResult.Valid
    }

    /** Register's confirm-password field: same value, not re-validated for strength. */
    fun validateConfirmation(password: String, confirmation: String): ValidationResult = when {
        confirmation.isBlank() -> ValidationResult.Invalid(ValidationError.EMPTY)
        confirmation != password -> ValidationResult.Invalid(ValidationError.PASSWORDS_DO_NOT_MATCH)
        else -> ValidationResult.Valid
    }
}
