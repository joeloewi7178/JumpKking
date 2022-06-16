package com.joeloewi.data.common

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

class PageKey(
    private val startAfter: DocumentSnapshot?,
    private val endBefore: DocumentSnapshot?
) {
    fun getPageQuery(baseQuery: Query, size: Int? = null): Query {
        var pageQuery = baseQuery
        if (startAfter != null) {
            pageQuery = pageQuery.startAfter(startAfter)
        }
        pageQuery = if (endBefore != null) {
            pageQuery.endBefore(endBefore)
        } else {
            size?.let {
                pageQuery.limit(it.toLong())
            } ?: pageQuery
        }
        return pageQuery
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val key = other as PageKey
        if (startAfter == null && key.startAfter == null) return true
        return if (endBefore == null && key.endBefore == null) true else startAfter!!.id === key.startAfter!!.id &&
                endBefore!!.id === key.endBefore!!.id
    }

    override fun toString(): String {
        val startAfter = startAfter?.id
        val endBefore = endBefore?.id
        return "PageKey{" +
                "StartAfter=" + startAfter +
                ", EndBefore=" + endBefore +
                '}'
    }

    override fun hashCode(): Int {
        var result = startAfter?.hashCode() ?: 0
        result = 31 * result + (endBefore?.hashCode() ?: 0)
        return result
    }
}