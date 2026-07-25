package com.lifeos.app.core.util

import java.util.Calendar

actual fun currentHourOfDay(): Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
