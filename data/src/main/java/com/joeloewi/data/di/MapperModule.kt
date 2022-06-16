package com.joeloewi.data.di

import com.joeloewi.data.mapper.ReportCardMapper
import com.joeloewi.data.mapper.ValuesMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MapperModule {

    @Singleton
    @Provides
    fun provideValuesMapper(): ValuesMapper = ValuesMapper()

    @Singleton
    @Provides
    fun provideReportCardMapper(): ReportCardMapper = ReportCardMapper()
}