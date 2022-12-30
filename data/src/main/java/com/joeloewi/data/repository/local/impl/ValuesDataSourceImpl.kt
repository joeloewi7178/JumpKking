package com.joeloewi.data.repository.local.impl

import android.app.Application
import com.joeloewi.data.datastore.valuesDataStore
import com.joeloewi.data.mapper.ValuesMapper
import com.joeloewi.data.repository.local.ValuesDataSource
import com.joeloewi.domain.entity.Values
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ValuesDataSourceImpl @Inject constructor(
    private val application: Application,
    private val valuesMapper: ValuesMapper
) : ValuesDataSource {
    override fun getValues(): Flow<Values> =
        application.valuesDataStore.data.map { valuesMapper.toDomain(it) }

    override suspend fun setJumpCount(jumpCount: Long): Values =
        application.valuesDataStore.updateData {
            it.toBuilder().setJumpCount(jumpCount).build()
        }.let {
            valuesMapper.toDomain(it)
        }
}