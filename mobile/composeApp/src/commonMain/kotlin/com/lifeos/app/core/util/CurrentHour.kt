package com.lifeos.app.core.util

/**
 * The device's current local hour of day (0..23). Used for the Home
 * greeting ("Günaydın" / "İyi günler" / "İyi akşamlar"), per this feature's
 * "dynamic greeting based on current time" requirement.
 *
 * A tiny expect/actual reading the real device clock directly via platform
 * calendar APIs (`java.util.Calendar` / `NSCalendar`) rather than routing
 * through `kotlinx-datetime` (added to this project for Planner's date
 * refactor — see [com.lifeos.app.core.date.AppToday]'s KDoc) — Planner's
 * own "today" is intentionally a fixed fake date, not the real device
 * date, so it cannot supply this, and a real device *hour* has no
 * `kotlinx-datetime` equivalent worth adding a `Clock`/`TimeZone` dependency
 * for just to read one `Int`.
 */
expect fun currentHourOfDay(): Int
