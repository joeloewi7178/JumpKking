package com.joeloewi.data.entity

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class ReportCardEntity(
    val jumpCount: Long = 0,
    @ServerTimestamp
    val timestamp: Date = Calendar.getInstance().time
)
