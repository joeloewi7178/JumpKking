package com.joeloewi.jumpkking.di

import com.joeloewi.domain.repository.FirebaseAuthRepository
import com.joeloewi.domain.repository.ReportCardRepository
import com.joeloewi.domain.repository.ValuesRepository
import com.joeloewi.domain.usecase.FirebaseAuthUseCase
import com.joeloewi.domain.usecase.ReportCardUseCase
import com.joeloewi.domain.usecase.ValuesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetValuesUseCase(valuesRepository: ValuesRepository): ValuesUseCase.GetValues =
        ValuesUseCase.GetValues(valuesRepository)

    @Provides
    @Singleton
    fun provideSetJumpCountValuesUseCase(valuesRepository: ValuesRepository): ValuesUseCase.SetJumpCount =
        ValuesUseCase.SetJumpCount(valuesRepository)

    @Provides
    @Singleton
    fun provideGetAllPagedReportCardUseCase(reportCardRepository: ReportCardRepository): ReportCardUseCase.GetAllPaged =
        ReportCardUseCase.GetAllPaged(reportCardRepository)

    @Provides
    @Singleton
    fun provideInsertReportCardUseCase(reportCardRepository: ReportCardRepository): ReportCardUseCase.Insert =
        ReportCardUseCase.Insert(reportCardRepository)

    @Provides
    @Singleton
    fun provideGetOneReportCardUseCase(reportCardRepository: ReportCardRepository): ReportCardUseCase.GetOne =
        ReportCardUseCase.GetOne(reportCardRepository)

    @Provides
    @Singleton
    fun provideGetCurrentUserFirebaseAuthUseCase(firebaseAuthRepository: FirebaseAuthRepository): FirebaseAuthUseCase.GetCurrentUser =
        FirebaseAuthUseCase.GetCurrentUser(firebaseAuthRepository)

    @Provides
    @Singleton
    fun provideSignInAnonymouslyFirebaseAuthUseCase(firebaseAuthRepository: FirebaseAuthRepository): FirebaseAuthUseCase.SignInAnonymously =
        FirebaseAuthUseCase.SignInAnonymously(firebaseAuthRepository)
}