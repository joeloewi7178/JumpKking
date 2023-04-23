package com.joeloewi.domain.entity

import java.util.Calendar
import java.util.Date

data class ReportCard(
    val androidId: String = "",
    val jumpCount: Long = 0,
    val timestamp: Date = Calendar.getInstance().time
)
