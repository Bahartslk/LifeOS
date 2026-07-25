package com.lifeos.app.features.home.data.datasource

import com.lifeos.app.features.home.domain.model.GreetingInfo
import com.lifeos.app.features.home.domain.model.HomeDashboard
import com.lifeos.app.features.home.domain.model.IntelligentHubSummary
import com.lifeos.app.features.home.domain.model.TaskTrend

/**
 * The base (unenriched) Home dashboard shell, per Iteration 4's backend
 * integration — replaces the former `FakeHomeDataSource`. No backend
 * `GET /dashboard` endpoint exists for Home's own chrome, so this supplies
 * only the honest, real-or-empty values Home has no other source for:
 * [GreetingInfo.avatarUrl] (`null` — no user-photo backend field exists),
 * and an empty [IntelligentHubSummary]/[TaskTrend] with `null` trend delta.
 *
 * [GreetingInfo.userFirstName]/[IntelligentHubSummary.highlights]/
 * [HomeDashboard.upcomingJourney] are all immediately overwritten by
 * [com.lifeos.app.features.home.presentation.HomeViewModel] with real
 * session/Planner/Travel data — this class never invents a name, a
 * highlight, or a trip. See [com.lifeos.app.features.home.data.repository.HomeRepositoryImpl]'s
 * KDoc for why Home still has this small shell rather than deleting
 * [com.lifeos.app.features.home.domain.repository.HomeRepository] outright.
 */
class HomeDataSource {

    fun getDashboard(): HomeDashboard = HomeDashboard(
        greeting = GreetingInfo(userFirstName = "", avatarUrl = null),
        intelligentHub = IntelligentHubSummary(highlights = emptyList()),
        taskTrend = TaskTrend(productivityDeltaPercent = null, weeklyCompletionRatios = emptyList()),
        upcomingJourney = null,
    )
}
