package com.joeloewi.jumpkking.ui.navigation.friends.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.joeloewi.jumpkking.R
import com.joeloewi.jumpkking.state.Lce
import com.joeloewi.jumpkking.util.RoundTripState
import com.joeloewi.jumpkking.util.RoundTripValue
import com.joeloewi.jumpkking.util.animateRoundTripByDpAsState
import com.joeloewi.jumpkking.util.castToQuotaReachedExceptionAndGetMessage
import com.joeloewi.jumpkking.util.rememberRoundTripState
import com.joeloewi.jumpkking.viewmodel.Friend
import com.joeloewi.jumpkking.viewmodel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.marc_apps.tts.TextToSpeechInstance
import java.text.DecimalFormat

@Composable
fun FriendsScreen(
    onViewRankingButtonClick: () -> Unit,
    friendsViewModel: FriendsViewModel = hiltViewModel()
) {
    val jumpCount by friendsViewModel.jumpCount.collectAsStateWithLifecycle()
    val textToSpeech by friendsViewModel.textToSpeech.collectAsStateWithLifecycle()
    val insertReportCardState by friendsViewModel.insertReportCardState.collectAsStateWithLifecycle()

    FriendsContent(
        jumpCount = { jumpCount },
        textToSpeech = { textToSpeech },
        insertReportCardState = { insertReportCardState },
        onViewRankingButtonClick = onViewRankingButtonClick,
        onCountChange = friendsViewModel::increaseJumpCount
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FriendsContent(
    jumpCount: () -> Long,
    textToSpeech: () -> Lce<TextToSpeechInstance>,
    insertReportCardState: () -> Lce<Void?>,
    onViewRankingButtonClick: () -> Unit,
    onCountChange: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val pagerState = rememberPagerState { Friend.entries.size }

    LaunchedEffect(Unit) {
        snapshotFlow(insertReportCardState).catch { }.flowOn(Dispatchers.IO).collect {
            when (it) {
                is Lce.Error -> {
                    val message = it.error.castToQuotaReachedExceptionAndGetMessage()

                    with(snackbarHostState) {
                        currentSnackbarData?.dismiss()
                        showSnackbar(
                            message = message,
                            duration = SnackbarDuration.Indefinite
                        )
                    }
                }

                else -> {

                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                AnimatedCount(count = jumpCount())
                IconButton(onClick = onViewRankingButtonClick) {
                    Icon(
                        imageVector = Icons.Default.Leaderboard,
                        contentDescription = Icons.Default.Leaderboard.name
                    )
                }
            }

            Row(
                modifier = Modifier.weight(1f),
            ) {
                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fill,
                    key = { Friend.entries[it].name }
                ) { page ->
                    when (Friend.entries[page]) {
                        Friend.Hamster -> {
                            val configuration = LocalConfiguration.current
                            val maxOffset by remember(configuration) {
                                derivedStateOf {
                                    (configuration.screenHeightDp * 0.3).dp
                                }
                            }
                            val roundTripState = rememberRoundTripState(maxOffset = -maxOffset)

                            HamsterCard(
                                textToSpeech = textToSpeech,
                                roundTripState = roundTripState,
                                onCountChange = onCountChange
                            )
                        }

                        Friend.Cat -> {
                            CatCard(
                                textToSpeech = textToSpeech,
                                onCountChange = onCountChange
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                HorizontalPagerIndicator(
                    pagerState = pagerState,
                    pageCount = Friend.entries.size
                )
            }
        }
    }
}

@Composable
private fun AnimatedCount(
    count: Long,
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            if (targetState > initialState) {
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            } else {
                slideInVertically { height -> -height } + fadeIn() togetherWith
                        slideOutVertically { height -> height } + fadeOut()
            }.using(
                SizeTransform(clip = false)
            )
        }, label = ""
    ) { targetCount ->
        Text(
            modifier = Modifier,
            text = DecimalFormat.getInstance().format(targetCount),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
private fun HamsterCard(
    textToSpeech: () -> Lce<TextToSpeechInstance>,
    roundTripState: RoundTripState,
    onCountChange: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                HamsterImage(
                    textToSpeech = textToSpeech,
                    roundTripState = roundTripState,
                    onCountChange = onCountChange
                )
            }
        }
    }
}

@Composable
private fun CatCard(
    textToSpeech: () -> Lce<TextToSpeechInstance>,
    onCountChange: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                CatImage(
                    textToSpeech = textToSpeech,
                    onCountChange = onCountChange
                )
            }
        }
    }
}

@Composable
private fun CatImage(
    textToSpeech: () -> Lce<TextToSpeechInstance>,
    onCountChange: () -> Unit
) {
    val meowing = remember { "뫼애앵" }
    val coroutineScope = rememberCoroutineScope()
    val (isTtsPlaying, onIsTtsPlayingChange) = remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val idleCat = remember(context, lifecycleOwner) {
        ImageRequest.Builder(context)
            .data(R.drawable.idle_mysterious_cat)
            .lifecycle(lifecycleOwner)
            .build()
    }
    val meowingCat = remember(context, lifecycleOwner) {
        ImageRequest.Builder(context)
            .data(R.drawable.meowing_mysterious_cat)
            .lifecycle(lifecycleOwner)
            .build()
    }

    AsyncImage(
        modifier = Modifier
            .fillMaxWidth(0.4f)
            .aspectRatio(1.0f)
            .padding(bottom = 16.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = textToSpeech() is Lce.Content && !isTtsPlaying
            ) {
                onCountChange()

                coroutineScope.launch(Dispatchers.IO) {
                    onIsTtsPlayingChange(true)

                    textToSpeech().content?.runCatching {
                        say(
                            text = meowing,
                            clearQueue = true,
                        )
                    }

                    onIsTtsPlayingChange(false)
                }
            },
        model = if (isTtsPlaying) {
            meowingCat
        } else {
            idleCat
        },
        contentDescription = null
    )
}

@Composable
private fun HamsterImage(
    textToSpeech: () -> Lce<TextToSpeechInstance>,
    roundTripState: RoundTripState,
    onCountChange: () -> Unit
) {
    val kking = remember { "끼잉!" }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val roundTripValue = roundTripState.roundTripValue
    val idleHamster = ImageRequest.Builder(context)
        .data(R.drawable.idle_hamster)
        .build()
    val jumpingHamster = ImageRequest.Builder(context)
        .data(R.drawable.jumping_hamster)
        .build()

    AsyncImage(
        modifier = Modifier
            .fillMaxWidth(0.4f)
            .aspectRatio(1.0f)
            .padding(bottom = 16.dp)
            .composed {
                val roundTripAnimationOffset by animateRoundTripByDpAsState(roundTripState = roundTripState)

                absoluteOffset(y = roundTripAnimationOffset)
            }
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = roundTripValue.isIdle && textToSpeech() is Lce.Content
            ) {
                onCountChange()

                coroutineScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        roundTripState.start()
                    }

                    textToSpeech().content
                        ?.runCatching {
                            say(
                                text = kking,
                                clearQueue = true
                            )
                        }
                }
            },
        model = when (roundTripValue) {
            RoundTripValue.Idle -> {
                idleHamster
            }

            else -> {
                jumpingHamster
            }
        },
        contentDescription = null
    )
}