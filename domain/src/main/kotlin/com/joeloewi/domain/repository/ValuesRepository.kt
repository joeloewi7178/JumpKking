package com.joeloewi.domain.repository

import com.joeloewi.domain.entity.Values
import kotlinx.coroutines.flow.Flow

interface ValuesRepository {
    fun getValues(): Flow<Values>
    suspend fun setJumpCount(jumpCount: Long): Values
}