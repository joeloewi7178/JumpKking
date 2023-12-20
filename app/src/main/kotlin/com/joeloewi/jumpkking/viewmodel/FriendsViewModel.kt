package com.joeloewi.jumpkking.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.joeloewi.domain.entity.ReportCard
import com.joeloewi.domain.entity.Values
import com.joeloewi.domain.usecase.FirebaseAuthUseCase
import com.joeloewi.domain.usecase.ReportCardUseCase
import com.joeloewi.domain.usecase.ValuesUseCase
import com.joeloewi.jumpkking.state.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.asFlowable
import kotlinx.coroutines.rx3.asScheduler
import nl.marc_apps.tts.TextToSpeechFactory
import okhttp3.internal.closeQuietly
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class FriendsViewModel @Inject constructor(
    private val textToSpeechFactory: TextToSpeechFactory,
    private val getValuesUseCase: ValuesUseCase.GetValues,
    private val setJumpCountUseCase: ValuesUseCase.SetJumpCount,
    private val setReportCardUseCase: ReportCardUseCase.Insert,
    private val androidId: String,
    private val getCurrentUserFirebaseAuthUseCase: FirebaseAuthUseCase.GetCurrentUser,
    private val signInAnonymouslyFirebaseAuthUseCase: FirebaseAuthUseCase.SignInAnonymously
) : ViewModel() {
    private val _ioScheduler = Dispatchers.IO.asScheduler()
    private val _values = getValuesUseCase()
    private val _jumpCount = _values.map { it.jumpCount }
    private val _throttledJumpCount = _jumpCount
        .flowOn(Dispatchers.IO)
        .asFlowable(Dispatchers.IO)
        .observeOn(_ioScheduler)
        .throttleLatest(5, TimeUnit.SECONDS)
        .subscribeOn(_ioScheduler)
        .asFlow()

    val insertReportCardState = _throttledJumpCount.map { jumpCount ->
        signInAnonymouslyFirebaseAuthUseCase.runCatching {
            getCurrentUserFirebaseAuthUseCase() ?: invoke()
        }.mapCatching {
            setReportCardUseCase(
                ReportCard(
                    androidId = androidId,
                    jumpCount = jumpCount
                )
            )
        }.fold(
            onSuccess = {
                Lce.Content(it)
            },
            onFailure = { cause ->
                if (cause is CancellationException) {
                    throw cause
                }

                FirebaseCrashlytics.getInstance().recordException(cause)
                Lce.Error(cause)
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
    val textToSpeech = callbackFlow {
        val textToSpeechInstance = textToSpeechFactory.runCatching {
            createOrThrow()
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
        if (cause is CancellationException) {
            throw cause
        }

        FirebaseCrashlytics.getInstance().recordException(cause)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Lce.Loading
    )

    fun increaseJumpCount() {
        viewModelScope.launch(Dispatchers.IO) {
            setJumpCountUseCase(getValuesUseCase().first().jumpCount + 1)
        }
    }
}