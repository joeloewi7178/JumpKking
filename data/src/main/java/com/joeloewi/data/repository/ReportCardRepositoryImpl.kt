package com.joeloewi.data.repository

import androidx.paging.PagingData
import com.joeloewi.data.repository.remote.ReportCardDataSource
import com.joeloewi.domain.entity.ReportCard
import com.joeloewi.domain.repository.ReportCardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReportCardRepositoryImpl @Inject constructor(
    private val reportCardDataSource: ReportCardDataSource
) : ReportCardRepository {
    override fun getAllPaged(): Flow<PagingData<ReportCard>> = reportCardDataSource.getAllPaged()

    override suspend fun insertReportCard(reportCard: ReportCard) = reportCardDataSource.insertReportCard(reportCard)

    override suspend fun getReportCard(): ReportCard? = reportCardDataSource.getReportCard()
}