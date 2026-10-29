package com.lifeos.app.features.auth.domain.model

/** Thrown by [com.lifeos.app.features.auth.domain.repository.AuthRepository.login] on invalid credentials (backend: 401). */
class InvalidCredentialsException : Exception("Invalid email or password")

/** Thrown by [com.lifeos.app.features.auth.domain.repository.AuthRepository.register] when the email is already taken (backend: 409). */
class EmailAlreadyRegisteredException : Exception("Email already registered")

/**
 * Thrown by [com.lifeos.app.features.auth.domain.repository.AuthRepository.resetPassword]
 * when the code is wrong, expired, used up or unknown (backend: 400 `INVALID_RESET_CODE`).
 */
class InvalidResetCodeException : Exception("Invalid or expired reset code")

/** Too many password reset requests or attempts in a short time (backend: 429). */
class PasswordResetRateLimitedException : Exception("Too many password reset attempts")
