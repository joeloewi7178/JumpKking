package com.joeloewi.domain.usecase

import com.joeloewi.domain.repository.ValuesRepository
import javax.inject.Inject

sealed class ValuesUseCase {
    class GetValues @Inject constructor(
        private val valuesRepository: ValuesRepository
    ) : ValuesUseCase() {
        operator fun invoke() = valuesRepository.getValues()
    }

    class SetJumpCount @Inject constructor(
        private val valuesRepository: ValuesRepository
    ) : ValuesUseCase() {
        suspend operator fun invoke(jumpCount: Long) = valuesRepository.setJumpCount(jumpCount)
    }
}
