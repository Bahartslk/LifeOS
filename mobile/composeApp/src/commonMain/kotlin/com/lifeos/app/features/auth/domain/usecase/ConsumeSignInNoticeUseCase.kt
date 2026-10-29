package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.model.SignInNotice
import com.lifeos.app.features.auth.domain.repository.SessionExpiryRepository

class ConsumeSignInNoticeUseCase(private val sessionExpiryRepository: SessionExpiryRepository) {
    operator fun invoke(): SignInNotice? = sessionExpiryRepository.consumeSignInNotice()
}
