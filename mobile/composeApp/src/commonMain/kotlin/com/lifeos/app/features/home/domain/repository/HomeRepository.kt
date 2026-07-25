package com.lifeos.app.features.home.domain.repository

import com.lifeos.app.features.home.domain.model.HomeDashboard

/**
 * Abstracts fetching the dashboard aggregate, per
 * docs/12-project-architecture.md#repository-pattern. ViewModels depend
 * only on this interface; the fake implementation
 * ([com.lifeos.app.features.home.data.repository.FakeHomeRepository]) is
 * bound in `di/HomeModule.kt` today and is the only thing that changes when
 * a real backend exists.
 */
interface HomeRepository {
    suspend fun getDashboard(): Result<HomeDashboard>
}
