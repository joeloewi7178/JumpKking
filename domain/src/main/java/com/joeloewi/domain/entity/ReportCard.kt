package com.joeloewi.domain.entity

import java.util.*

data class ReportCard(
    val jumpCount: Long = 0,
    val timestamp: Date = Calendar.getInstance().time
)
