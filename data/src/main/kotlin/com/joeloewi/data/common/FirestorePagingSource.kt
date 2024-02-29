package com.joeloewi.data.common

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class FirestorePagingSource(
    private val query: Query,
    private val source: Source
) : PagingSource<PageKey, DocumentSnapshot>() {

    override val jumpingSupported: Boolean = true

    override suspend fun load(params: LoadParams<PageKey>): LoadResult<PageKey, DocumentSnapshot> {
        return try {
            val key = params.key
            val documentsSnapshots = (key?.getPageQuery(query, params.loadSize)?.get(source)
                ?: query.limit(params.loadSize.toLong())[source]).await().documents
            val nextKey = PageKey(documentsSnapshots.lastOrNull(), null)
            val prevKey = PageKey(null, documentsSnapshots.firstOrNull())
            val original = query.get(source).await()

            LoadResult.Page(
                data = documentsSnapshots,
                prevKey = if (key == null) {
                    null
                } else {
                    prevKey
                },
                nextKey = if (documentsSnapshots.isEmpty()) {
                    null
                } else {
                    nextKey
                },
                itemsBefore = key?.endBefore?.let {
                    original.indexOf(it)
                } ?: 0,
                itemsAfter = key?.startAfter?.let {
                    original.reversed().indexOf(it)
                } ?: 0
            )
        } catch (cause: Throwable) {
            LoadResult.Error(cause)
        }
    }

    override fun getRefreshKey(state: PagingState<PageKey, DocumentSnapshot>): PageKey? = null
}