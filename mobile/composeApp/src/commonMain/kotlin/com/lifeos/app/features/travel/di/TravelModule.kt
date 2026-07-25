package com.lifeos.app.features.travel.di

import com.lifeos.app.features.travel.data.datasource.FakeTravelDataSource
import com.lifeos.app.features.travel.data.datasource.FakeTripGenerationDataSource
import com.lifeos.app.features.travel.data.remote.TravelRemoteDataSource
import com.lifeos.app.features.travel.data.repository.FakeTripGenerationRepository
import com.lifeos.app.features.travel.data.repository.TravelRepositoryImpl
import com.lifeos.app.features.travel.domain.repository.TravelRepository
import com.lifeos.app.features.travel.domain.repository.TripGenerationRepository
import com.lifeos.app.features.planner.domain.usecase.GetPlannerDashboardUseCase
import com.lifeos.app.features.travel.domain.usecase.DeleteTripUseCase
import com.lifeos.app.features.travel.domain.usecase.GenerateTripUseCase
import com.lifeos.app.features.travel.domain.usecase.GetTravelListUseCase
import com.lifeos.app.features.travel.domain.usecase.GetTripDetailUseCase
import com.lifeos.app.features.travel.domain.usecase.SaveTripUseCase
import com.lifeos.app.features.travel.domain.usecase.UpdateTripUseCase
import com.lifeos.app.features.travel.presentation.CreateTripViewModel
import com.lifeos.app.features.travel.presentation.TravelDetailViewModel
import com.lifeos.app.features.travel.presentation.TravelViewModel
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/**
 * Travel's Koin module — covers both Travel List and Travel Detail, per
 * this task's "extend the existing Travel module" scope, following the
 * same shape as `features/auth/di/AuthModule.kt` and
 * `features/home/di/HomeModule.kt`. The single line to swap when a real
 * backend exists: `single<TravelRepository> { FakeTravelRepository(get()) }`
 * — see [FakeTravelRepository]'s KDoc for the replacement plan.
 *
 * [TravelDetailViewModel] takes its `tripId` as a Koin runtime parameter
 * (supplied via `parametersOf(tripId)` at the `koinViewModel()` call site
 * in `TravelDetailScreen.kt`) — the same way a `NavHost` hands a route
 * argument to whichever screen owns it, just resolved through DI instead
 * of a constructor call. It also resolves [GetPlannerDashboardUseCase] —
 * already registered by `features/planner/di/PlannerModule.kt`, which
 * Koin's single shared container makes resolvable here with no extra
 * wiring (this sprint's Travel-consumes-Planner integration, the same
 * pattern `features/home/di/HomeModule.kt` already established; no new
 * Planner use case needed).
 *
 * [TripGenerationRepository] is bound separately from [TravelRepository] —
 * see [FakeTripGenerationRepository]'s KDoc for why "Create Travel (AI)"
 * keeps its own repository rather than adding one more method to
 * [TravelRepository]. It stays fake/unchanged this iteration (Iteration 3
 * of the backend integration migration only replaces [TravelRepository]'s
 * implementation) — [FakeTripGenerationRepository] still depends on
 * [FakeTravelDataSource] for a client-side draft id, which is why that
 * data source is kept even though [TravelRepositoryImpl] no longer uses it.
 */
val travelModule: Module = module {
    single { FakeTravelDataSource() }
    single { FakeTripGenerationDataSource() }
    single { TravelRemoteDataSource(httpClient = get<HttpClient>()) }
    single<TravelRepository> { TravelRepositoryImpl(remoteDataSource = get()) }
    single<TripGenerationRepository> {
        FakeTripGenerationRepository(dataSource = get(), travelDataSource = get())
    }
    factory { GetTravelListUseCase(travelRepository = get()) }
    factory { GetTripDetailUseCase(travelRepository = get()) }
    factory { SaveTripUseCase(travelRepository = get()) }
    factory { UpdateTripUseCase(travelRepository = get()) }
    factory { DeleteTripUseCase(travelRepository = get()) }
    factory { GenerateTripUseCase(tripGenerationRepository = get()) }
    viewModel { TravelViewModel(getTravelList = get()) }
    viewModel { (tripId: String) ->
        TravelDetailViewModel(tripId = tripId, getTripDetail = get(), getPlannerDashboard = get())
    }
    viewModel { CreateTripViewModel(generateTrip = get(), saveTrip = get()) }
}
