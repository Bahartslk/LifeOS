package com.lifeos.app.features.profile.domain.usecase

import com.lifeos.app.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.TimeZone

/**
 * Keeps the backend's time zone in step with the device's. The backend uses
 * it to decide where "today" starts, e.g. for the daily chat limit.
 * Best effort: the backend accepts any IANA name (including "UTC" and
 * "GMT") and rejects the rest, such as a custom "GMT+03:00" id, and a
 * rejected or failed update changes nothing on the device.
 */
class SyncDeviceTimezoneUseCase(
    private val profileRepository: ProfileRepository,
    private val deviceTimezone: () -> String = { TimeZone.currentSystemDefault().id },
) {
    suspend operator fun invoke(storedTimezone: String) {
        val timezone = deviceTimezone()
        if (timezone == storedTimezone) return
        try {
            profileRepository.updateTimezone(timezone)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }
}
