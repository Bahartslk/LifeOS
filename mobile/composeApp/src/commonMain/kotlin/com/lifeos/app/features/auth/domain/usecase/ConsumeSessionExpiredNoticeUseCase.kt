package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.repository.SessionExpiryRepository

class ConsumeSessionExpiredNoticeUseCase(private val sessionExpiryRepository: SessionExpiryRepository) {
    operator fun invoke(): Boolean = sessionExpiryRepository.consumeSessionExpiredNotice()
}
