package com.joeloewi.data.repository.remote

import androidx.paging.PagingData
import com.google.firebase.firestore.Source
import com.joeloewi.domain.entity.ReportCard
import kotlinx.coroutines.flow.Flow

interface ReportCardDataSource {
    fun getAllPaged(source: Source = Source.DEFAULT): Flow<PagingData<ReportCard>>
    suspend fun insertReportCard(reportCard: ReportCard): Void?
    suspend fun getReportCard(): ReportCard?
}