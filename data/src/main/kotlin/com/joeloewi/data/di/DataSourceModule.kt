package com.joeloewi.data.di

import com.joeloewi.data.repository.local.ValuesDataSource
import com.joeloewi.data.repository.local.impl.ValuesDataSourceImpl
import com.joeloewi.data.repository.remote.FirebaseAuthDataSource
import com.joeloewi.data.repository.remote.ReportCardDataSource
import com.joeloewi.data.repository.remote.impl.FirebaseAuthDataSourceImpl
import com.joeloewi.data.repository.remote.impl.ReportCardDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataSourceModule {

    @Binds
    @Singleton
    fun bindValuesDataSource(valuesDataSourceImpl: ValuesDataSourceImpl): ValuesDataSource

    @Binds
    @Singleton
    fun bindReportCardDataSource(reportCardDataSourceImpl: ReportCardDataSourceImpl): ReportCardDataSource

    @Binds
    @Singleton
    fun bindFirebaseAuthDataSource(firebaseAuthDataSourceImpl: FirebaseAuthDataSourceImpl): FirebaseAuthDataSource
}