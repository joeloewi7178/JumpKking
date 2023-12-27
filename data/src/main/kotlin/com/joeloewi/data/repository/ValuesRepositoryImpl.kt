package com.joeloewi.data.repository

import com.joeloewi.data.repository.local.ValuesDataSource
import com.joeloewi.domain.entity.Values
import com.joeloewi.domain.repository.ValuesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ValuesRepositoryImpl @Inject constructor(
    private val valuesDataSource: ValuesDataSource
) : ValuesRepository {

    override fun getValues(): Flow<Values> = valuesDataSource.getValues()

    override suspend fun setJumpCount(jumpCount: Long): Values =
        valuesDataSource.setJumpCount(jumpCount)
}