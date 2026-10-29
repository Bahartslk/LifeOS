package com.lifeos.app.features.profile.domain.usecase

import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.TimeZone

/**
 * Keeps the backend's time zone in step with the device's. The backend uses
 * it to decide where "today" starts, e.g. for the daily chat limit.
 * Best effort, and skipped for ids the backend doesn't accept (e.g. "UTC" or
 * a fixed offset; it expects an IANA "Area/Location" name).
 */
class SyncDeviceTimezoneUseCase(
    private val profileRepository: ProfileRepository,
    private val deviceTimezone: () -> String = { TimeZone.currentSystemDefault().id },
) {
    suspend operator fun invoke(storedTimezone: String) {
        val timezone = deviceTimezone()
        if (timezone == storedTimezone || !IANA_AREA_LOCATION.matches(timezone)) return
        try {
            profileRepository.updateTimezone(timezone)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    private companion object {
        // Mirrors the backend's TIMEZONE_PATTERN (preference-fields.dto.ts).
        val IANA_AREA_LOCATION = Regex("^[A-Za-z_]+/[A-Za-z_]+$")
    }
}
