package com.joeloewi.jumpkking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.joeloewi.domain.usecase.ReportCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RankingViewModel @Inject constructor(
    getAllPagedReportCardUseCase: ReportCardUseCase.GetAllPaged,
    val androidId: String,
) : ViewModel() {
    val pagedReportCards = getAllPagedReportCardUseCase().cachedIn(viewModelScope)
}