package com.lifeos.app.features.auth.domain.usecase

import com.lifeos.app.features.auth.domain.repository.SessionExpiryRepository
import kotlinx.coroutines.flow.Flow

class ObserveSessionExpiredUseCase(private val sessionExpiryRepository: SessionExpiryRepository) {
    operator fun invoke(): Flow<Unit> = sessionExpiryRepository.observeSessionExpired()
}
