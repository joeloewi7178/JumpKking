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
            val lastItem = documentsSnapshots.lastOrNull()
            val firstItem = documentsSnapshots.firstOrNull()
            val nextKey = lastItem?.let { PageKey(it, null) }
            val prevKey = firstItem?.let { PageKey(null, it) }
            val original = query.get(source).await()

            LoadResult.Page(
                data = documentsSnapshots,
                prevKey = prevKey,
                nextKey = nextKey,
                itemsBefore = original.indexOfFirst { it == firstItem }.takeIf { it != -1 } ?: 0,
                itemsAfter = original.indexOfLast { it == lastItem }.takeIf { it != -1 } ?: 0,
            )
        } catch (cause: Throwable) {
            LoadResult.Error(cause)
        }
    }

    override fun getRefreshKey(state: PagingState<PageKey, DocumentSnapshot>): PageKey? =
        with(state) {
            anchorPosition?.let { closestPageToPosition(it)?.prevKey }
        }
}