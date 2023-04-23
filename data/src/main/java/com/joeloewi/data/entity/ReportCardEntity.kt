package com.joeloewi.data.entity

import com.google.firebase.firestore.ServerTimestamp
import java.time.ZonedDateTime
import java.util.Date

data class ReportCardEntity(
    val androidId: String = "",
    val jumpCount: Long = 0,
    @ServerTimestamp
    val timestamp: Date = Date(ZonedDateTime.now().toInstant().toEpochMilli())
)
