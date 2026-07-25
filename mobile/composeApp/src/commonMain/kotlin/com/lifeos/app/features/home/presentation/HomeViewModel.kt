package com.lifeos.app.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeos.app.core.date.RelativeDateFormatter
import com.lifeos.app.core.util.currentHourOfDay
import com.lifeos.app.features.auth.domain.usecase.GetSessionUseCase
import com.lifeos.app.features.home.domain.model.HomeDashboard
import com.lifeos.app.features.home.domain.model.HubHighlight
import com.lifeos.app.features.home.domain.model.HubHighlightType
import com.lifeos.app.features.home.domain.model.IntelligentHubSummary
import com.lifeos.app.features.home.domain.model.OverviewStats
import com.lifeos.app.features.home.domain.model.PriorityTask
import com.lifeos.app.features.home.domain.model.UpcomingJourney
import com.lifeos.app.features.home.domain.usecase.GetHomeDashboardUseCase
import com.lifeos.app.features.planner.domain.model.PlannerDashboard
import com.lifeos.app.features.planner.domain.model.Task
import com.lifeos.app.features.planner.domain.model.TaskCategory
import com.lifeos.app.features.planner.domain.model.TaskStatus
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import com.lifeos.app.features.travel.domain.model.TravelListData
import com.lifeos.app.features.travel.domain.model.Trip
import com.lifeos.app.features.travel.domain.model.TripStatus
import com.lifeos.app.features.travel.domain.usecase.GetTravelListUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Home's own ViewModel — a pure consumer of Planner and Travel, not an
 * independent feature (Iteration 4's backend integration completes what
 * the earlier Planner integration started). Depends on
 * [GetHomeDashboardUseCase] only for Home's own honest-empty chrome shell
 * (see [com.lifeos.app.features.home.data.repository.HomeRepositoryImpl]'s
 * KDoc); every piece of *real* data — today's priorities, task counts, and
 * the upcoming trip — comes directly from Planner's existing
 * [GetPlannerDashboardUseCase] and Travel's existing [GetTravelListUseCase],
 * composed here rather than either repository depending on the other. This
 * mirrors the exact `PlannerRepository -> UseCases -> HomeViewModel -> Home
 * UI` / `TravelRepository -> UseCases -> HomeViewModel -> Home UI`
 * data-flow the rest of this migration already established (see
 * [com.lifeos.app.features.travel.presentation.TravelDetailViewModel]'s own
 * identical Planner composition).
 *
 * All three use cases are fetched concurrently in [loadDashboard] — they're
 * independent, so there's no reason to serialize them — and each is called
 * exactly once per load, with its result reused for every dashboard section
 * that needs it (Planner's single fetch backs [priorities], [OverviewStats],
 * and the Intelligent Hub's task highlight; Travel's single fetch backs the
 * Intelligent Hub's trip highlight and [UpcomingJourney]), per this
 * iteration's "avoid fetching the same data twice" requirement.
 *
 * A Travel fetch failure degrades gracefully (no upcoming journey, no trip
 * highlight) rather than failing the whole screen — matching how
 * [UpcomingJourney] was already documented as an optional, sometimes-absent
 * section; a Planner fetch failure still fails the whole screen, unchanged
 * from before this iteration, since Planner is Home's primary data source.
 */
class HomeViewModel(
    private val getHomeDashboard: GetHomeDashboardUseCase,
    private val getPlannerDashboard: GetPlannerDashboardUseCase,
    private val getTravelList: GetTravelListUseCase,
    private val getSession: GetSessionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _actions = Channel<HomeAction>(Channel.BUFFERED)
    val actions = _actions.receiveAsFlow()

    init {
        loadDashboard()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.RetryClicked -> loadDashboard()
            // A silent background refresh, not a full reload — so returning to Home after
            // creating/completing/deleting a task or trip elsewhere never re-shows the
            // loading skeleton over data the user has already seen.
            HomeEvent.ScreenResumed -> loadDashboard(showLoading = false)
            HomeEvent.SettingsClicked -> sendAction(HomeAction.NavigateToProfile)
            HomeEvent.ViewTripClicked -> sendAction(HomeAction.NavigateToTravel)
            HomeEvent.CompleteChecklistClicked -> sendAction(HomeAction.NavigateToTravel)
            HomeEvent.ManageAllPrioritiesClicked -> sendAction(HomeAction.NavigateToPlanner)
            is HomeEvent.PriorityTaskClicked -> sendAction(HomeAction.NavigateToTaskDetail(event.taskId))
            is HomeEvent.QuickActionClicked -> onQuickAction(event.action)
        }
    }

    private fun onQuickAction(action: HomeQuickAction) {
        when (action) {
            HomeQuickAction.NEW_TASK -> sendAction(HomeAction.NavigateToCreateTask)
            HomeQuickAction.CREATE_TRIP -> sendAction(HomeAction.NavigateToTravel)
            HomeQuickAction.ASK_AI -> sendAction(HomeAction.NavigateToAiChat)
            // Notes has no destination yet — 05-screen-inventory.md defines no
            // "Notes" screen under any module, unlike the other three actions.
            HomeQuickAction.CREATE_NOTE ->
                sendAction(HomeAction.ShowMessage(HomeStrings.QUICK_ACTION_NOTES_COMING_SOON))
        }
    }

    private fun loadDashboard(showLoading: Boolean = true) {
        viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            }
            coroutineScope {
                val homeDeferred = async { getHomeDashboard() }
                val plannerDeferred = async { getPlannerDashboard() }
                val travelDeferred = async { getTravelList() }
                val sessionDeferred = async { getSession() }
                val homeResult = homeDeferred.await()
                val plannerResult = plannerDeferred.await()
                val travelResult = travelDeferred.await()
                val session = sessionDeferred.await()

                val plannerDashboard = plannerResult.getOrNull()
                // Travel is a supplementary section here (same status
                // Planner's own relatedTasks fetch already treats it as in
                // TravelDetailViewModel) — a failure just means no upcoming
                // journey/trip highlight, not a failed Home screen.
                val travelList = travelResult.getOrNull()

                val homeDashboard = homeResult.getOrNull()?.let { dashboard ->
                    val withGreeting = session?.user?.fullName?.substringBefore(' ')?.takeIf { it.isNotBlank() }
                        ?.let { firstName -> dashboard.copy(greeting = dashboard.greeting.copy(userFirstName = firstName)) }
                        ?: dashboard
                    withGreeting.copy(
                        intelligentHub = buildIntelligentHub(plannerDashboard, travelList),
                        upcomingJourney = travelList?.let { buildUpcomingJourney(it) },
                    )
                }

                if (homeDashboard != null && plannerDashboard != null) {
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        greetingMessage = greetingWordForCurrentHour(),
                        dashboard = homeDashboard,
                        priorities = plannerDashboard.todayTasks.map { it.toPriorityTask() },
                        overview = OverviewStats(
                            completedTaskCount = plannerDashboard.overview.completedTaskCount,
                            totalTaskCount = plannerDashboard.overview.totalTaskCount,
                            productivityPercent = plannerDashboard.overview.productivityPercent,
                            // No real week-over-week history exists anywhere
                            // (Planner and Travel's backends both confirmed to
                            // have none) — null/empty are honest states, not a
                            // loading gap. See OverviewStats's KDoc.
                            productivityDeltaPercent = null,
                            weeklyCompletionRatios = emptyList(),
                        ),
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = HomeStrings.LOAD_ERROR_MESSAGE)
                }
            }
        }
    }

    private fun sendAction(action: HomeAction) {
        viewModelScope.launch { _actions.send(action) }
    }

    /**
     * Reads the device clock once, here — never from a Composable, which
     * must stay a pure function of [HomeUiState] so its @Preview renders
     * the same result regardless of when someone opens it.
     */
    private fun greetingWordForCurrentHour(): String = when (currentHourOfDay()) {
        in 5..11 -> HomeStrings.GREETING_MORNING
        in 12..17 -> HomeStrings.GREETING_AFTERNOON
        else -> HomeStrings.GREETING_EVENING
    }
}

/**
 * Builds the Intelligent Hub's highlight list from real Planner/Travel
 * data — 0, 1, or 2 rows, never a fixed count, per
 * [IntelligentHubSummary.highlights]'s own "variable-length list" design.
 * No PACKING or WEATHER highlight is ever produced — Travel's backend has
 * no packing or weather data at all (Iteration 3's own findings), and
 * inventing either would be exactly the "fake business data" this
 * iteration's Quality Rules forbid. A TASK highlight is only added when
 * there's actually something to report — "0 important tasks today" reads
 * as a fabricated observation, not a real one, so a zero count simply
 * omits the row instead.
 */
private fun buildIntelligentHub(
    plannerDashboard: PlannerDashboard?,
    travelList: TravelListData?,
): IntelligentHubSummary {
    val highlights = buildList {
        nextPlannedTrip(travelList)?.daysUntilStart?.let { days ->
            add(HubHighlight(HubHighlightType.TRIP, HomeStrings.hubTripHighlight(days)))
        }
        val importantTaskCount = plannerDashboard?.todayTasks?.count { it.status != TaskStatus.DONE } ?: 0
        if (importantTaskCount > 0) {
            add(HubHighlight(HubHighlightType.TASK, HomeStrings.hubTaskHighlight(importantTaskCount)))
        }
    }
    return IntelligentHubSummary(highlights = highlights)
}

/**
 * [UpcomingJourney] built from Travel's real, lean [Trip] list — never
 * [com.lifeos.app.features.travel.domain.model.TripDetail], which would
 * need an extra itinerary fetch per trip just to populate this one summary
 * card (an unnecessary network call this iteration's Performance rules
 * explicitly call out to avoid). Weather/flight/hotel fields are always
 * `null` as a result — see [UpcomingJourney]'s own KDoc for why that's a
 * real, documented limitation rather than a gap to fill with a placeholder.
 */
private fun buildUpcomingJourney(travelList: TravelListData): UpcomingJourney? {
    val trip = nextPlannedTrip(travelList) ?: return null
    val daysRemaining = trip.daysUntilStart ?: return null
    return UpcomingJourney(
        destinationName = trip.destinationCity,
        region = trip.destinationCountry,
        daysRemaining = daysRemaining,
        weatherTemperatureCelsius = null,
        flightCode = null,
        flightGate = null,
        flightDepartureLabel = null,
        hotelName = null,
        hotelRoomType = null,
        coverImageUrl = trip.coverImageUrl,
    )
}

/** The soonest not-yet-started trip with a known start date — the same trip both [buildIntelligentHub]'s TRIP highlight and [buildUpcomingJourney] describe, computed once and reused per this iteration's "don't fetch/derive the same thing twice" rule. */
private fun nextPlannedTrip(travelList: TravelListData?): Trip? = travelList
    ?.upcomingTrips
    ?.filter { it.status == TripStatus.PLANNED && it.daysUntilStart != null }
    ?.minByOrNull { it.daysUntilStart!! }

/**
 * [Task] -> [PriorityTask]: Home's own presentation-shaped projection of
 * Planner's canonical task, per [PriorityTask]'s KDoc. [dueLabel] reuses
 * [RelativeDateFormatter] directly on [Task.dueDate] — no string parsing,
 * per this integration's "reuse the LocalDate infrastructure" requirement.
 *
 * `internal` (not `private`): `HomeScreen.kt`'s previews need this exact
 * same mapping to build realistic [PriorityTask] preview data from
 * [com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource]
 * without duplicating the mapping logic a second time.
 */
internal fun Task.toPriorityTask(): PriorityTask = PriorityTask(
    id = id,
    title = title,
    category = category.toHomeCategoryLabel(),
    dueLabel = RelativeDateFormatter.format(dueDate.date, dueDate.time),
    isCompleted = status == TaskStatus.DONE,
    priority = priority,
)

internal fun TaskCategory.toHomeCategoryLabel(): String = when (this) {
    TaskCategory.WORK -> HomeStrings.CATEGORY_WORK
    TaskCategory.PERSONAL -> HomeStrings.CATEGORY_PERSONAL
    TaskCategory.HEALTH -> HomeStrings.CATEGORY_HEALTH
    TaskCategory.TRAVEL -> HomeStrings.CATEGORY_TRAVEL
    TaskCategory.FINANCE -> HomeStrings.CATEGORY_FINANCE
}
