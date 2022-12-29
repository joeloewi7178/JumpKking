package com.joeloewi.jumpkking.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.google.firebase.crashlytics.internal.model.ImmutableList
import com.joeloewi.jumpkking.viewmodel.MainViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Stable
class MainState(
    val friends: ImmutableList<Friend>,
    private val mainViewModel: MainViewModel
) {
    val androidId
        get() = mainViewModel.androidId

    val pagedReportCards
        @Composable get() = mainViewModel.pagedReportCards.collectAsLazyPagingItems()


    val insertReportCardState
        @Composable get() = mainViewModel.insertReportCardState.collectAsStateWithLifecycle().value

    val jumpCount
        @Composable get() = mainViewModel.jumpCount.collectAsStateWithLifecycle().value

    val textToSpeech
        @Composable get() = mainViewModel.textToSpeech.collectAsStateWithLifecycle().value

    fun setJumpCount(jumpCount: Long) {
        mainViewModel.setJumpCount(jumpCount)
    }
}

enum class Friend {
    Hamster, Cat
}

@Composable
fun rememberMainState(
    friends: ImmutableList<Friend> = ImmutableList.from(*Friend.values()),
    mainViewModel: MainViewModel
) = remember(
    friends,
    mainViewModel
) {
    MainState(
        friends = friends,
        mainViewModel = mainViewModel
    )
}