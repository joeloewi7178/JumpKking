package com.joeloewi.domain.usecase

import com.joeloewi.domain.repository.ValuesRepository

sealed class ValuesUseCase {
    class GetValues constructor(
        private val valuesRepository: ValuesRepository
    ) : ValuesUseCase() {
        operator fun invoke() = valuesRepository.getValues()
    }

    class SetJumpCount constructor(
        private val valuesRepository: ValuesRepository
    ) : ValuesUseCase() {
        suspend operator fun invoke(jumpCount: Long) = valuesRepository.setJumpCount(jumpCount)
    }
}
