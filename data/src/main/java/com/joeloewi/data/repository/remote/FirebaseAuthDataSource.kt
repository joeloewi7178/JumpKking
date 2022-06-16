package com.joeloewi.data.repository.remote

interface FirebaseAuthDataSource {
    fun getCurrentUser(): Any?
    suspend fun signInAnonymously(): Any
}