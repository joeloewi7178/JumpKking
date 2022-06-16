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
            val documentsSnapshots = (params.key?.let {
                it.getPageQuery(query, params.loadSize)[source]
            } ?: query.limit(params.loadSize.toLong())[source]).await().documents
            val nextKey = PageKey(documentsSnapshots.lastOrNull(), null)

            if (params is LoadParams.Refresh) {
                LoadResult.Page(
                    data = documentsSnapshots,
                    prevKey = null,
                    nextKey = if (documentsSnapshots.isEmpty()) null else nextKey,
                )
            } else {
                LoadResult.Page(
                    data = documentsSnapshots,
                    prevKey = null,
                    nextKey = if (documentsSnapshots.isEmpty() && params is LoadParams.Append) null else nextKey,
                )
            }
        } catch (cause: Throwable) {
            LoadResult.Error(cause)
        }
    }

    override fun getRefreshKey(state: PagingState<PageKey, DocumentSnapshot>): PageKey? =
        state.anchorPosition?.let { anchorPosition ->
            val anchorPageIndex = state.pages.indexOf(state.closestPageToPosition(anchorPosition))
            state.pages.getOrNull(anchorPageIndex + 1)?.prevKey ?: state.pages.getOrNull(
                anchorPageIndex - 1
            )?.nextKey
        }
}