package com.joeloewi.data.repository.remote.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.toObject
import com.joeloewi.data.common.FirestorePagingSource
import com.joeloewi.data.entity.ReportCardEntity
import com.joeloewi.data.mapper.ReportCardMapper
import com.joeloewi.data.repository.remote.ReportCardDataSource
import com.joeloewi.domain.entity.ReportCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReportCardDataSourceImpl @Inject constructor(
    private val firestoreReportCardCollection: CollectionReference,
    private val reportCardMapper: ReportCardMapper,
    private val androidId: String
) : ReportCardDataSource {
    override fun getAllPaged(source: Source): Flow<PagingData<ReportCard>> = Pager(
        config = PagingConfig(
            pageSize = 8
        ),
        pagingSourceFactory = {
            FirestorePagingSource(
                firestoreReportCardCollection.orderBy(
                    "jumpCount",
                    Query.Direction.DESCENDING
                ).orderBy(
                    "timestamp",
                    Query.Direction.ASCENDING
                ),
                source
            )
        }
    ).flow.map { pagingData ->
        pagingData.map {
            reportCardMapper.toDomain(it.toObject<ReportCardEntity>()!!)
        }
    }.catch { }.flowOn(Dispatchers.IO)

    override suspend fun insertReportCard(reportCard: ReportCard): Void? =
        withContext(Dispatchers.IO) {
            firestoreReportCardCollection.document(androidId)
                .set(reportCardMapper.toData(reportCard)).await()
        }

    override suspend fun getReportCard() = withContext(Dispatchers.IO) {
        firestoreReportCardCollection.document(androidId).get().await().toObject<ReportCardEntity>()
            ?.let {
                reportCardMapper.toDomain(it)
            }
    }
}