package com.joeloewi.data.di

import com.joeloewi.data.repository.FirebaseAuthRepositoryImpl
import com.joeloewi.data.repository.ReportCardRepositoryImpl
import com.joeloewi.data.repository.ValuesRepositoryImpl
import com.joeloewi.domain.repository.FirebaseAuthRepository
import com.joeloewi.domain.repository.ReportCardRepository
import com.joeloewi.domain.repository.ValuesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindValuesRepository(valuesRepositoryImpl: ValuesRepositoryImpl): ValuesRepository

    @Binds
    @Singleton
    fun bindReportCardRepository(reportCardRepositoryImpl: ReportCardRepositoryImpl): ReportCardRepository

    @Binds
    @Singleton
    fun bindFirebaseAuthRepository(firebaseAuthRepositoryImpl: FirebaseAuthRepositoryImpl): FirebaseAuthRepository
}