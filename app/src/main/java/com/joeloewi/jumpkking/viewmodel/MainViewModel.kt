package com.joeloewi.jumpkking.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.joeloewi.domain.entity.ReportCard
import com.joeloewi.domain.entity.Values
import com.joeloewi.domain.usecase.FirebaseAuthUseCase
import com.joeloewi.domain.usecase.ReportCardUseCase
import com.joeloewi.domain.usecase.ValuesUseCase
import com.joeloewi.jumpkking.state.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.asFlowable
import kotlinx.coroutines.rx3.asScheduler
import kotlinx.coroutines.withContext
import nl.marc_apps.tts.TextToSpeech
import okhttp3.internal.closeQuietly
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val application: Application,
    getValuesUseCase: ValuesUseCase.GetValues,
    private val setJumpCountUseCase: ValuesUseCase.SetJumpCount,
    private val setReportCardUseCase: ReportCardUseCase.Insert,
    getAllPagedReportCardUseCase: ReportCardUseCase.GetAllPaged,
    private val getOneReportCardUseCase: ReportCardUseCase.GetOne,
    val androidId: String,
    private val getCurrentUserFirebaseAuthUseCase: FirebaseAuthUseCase.GetCurrentUser,
    private val signInAnonymouslyFirebaseAuthUseCase: FirebaseAuthUseCase.SignInAnonymously
) : ViewModel() {
    private val _ioScheduler = Dispatchers.IO.asScheduler()
    private val _values = getValuesUseCase()
    private val _jumpCount = _values.map { it.jumpCount }
    private val _reportCard = _jumpCount
        .flowOn(Dispatchers.IO)
        .asFlowable(viewModelScope.coroutineContext)
        .observeOn(_ioScheduler)
        .throttleLatest(5, TimeUnit.SECONDS)
        .subscribeOn(_ioScheduler)
        .asFlow()

    val currentUser = flow {
        signInAnonymouslyFirebaseAuthUseCase.runCatching {
            getCurrentUserFirebaseAuthUseCase() ?: invoke()
        }.fold(
            onSuccess = {
                setJumpCountIfExistsInServer(_values.first().jumpCount)
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
    val insertReportCardState =
        combine(_reportCard, currentUser) { reportCard, currentUser -> reportCard to currentUser }
            .filter { it.second is Lce.Content }
            .map { it.first }
            .map {
                setReportCardUseCase.runCatching {
                    invoke(
                        ReportCard(
                            androidId = androidId,
                            jumpCount = it
                        )
                    )
                }.fold(
                    onSuccess = {
                        Lce.Content(it)
                    },
                    onFailure = {
                        FirebaseCrashlytics.getInstance().recordException(it)
                        Lce.Error(it)
                    }
                )
            }.flowOn(Dispatchers.IO).stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(),
                initialValue = Lce.Loading
            )
    val jumpCount = _jumpCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Values().jumpCount
    )
    val pagedReportCards = getAllPagedReportCardUseCase().cachedIn(viewModelScope)
    val textToSpeech = callbackFlow {
        val textToSpeechInstance = TextToSpeech.runCatching {
            createOrThrow(application)
        }.fold(
            onSuccess = {
                Lce.Content(it)
            },
            onFailure = {
                Lce.Error(it)
            }
        )

        trySend(textToSpeechInstance)

        awaitClose { textToSpeechInstance.content?.closeQuietly() }
    }.catch { cause ->
        FirebaseCrashlytics.getInstance().recordException(cause)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Lce.Loading
    )

    private suspend fun setJumpCountIfExistsInServer(
        currentJumpCount: Long
    ) = withContext(Dispatchers.IO) {
        getOneReportCardUseCase.runCatching {
            invoke()
        }.onSuccess {
            if (currentJumpCount == 0L && it != null) {
                setJumpCount(it.jumpCount)
            }
        }.onFailure { cause ->
            FirebaseCrashlytics.getInstance().recordException(cause)
        }
    }

    fun setJumpCount(jumpCount: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            setJumpCountUseCase(jumpCount)
        }
    }
}