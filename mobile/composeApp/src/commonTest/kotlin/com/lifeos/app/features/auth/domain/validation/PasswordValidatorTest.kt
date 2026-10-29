package com.lifeos.app.features.auth.domain.validation

import kotlin.test.Test
import kotlin.test.assertEquals

class PasswordValidatorTest {

    @Test
    fun acceptsPasswordsFrom8To72Characters() {
        assertEquals(ValidationResult.Valid, PasswordValidator.validate("a".repeat(8)))
        assertEquals(ValidationResult.Valid, PasswordValidator.validate("a".repeat(72)))
    }

    @Test
    fun rejectsEmptyShortAndOverlongPasswords() {
        assertEquals(ValidationResult.Invalid(ValidationError.EMPTY), PasswordValidator.validate(""))
        assertEquals(ValidationResult.Invalid(ValidationError.PASSWORD_TOO_SHORT), PasswordValidator.validate("a".repeat(7)))
        assertEquals(ValidationResult.Invalid(ValidationError.PASSWORD_TOO_LONG), PasswordValidator.validate("a".repeat(73)))
    }
}
