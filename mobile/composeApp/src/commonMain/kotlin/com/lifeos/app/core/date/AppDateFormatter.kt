package com.lifeos.app.core.date

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Turkish-locale date/time formatting primitives, shared by every feature
 * that displays a [LocalDate]/[LocalTime] — this app has no per-platform
 * locale-aware date formatter available in common code (no `java.text` on
 * iOS), so Turkish month/weekday names are plain lookup tables here rather
 * than a system locale API, the same "no date library, hand-roll the
 * Turkish vocabulary" approach [com.lifeos.app.features.planner.data.datasource.FakePlannerDataSource]
 * used for the Planner Calendar grid before this refactor consolidated it
 * here for reuse by Home and Travel too.
 *
 * These are context-free formatting primitives only — no "is this due
 * date near, so show it differently" logic. That smarter, relative
 * decision belongs to [RelativeDateFormatter], built on top of these.
 */
object AppDateFormatter {

    /** "24 Ekim Perşembe" — day + Turkish month name + Turkish weekday name, no year. Planner Dashboard's header shape; Home's future dashboard header can reuse it unchanged. */
    fun toDayMonthWeekdayLabel(date: LocalDate): String =
        "${date.dayOfMonth} ${monthName(date)} ${weekdayName(date)}"

    /** "18 Ekim 2024" — day + Turkish month name + year. Used wherever a date needs to stand fully on its own (e.g. Task Detail's Activity Timeline). */
    fun toFullLabel(date: LocalDate): String =
        "${date.dayOfMonth} ${monthName(date)} ${date.year}"

    /** "12 Kasım" — day + Turkish month name, no year. Used for a due date far enough away that a bare day+month reads unambiguously. */
    fun toShortLabel(date: LocalDate): String =
        "${date.dayOfMonth} ${monthName(date)}"

    /** "Pazartesi" — the Turkish weekday name alone. */
    fun toWeekdayLabel(date: LocalDate): String = weekdayName(date)

    /** "14:00" — zero-padded 24-hour clock. */
    fun toTimeLabel(time: LocalTime): String =
        "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"

    /** "Ekim 2024" — Turkish month name + year, no day. The Planner Calendar grid's month header (a month has no single "day" of its own). */
    fun toMonthYearLabel(year: Int, monthNumber: Int): String = "${TURKISH_MONTH_NAMES[monthNumber - 1]} $year"

    /** Monday-first Turkish weekday abbreviations ("Pt, Sa, Ça, Pe, Cu, Ct, Pa") — the Planner Calendar grid's column headers. */
    val weekdayShortLabels: List<String> = listOf("Pt", "Sa", "Ça", "Pe", "Cu", "Ct", "Pa")

    private fun monthName(date: LocalDate): String = TURKISH_MONTH_NAMES[date.monthNumber - 1]

    private fun weekdayName(date: LocalDate): String = requireNotNull(TURKISH_WEEKDAY_NAMES[date.dayOfWeek])

    /** Case-insensitive reverse lookup for [AppDateParser] — "pazartesi" -> [DayOfWeek.MONDAY]. */
    internal fun weekdayFromTurkishName(name: String): DayOfWeek? =
        TURKISH_WEEKDAY_NAMES.entries.find { it.value.equals(name, ignoreCase = true) }?.key

    /** Case-insensitive reverse lookup for [AppDateParser] — "kasım" -> 11. */
    internal fun monthNumberFromTurkishName(name: String): Int? {
        val index = TURKISH_MONTH_NAMES.indexOfFirst { it.equals(name, ignoreCase = true) }
        return if (index == -1) null else index + 1
    }

    private val TURKISH_MONTH_NAMES = listOf(
        "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
        "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık",
    )

    private val TURKISH_WEEKDAY_NAMES = mapOf(
        DayOfWeek.MONDAY to "Pazartesi",
        DayOfWeek.TUESDAY to "Salı",
        DayOfWeek.WEDNESDAY to "Çarşamba",
        DayOfWeek.THURSDAY to "Perşembe",
        DayOfWeek.FRIDAY to "Cuma",
        DayOfWeek.SATURDAY to "Cumartesi",
        DayOfWeek.SUNDAY to "Pazar",
    )
}
