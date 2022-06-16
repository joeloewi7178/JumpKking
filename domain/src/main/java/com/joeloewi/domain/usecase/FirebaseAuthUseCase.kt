package com.joeloewi.domain.usecase

import com.joeloewi.domain.repository.FirebaseAuthRepository

sealed class FirebaseAuthUseCase {
    class GetCurrentUser constructor(
        private val firebaseAuthRepository: FirebaseAuthRepository
    ) : FirebaseAuthUseCase() {
        operator fun invoke() = firebaseAuthRepository.getCurrentUser()
    }

    class SignInAnonymously constructor(
        private val firebaseAuthRepository: FirebaseAuthRepository
    ) : FirebaseAuthUseCase() {
        suspend operator fun invoke() = firebaseAuthRepository.signInAnonymously()
    }
}
