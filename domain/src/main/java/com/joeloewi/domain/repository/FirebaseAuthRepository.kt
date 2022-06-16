package com.joeloewi.domain.repository

interface FirebaseAuthRepository {
    fun getCurrentUser(): Any?
    suspend fun signInAnonymously(): Any
}