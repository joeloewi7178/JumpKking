package com.joeloewi.data.repository

import com.joeloewi.data.repository.remote.FirebaseAuthDataSource
import com.joeloewi.domain.repository.FirebaseAuthRepository
import javax.inject.Inject

class FirebaseAuthRepositoryImpl @Inject constructor(
    private val firebaseAuthDataSource: FirebaseAuthDataSource
) : FirebaseAuthRepository {
    override fun getCurrentUser(): Any? = firebaseAuthDataSource.getCurrentUser()

    override suspend fun signInAnonymously(): Any = firebaseAuthDataSource.signInAnonymously()
}