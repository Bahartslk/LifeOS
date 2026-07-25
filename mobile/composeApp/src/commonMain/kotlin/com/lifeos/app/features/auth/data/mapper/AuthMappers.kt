package com.lifeos.app.features.auth.data.mapper

import com.lifeos.app.features.auth.data.dto.AuthResponseDto
import com.lifeos.app.features.auth.domain.model.AuthSession
import com.lifeos.app.features.auth.domain.model.AuthUser

/** Data-layer DTO → domain-model mapping, per docs/12-project-architecture.md#repository-pattern. */
fun AuthResponseDto.toDomain(): AuthSession = AuthSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    user = AuthUser(email = email, fullName = fullName),
)
