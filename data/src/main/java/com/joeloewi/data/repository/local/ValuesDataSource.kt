package com.joeloewi.data.repository.local

import com.joeloewi.domain.entity.Values
import kotlinx.coroutines.flow.Flow

interface ValuesDataSource {
    fun getValues(): Flow<Values>
    suspend fun setJumpCount(jumpCount: Long): Values
}