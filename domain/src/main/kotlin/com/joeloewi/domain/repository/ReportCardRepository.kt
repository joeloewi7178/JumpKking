package com.joeloewi.domain.repository

import androidx.paging.PagingData
import com.joeloewi.domain.entity.ReportCard
import kotlinx.coroutines.flow.Flow

interface ReportCardRepository {
    fun getAllPaged(): Flow<PagingData<ReportCard>>
    suspend fun insertReportCard(reportCard: ReportCard): Void?
    suspend fun getReportCard(): ReportCard?
}