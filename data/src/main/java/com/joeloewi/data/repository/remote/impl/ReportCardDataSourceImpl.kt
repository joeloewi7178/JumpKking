package com.joeloewi.data.repository.remote.impl

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import com.joeloewi.data.entity.ReportCardEntity
import com.joeloewi.data.mapper.ReportCardMapper
import com.joeloewi.data.repository.remote.ReportCardDataSource
import com.joeloewi.domain.entity.ReportCard
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReportCardDataSourceImpl @Inject constructor(
    private val coroutineDispatcher: CoroutineDispatcher,
    private val firestoreReportCardCollection: CollectionReference,
    private val reportCardMapper: ReportCardMapper,
    private val androidId: String
) : ReportCardDataSource {
    override fun getAllPaged(source: Source): Flow<PagingData<ReportCard>> = Pager(
        config = PagingConfig(
            pageSize = 8
        ),
        pagingSourceFactory = {
            com.firebase.ui.firestore.paging.FirestorePagingSource(
                firestoreReportCardCollection.orderBy(
                    "jumpCount",
                    Query.Direction.DESCENDING
                ).orderBy(
                    "timestamp",
                    Query.Direction.ASCENDING
                ).limit(100),
                source
            )
        }
    ).flow.map { pagingData ->
        pagingData.map {
            reportCardMapper.toDomain(it.toObject<ReportCardEntity>()!!)
        }
    }.flowOn(coroutineDispatcher).catch { it.printStackTrace() }

    override suspend fun insertReportCard(reportCard: ReportCard): Unit =
        withContext(coroutineDispatcher) {
            firestoreReportCardCollection.document(androidId)
                .set(reportCardMapper.toData(reportCard)).await()
        }

    override suspend fun getReportCard() = withContext(coroutineDispatcher) {
        firestoreReportCardCollection.document(androidId).get().await().toObject<ReportCardEntity>()
            ?.let {
                reportCardMapper.toDomain(it)
            }
    }
}