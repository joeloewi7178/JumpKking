package com.joeloewi.jumpkking.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.google.firebase.crashlytics.internal.model.ImmutableList
import com.joeloewi.jumpkking.ui.navigation.friends.FriendsDestination
import com.joeloewi.jumpkking.viewmodel.FriendsViewModel

@OptIn(ExperimentalLifecycleComposeApi::class)
@Stable
class FriendsState(
    private val navController: NavController,
    val friends: ImmutableList<Friend>,
    private val friendsViewModel: FriendsViewModel
) {
    val insertReportCardState
        @Composable get() = friendsViewModel.insertReportCardState.collectAsStateWithLifecycle().value

    val jumpCount
        @Composable get() = friendsViewModel.jumpCount.collectAsStateWithLifecycle().value

    val textToSpeech
        @Composable get() = friendsViewModel.textToSpeech.collectAsStateWithLifecycle().value

    fun increaseJumpCount() {
        friendsViewModel.increaseJumpCount()
    }

    fun onViewRankingButtonClick() {
        navController.navigate(FriendsDestination.RankingScreen.route)
    }
}

enum class Friend {
    Hamster, Cat
}

@Composable
fun rememberFriendsState(
    navController: NavController,
    friends: ImmutableList<Friend> = ImmutableList.from(*Friend.values()),
    friendsViewModel: FriendsViewModel
) = remember(
    navController,
    friends,
    friendsViewModel
) {
    FriendsState(
        navController = navController,
        friends = friends,
        friendsViewModel = friendsViewModel
    )
}