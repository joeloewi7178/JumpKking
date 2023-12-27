package com.joeloewi.data.repository.remote.impl

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.joeloewi.data.repository.remote.FirebaseAuthDataSource
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthDataSourceImpl @Inject constructor() : FirebaseAuthDataSource {
    override fun getCurrentUser(): Any? = Firebase.auth.currentUser

    override suspend fun signInAnonymously(): Any = Firebase.auth.signInAnonymously().await()
}