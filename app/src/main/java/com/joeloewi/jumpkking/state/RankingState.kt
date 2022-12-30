package com.joeloewi.jumpkking.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import com.joeloewi.jumpkking.viewmodel.RankingViewModel
import kotlinx.coroutines.Dispatchers

@Stable
class RankingState(
    private val navController: NavController,
    private val rankingViewModel: RankingViewModel
) {
    val androidId: String
        get() = rankingViewModel.androidId

    val pagedReportCards
        @Composable get() = rankingViewModel.pagedReportCards.collectAsLazyPagingItems(Dispatchers.IO)

    fun onCloseButtonClick() {
        navController.navigateUp()
    }
}

@Composable
fun rememberRankingState(
    navController: NavController,
    rankingViewModel: RankingViewModel
) = remember(
    navController,
    rankingViewModel
) {
    RankingState(
        navController = navController,
        rankingViewModel = rankingViewModel
    )
}