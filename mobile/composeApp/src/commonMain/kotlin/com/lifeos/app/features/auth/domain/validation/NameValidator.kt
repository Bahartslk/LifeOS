package com.lifeos.app.features.auth.domain.validation

/** Business rule for Register's full-name field. */
object NameValidator {
    const val MIN_LENGTH = 2

    fun validate(name: String): ValidationResult = when {
        name.isBlank() -> ValidationResult.Invalid(ValidationError.EMPTY)
        name.trim().length < MIN_LENGTH -> ValidationResult.Invalid(ValidationError.NAME_TOO_SHORT)
        else -> ValidationResult.Valid
    }
}
