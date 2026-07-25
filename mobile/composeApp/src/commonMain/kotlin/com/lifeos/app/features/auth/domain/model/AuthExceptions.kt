package com.lifeos.app.features.auth.domain.model

/** Thrown by [com.lifeos.app.features.auth.domain.repository.AuthRepository.login] on invalid credentials (backend: 401). */
class InvalidCredentialsException : Exception("Invalid email or password")

/** Thrown by [com.lifeos.app.features.auth.domain.repository.AuthRepository.register] when the email is already taken (backend: 409). */
class EmailAlreadyRegisteredException : Exception("Email already registered")
