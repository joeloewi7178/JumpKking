package com.joeloewi.jumpkking.ui.navigation.friends.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.joeloewi.domain.entity.ReportCard
import com.joeloewi.jumpkking.util.castToQuotaReachedExceptionAndGetMessage
import com.joeloewi.jumpkking.viewmodel.RankingViewModel
import io.github.fornewid.placeholder.foundation.PlaceholderHighlight
import io.github.fornewid.placeholder.foundation.fade
import io.github.fornewid.placeholder.foundation.placeholder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import java.text.DecimalFormat

@Composable
fun RankingScreen(
    onCloseButtonClick: () -> Unit,
    rankingViewModel: RankingViewModel = hiltViewModel()
) {
    val pagedReportCards = rankingViewModel.pagedReportCards.collectAsLazyPagingItems()
    val androidId = rankingViewModel.androidId

    RankingContent(
        pagedReportCards = pagedReportCards,
        androidId = androidId,
        onCloseButtonClick = onCloseButtonClick
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun RankingContent(
    pagedReportCards: LazyPagingItems<ReportCard>,
    androidId: String,
    onCloseButtonClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        snapshotFlow { pagedReportCards.loadState }.mapLatest { it.source }.mapLatest {
            with(pagedReportCards.loadState.source) { listOf(append, prepend, refresh) }
        }.mapLatest { loadStates ->
            loadStates.filterIsInstance<LoadState.Error>().firstOrNull()
        }.filterNotNull().mapLatest {
            it.error.castToQuotaReachedExceptionAndGetMessage()
        }.catch { }.flowOn(Dispatchers.IO).collectLatest { message ->
            with(snackbarHostState) {
                currentSnackbarData?.dismiss()
                showSnackbar(
                    message = message,
                    duration = SnackbarDuration.Indefinite
                )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            val isRefreshing = pagedReportCards.loadState.refresh is LoadState.Loading
            val alpha by remember(isRefreshing) {
                derivedStateOf {
                    if (isRefreshing) {
                        0.4f
                    } else {
                        1.0f
                    }
                }
            }

            FloatingActionButton(
                onClick = {
                    if (!isRefreshing) {
                        pagedReportCards.refresh()
                    }
                }
            ) {
                Icon(
                    modifier = Modifier.alpha(alpha),
                    imageVector = Icons.Default.Refresh,
                    contentDescription = Icons.Default.Refresh.name
                )
            }
        },
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
                navigationIcon = {
                    IconButton(
                        onClick = { },
                        enabled = false
                    ) {
                        Icon(
                            imageVector = Icons.Default.Leaderboard,
                            contentDescription = Icons.Default.Leaderboard.name
                        )
                    }
                },
                title = {
                    Text(text = "랭킹")
                },
                actions = {
                    IconButton(
                        onClick = onCloseButtonClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = Icons.Default.Close.name
                        )
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(
                count = pagedReportCards.itemCount,
                key = pagedReportCards.itemKey { it.androidId },
                contentType = pagedReportCards.itemContentType { item -> item::class.java.simpleName }
            ) { index ->
                val item = runCatching { pagedReportCards[index] }.getOrNull()

                val isMyReportCard = item?.androidId == androidId

                val backgroundColor =
                    if (isMyReportCard) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        Color.Unspecified
                    }

                ListItem(
                    modifier = Modifier.animateItemPlacement(),
                    colors = ListItemDefaults.colors(
                        containerColor = backgroundColor
                    ),
                    leadingContent = {
                        Text(
                            modifier = Modifier
                                .placeholder(
                                    visible = item == null,
                                    color = MaterialTheme.colorScheme.outline,
                                    highlight = PlaceholderHighlight.fade(
                                        highlightColor = MaterialTheme.colorScheme.background,
                                    )
                                ),
                            text = "${index + 1}"
                        )
                    },
                    headlineContent = {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .placeholder(
                                    visible = item == null,
                                    color = MaterialTheme.colorScheme.outline,
                                    highlight = PlaceholderHighlight.fade(
                                        highlightColor = MaterialTheme.colorScheme.background,
                                    )
                                ),
                            text = item?.jumpCount?.let { DecimalFormat.getInstance().format(it) }
                                ?: ""
                        )
                    },
                    supportingContent = if (isMyReportCard) {
                        {
                            Text(text = "나")
                        }
                    } else {
                        null
                    }
                )
            }
        }
    }
}