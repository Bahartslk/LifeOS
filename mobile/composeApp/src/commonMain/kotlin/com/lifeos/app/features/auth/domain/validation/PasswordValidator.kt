package com.lifeos.app.features.auth.domain.validation

/** Business rule for a valid password — shared by Login and Register. */
object PasswordValidator {
    const val MIN_LENGTH = 8

    fun validate(password: String): ValidationResult = when {
        password.isBlank() -> ValidationResult.Invalid(ValidationError.EMPTY)
        password.length < MIN_LENGTH -> ValidationResult.Invalid(ValidationError.PASSWORD_TOO_SHORT)
        else -> ValidationResult.Valid
    }

    /** Register's confirm-password field: same value, not re-validated for strength. */
    fun validateConfirmation(password: String, confirmation: String): ValidationResult = when {
        confirmation.isBlank() -> ValidationResult.Invalid(ValidationError.EMPTY)
        confirmation != password -> ValidationResult.Invalid(ValidationError.PASSWORDS_DO_NOT_MATCH)
        else -> ValidationResult.Valid
    }
}
