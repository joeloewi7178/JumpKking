package com.joeloewi.domain.usecase

import com.joeloewi.domain.entity.ReportCard
import com.joeloewi.domain.repository.ReportCardRepository
import javax.inject.Inject

sealed class ReportCardUseCase {
    class GetAllPaged @Inject constructor(
        private val reportCardRepository: ReportCardRepository
    ) : ReportCardUseCase() {
        operator fun invoke() = reportCardRepository.getAllPaged()
    }

    class Insert @Inject constructor(
        private val reportCardRepository: ReportCardRepository
    ) : ReportCardUseCase() {
        suspend operator fun invoke(reportCard: ReportCard) =
            reportCardRepository.insertReportCard(reportCard)
    }

    class GetOne @Inject constructor(
        private val reportCardRepository: ReportCardRepository
    ) : ReportCardUseCase() {
        suspend operator fun invoke() = reportCardRepository.getReportCard()
    }
}
