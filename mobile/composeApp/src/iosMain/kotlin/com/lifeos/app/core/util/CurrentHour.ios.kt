package com.lifeos.app.core.util

import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSDate

actual fun currentHourOfDay(): Int {
    val components = NSCalendar.currentCalendar.components(NSCalendarUnitHour, NSDate())
    return components.hour.toInt()
}
