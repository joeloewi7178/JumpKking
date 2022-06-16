package com.joeloewi.data.repository.remote.impl

import com.google.firebase.auth.FirebaseAuth
import com.joeloewi.data.repository.remote.FirebaseAuthDataSource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseAuthDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val coroutineDispatcher: CoroutineDispatcher
) : FirebaseAuthDataSource {
    override fun getCurrentUser(): Any? = firebaseAuth.currentUser

    override suspend fun signInAnonymously(): Any = withContext(coroutineDispatcher) {
        firebaseAuth.signInAnonymously().await()
    }
}