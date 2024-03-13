package com.joeloewi.data.common

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class FirestorePagingSource(
    private val query: Query,
    private val source: Source
) : PagingSource<PageKey, DocumentSnapshot>() {

    override val jumpingSupported: Boolean = true
    override suspend fun load(params: LoadParams<PageKey>): LoadResult<PageKey, DocumentSnapshot> {
        return try {
            val key = params.key ?: PageKey(null, null)
            val documentsSnapshots =
                key.getPageQuery(query, params.loadSize).get(source).await().documents
            val lastItem = documentsSnapshots.lastOrNull()
            val firstItem = documentsSnapshots.firstOrNull()
            val nextKey = lastItem?.let { PageKey(it, null) }
            val prevKey = firstItem?.let { PageKey(null, it) }
            val itemsBefore = if (firstItem == null) {
                0
            } else {
                query.endBefore(firstItem).count().get(AggregateSource.SERVER)
                    .await().count.toInt()
            }
            val itemsAfter = if (lastItem == null) {
                0
            } else {
                query.startAfter(lastItem).count().get(AggregateSource.SERVER)
                    .await().count.toInt()
            }

            if (invalid) {
                return LoadResult.Invalid()
            }

            if (params is LoadParams.Refresh) {
                LoadResult.Page(
                    data = documentsSnapshots,
                    prevKey = if (documentsSnapshots.isEmpty()) null else prevKey,
                    nextKey = if (documentsSnapshots.isEmpty()) null else nextKey,
                    itemsBefore = itemsBefore,
                    itemsAfter = itemsAfter,
                )
            } else {
                LoadResult.Page(
                    data = documentsSnapshots,
                    prevKey = if (documentsSnapshots.isEmpty() && params is LoadParams.Prepend) null else prevKey,
                    nextKey = if (documentsSnapshots.isEmpty() && params is LoadParams.Append) null else nextKey,
                    itemsBefore = itemsBefore,
                    itemsAfter = itemsAfter,
                )
            }
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Throwable) {
            LoadResult.Error(cause)
        }
    }

    override fun getRefreshKey(state: PagingState<PageKey, DocumentSnapshot>): PageKey = PageKey(
        startAfter = null,
        endBefore = state.anchorPosition?.let { state.closestItemToPosition(it) }
    )
}