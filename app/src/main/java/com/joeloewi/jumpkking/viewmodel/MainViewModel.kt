package com.joeloewi.jumpkking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.joeloewi.domain.usecase.FirebaseAuthUseCase
import com.joeloewi.domain.usecase.ReportCardUseCase
import com.joeloewi.domain.usecase.ValuesUseCase
import com.joeloewi.jumpkking.state.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getValuesUseCase: ValuesUseCase.GetValues,
    private val setJumpCountUseCase: ValuesUseCase.SetJumpCount,
    private val getOneReportCardUseCase: ReportCardUseCase.GetOne,
    private val getCurrentUserFirebaseAuthUseCase: FirebaseAuthUseCase.GetCurrentUser,
    private val signInAnonymouslyFirebaseAuthUseCase: FirebaseAuthUseCase.SignInAnonymously
) : ViewModel() {
    val currentUser = flow {
        signInAnonymouslyFirebaseAuthUseCase.runCatching {
            getCurrentUserFirebaseAuthUseCase() ?: invoke()
        }.fold(
            onSuccess = {
                setJumpCountIfExistsInServer(getValuesUseCase().first().jumpCount)
                Lce.Content(it)
            },
            onFailure = {
                Lce.Error(it)
            }
        ).let {
            emit(it)
        }
    }.flowOn(Dispatchers.IO).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = Lce.Loading
    )

    private suspend fun setJumpCountIfExistsInServer(
        currentJumpCount: Long
    ) = withContext(Dispatchers.IO) {
        getOneReportCardUseCase.runCatching {
            invoke()
        }.onSuccess {
            if (currentJumpCount == 0L && it != null) {
                setJumpCountUseCase(it.jumpCount)
            }
        }.onFailure { cause ->
            if (cause is CancellationException) {
                throw cause
            }

            FirebaseCrashlytics.getInstance().recordException(cause)
        }
    }
}