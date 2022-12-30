package com.joeloewi.jumpkking.ui.navigation.friends.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.itemsIndexed
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.fade
import com.google.accompanist.placeholder.placeholder
import com.joeloewi.jumpkking.state.RankingState
import com.joeloewi.jumpkking.state.rememberRankingState
import com.joeloewi.jumpkking.util.castToQuotaReachedExceptionAndGetMessage
import com.joeloewi.jumpkking.viewmodel.RankingViewModel
import java.text.DecimalFormat

@Composable
fun RankingScreen(
    navController: NavController,
    rankingViewModel: RankingViewModel
) {
    val rankingState = rememberRankingState(
        navController = navController,
        rankingViewModel = rankingViewModel
    )

    RankingContent(
        rankingState = rankingState
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun RankingContent(
    rankingState: RankingState
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val pagedReportCards = rankingState.pagedReportCards

    LaunchedEffect(pagedReportCards.loadState) {
        val loadStates = mutableListOf<LoadState>()

        with(pagedReportCards.loadState.source) {
            loadStates.add(append)
            loadStates.add(prepend)
            loadStates.add(refresh)
        }

        loadStates.forEach {
            if (it is LoadState.Error) {
                val message = it.error.castToQuotaReachedExceptionAndGetMessage()

                with(snackbarHostState) {
                    currentSnackbarData?.dismiss()
                    showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Indefinite
                    )
                }
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
                    Text(text = "상위 100명 랭킹")
                },
                actions = {
                    IconButton(
                        onClick = rankingState::onCloseButtonClick
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
            itemsIndexed(
                pagedReportCards,
                key = { _, item -> item.androidId }
            ) { index, item ->
                if (item != null) {
                    val isMyReportCard =
                        item.androidId == rankingState.androidId

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
                            Text(text = "${index + 1}")
                        },
                        headlineText = {
                            Text(text = DecimalFormat.getInstance().format(item.jumpCount))
                        },
                        supportingText = if (isMyReportCard) {
                            {
                                Text(
                                    text = "나"
                                )
                            }
                        } else {
                            null
                        }
                    )
                } else {
                    ReportCardListItemPlaceHolder()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReportCardListItemPlaceHolder(
    isPlaceholderVisible: Boolean = true
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth(),
        leadingContent = {
            AsyncImage(
                modifier = Modifier
                    .size(24.dp)
                    .placeholder(
                        visible = isPlaceholderVisible,
                        color = MaterialTheme.colorScheme.outline,
                        highlight = PlaceholderHighlight.fade(
                            highlightColor = MaterialTheme.colorScheme.background,
                        )
                    ),
                model = ImageRequest.Builder(
                    LocalContext.current
                ).build(),
                contentDescription = null
            )
        },
        headlineText = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = isPlaceholderVisible,
                        color = MaterialTheme.colorScheme.outline,
                        highlight = PlaceholderHighlight.fade(
                            highlightColor = MaterialTheme.colorScheme.background,
                        )
                    ),
                text = ""
            )
        },
        supportingText = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .placeholder(
                        visible = isPlaceholderVisible,
                        color = MaterialTheme.colorScheme.outline,
                        highlight = PlaceholderHighlight.fade(
                            highlightColor = MaterialTheme.colorScheme.background,
                        )
                    ),
                text = ""
            )
        }
    )
}