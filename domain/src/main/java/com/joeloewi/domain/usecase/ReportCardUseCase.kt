package com.joeloewi.domain.usecase

import com.joeloewi.domain.entity.ReportCard
import com.joeloewi.domain.repository.ReportCardRepository

sealed class ReportCardUseCase {
    class GetAllPaged constructor(
        private val reportCardRepository: ReportCardRepository
    ) : ReportCardUseCase() {
        operator fun invoke() = reportCardRepository.getAllPaged()
    }

    class Insert constructor(
        private val reportCardRepository: ReportCardRepository
    ) : ReportCardUseCase() {
        suspend operator fun invoke(reportCard: ReportCard) =
            reportCardRepository.insertReportCard(reportCard)
    }

    class GetOne constructor(
        private val reportCardRepository: ReportCardRepository
    ) : ReportCardUseCase() {
        suspend operator fun invoke() = reportCardRepository.getReportCard()
    }
}
