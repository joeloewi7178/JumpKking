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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.rx3.asFlowable
import nl.marc_apps.tts.TextToSpeech
import nl.marc_apps.tts.TextToSpeechInstance
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
    private val _values = getValuesUseCase()
    private val _jumpCount = _values.map { it.jumpCount }
    private val _reportCard = _jumpCount.asFlowable(viewModelScope.coroutineContext)
        .throttleLatest(5, TimeUnit.SECONDS)
        .asFlow()
    private val _insertReportCardState = MutableStateFlow<Lce<Unit>>(Lce.Loading)
    private val _textToSpeech = MutableStateFlow<Lce<TextToSpeechInstance>>(Lce.Loading)
    val insertReportCardState = _insertReportCardState.asStateFlow()

    val jumpCount = _jumpCount.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = Values().jumpCount
    )
    val pagedReportCards = getAllPagedReportCardUseCase().cachedIn(viewModelScope)
    val textToSpeech = _textToSpeech.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            if (getCurrentUserFirebaseAuthUseCase() == null) {
                signInAnonymouslyFirebaseAuthUseCase()
            }

            getOneReportCardUseCase.runCatching {
                invoke()
            }.onSuccess {
                if (_values.first().jumpCount == 0L && it != null) {
                    setJumpCount(it.jumpCount)
                }
            }.onFailure { cause ->
                FirebaseCrashlytics.getInstance().recordException(cause)
            }

            _reportCard.onEach {
                _insertReportCardState.value = Lce.Loading
                _insertReportCardState.value = setReportCardUseCase.runCatching {
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
            }.flowOn(Dispatchers.IO).launchIn(this)
        }
    }

    fun setJumpCount(jumpCount: Long) {
        viewModelScope.launch(Dispatchers.Default) {
            setJumpCountUseCase(jumpCount)
        }
    }

    fun setTextToSpeech() {
        viewModelScope.launch(Dispatchers.Default) {
            _textToSpeech.value.content?.close()
            _textToSpeech.value = Lce.Loading
            _textToSpeech.value = TextToSpeech.runCatching {
                createOrThrow(application)
            }.fold(
                onSuccess = {
                    Lce.Content(it)
                },
                onFailure = {
                    Lce.Error(it)
                }
            )
        }
    }
}