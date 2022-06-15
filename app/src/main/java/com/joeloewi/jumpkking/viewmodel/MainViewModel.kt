package com.joeloewi.jumpkking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joeloewi.domain.entity.Values
import com.joeloewi.domain.usecase.ValuesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getValuesUseCase: ValuesUseCase.GetValues,
    private val setJumpCountUseCase: ValuesUseCase.SetJumpCount
) : ViewModel() {
    val jumpCount = getValuesUseCase().map { it.jumpState }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Values().jumpState
    )

    fun setJumpCount(jumpCount: Long) {
        viewModelScope.launch {
            setJumpCountUseCase(jumpCount)
        }
    }
}