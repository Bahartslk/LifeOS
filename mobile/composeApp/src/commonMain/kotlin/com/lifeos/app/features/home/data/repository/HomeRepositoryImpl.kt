package com.lifeos.app.features.home.data.repository

import com.lifeos.app.features.home.data.datasource.HomeDataSource
import com.lifeos.app.features.home.domain.model.HomeDashboard
import com.lifeos.app.features.home.domain.repository.HomeRepository

/**
 * Real implementation of [HomeRepository], per Iteration 4's backend
 * integration — replaces the former `FakeHomeRepository`. No dedicated
 * `GET /dashboard` endpoint exists for Home's own chrome, and none is
 * needed: per this iteration's explicit "do not access backend endpoints
 * directly from the Home feature" rule, every piece of *real* dashboard
 * data (task counts, today's priorities, the upcoming trip) is fetched
 * from Planner's and Travel's own repositories, through their own existing
 * use cases, composed at the ViewModel layer — the same architecture
 * [com.lifeos.app.features.home.presentation.HomeViewModel] already used
 * for Planner before this iteration. This repository's only remaining job
 * is [HomeDataSource]'s honest empty shell (no avatar photo, no
 * highlights, no trend history, no journey) for
 * [com.lifeos.app.features.home.presentation.HomeViewModel] to enrich.
 *
 * Kept (not deleted) so [HomeRepository]'s interface and
 * [com.lifeos.app.features.home.domain.usecase.GetHomeDashboardUseCase]
 * stay exactly as they were, per this iteration's "reuse the existing
 * Repository interfaces/UseCases" rule — [HomeViewModel] still calls
 * [getDashboard] as one of several concurrent fetches.
 */
class HomeRepositoryImpl(
    private val dataSource: HomeDataSource,
) : HomeRepository {

    override suspend fun getDashboard(): Result<HomeDashboard> = Result.success(dataSource.getDashboard())
}
