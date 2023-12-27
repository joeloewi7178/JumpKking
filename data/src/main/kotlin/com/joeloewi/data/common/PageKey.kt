package com.joeloewi.data.common

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

data class PageKey(
    val startAfter: DocumentSnapshot?,
    val endBefore: DocumentSnapshot?
) {
    fun getPageQuery(baseQuery: Query, size: Int): Query {
        var pageQuery = baseQuery
        if (startAfter != null) {
            pageQuery = pageQuery.startAfter(startAfter)
        }
        pageQuery = if (endBefore != null) {
            pageQuery.endBefore(endBefore)
        } else {
            pageQuery.limit(size.toLong())
        }
        return pageQuery
    }
}