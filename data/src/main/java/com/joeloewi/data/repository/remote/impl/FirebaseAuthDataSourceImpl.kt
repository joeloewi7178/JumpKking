package com.joeloewi.data.repository.remote.impl

import com.google.firebase.auth.FirebaseAuth
import com.joeloewi.data.repository.remote.FirebaseAuthDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) : FirebaseAuthDataSource {
    override fun getCurrentUser(): Any? = firebaseAuth.currentUser

    override suspend fun signInAnonymously(): Any = firebaseAuth.signInAnonymously().await()
}