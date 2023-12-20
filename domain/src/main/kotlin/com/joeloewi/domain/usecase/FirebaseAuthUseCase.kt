package com.joeloewi.domain.usecase

import com.joeloewi.domain.repository.FirebaseAuthRepository
import javax.inject.Inject

sealed class FirebaseAuthUseCase {
    class GetCurrentUser @Inject constructor(
        private val firebaseAuthRepository: FirebaseAuthRepository
    ) : FirebaseAuthUseCase() {
        operator fun invoke() = firebaseAuthRepository.getCurrentUser()
    }

    class SignInAnonymously @Inject constructor(
        private val firebaseAuthRepository: FirebaseAuthRepository
    ) : FirebaseAuthUseCase() {
        suspend operator fun invoke() = firebaseAuthRepository.signInAnonymously()
    }
}