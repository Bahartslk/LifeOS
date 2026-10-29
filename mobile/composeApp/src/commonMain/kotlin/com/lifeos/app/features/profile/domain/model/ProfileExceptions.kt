package com.lifeos.app.features.profile.domain.model

/** The password entered to confirm an account deletion is wrong (backend: 403). Nothing was deleted. */
class IncorrectPasswordException : Exception("Incorrect password")

/** Too many attempts in a short time (backend: 429). */
class TooManyAttemptsException : Exception("Too many attempts")
