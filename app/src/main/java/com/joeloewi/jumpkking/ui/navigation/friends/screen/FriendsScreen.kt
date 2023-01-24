package com.joeloewi.jumpkking.ui.navigation.friends.screen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.joeloewi.jumpkking.R
import com.joeloewi.jumpkking.state.Friend
import com.joeloewi.jumpkking.state.FriendsState
import com.joeloewi.jumpkking.state.Lce
import com.joeloewi.jumpkking.state.rememberFriendsState
import com.joeloewi.jumpkking.util.*
import com.joeloewi.jumpkking.viewmodel.FriendsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.marc_apps.tts.TextToSpeechInstance
import java.text.DecimalFormat

@Composable
fun FriendsScreen(
    navController: NavController,
    friendsViewModel: FriendsViewModel = hiltViewModel()
) {
    val friendsState = rememberFriendsState(
        navController = navController,
        friendsViewModel = friendsViewModel
    )

    FriendsContent(friendsState = friendsState)
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalPagerApi::class
)
@Composable
private fun FriendsContent(
    friendsState: FriendsState,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val jumpCount = friendsState.jumpCount
    val textToSpeech = friendsState.textToSpeech
    val pagerState = rememberPagerState()
    val insertReportCardState = friendsState.insertReportCardState

    LaunchedEffect(insertReportCardState) {
        with(insertReportCardState) {
            when (this) {
                is Lce.Error -> {
                    val message = error.castToQuotaReachedExceptionAndGetMessage()

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
                AnimatedCount(count = jumpCount)
                IconButton(onClick = friendsState::onViewRankingButtonClick) {
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
                    count = friendsState.friends.size,
                    key = { Friend.values()[it].name }
                ) { page ->
                    when (Friend.values()[page]) {
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
                                onCountChange = friendsState::increaseJumpCount
                            )
                        }
                        Friend.Cat -> {
                            CatCard(
                                textToSpeech = textToSpeech,
                                onCountChange = friendsState::increaseJumpCount
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                HorizontalPagerIndicator(
                    activeColor = LocalContentColor.current,
                    inactiveColor = LocalContentColor.current.copy(alpha = 0.38f),
                    pagerState = pagerState
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AnimatedCount(
    count: Long,
) {
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            if (targetState > initialState) {
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            } else {
                slideInVertically { height -> -height } + fadeIn() with
                        slideOutVertically { height -> height } + fadeOut()
            }.using(
                SizeTransform(clip = false)
            )
        }
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
    textToSpeech: Lce<TextToSpeechInstance>,
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
    textToSpeech: Lce<TextToSpeechInstance>,
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
    textToSpeech: Lce<TextToSpeechInstance>,
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
                enabled = textToSpeech is Lce.Content && !isTtsPlaying
            ) {
                onCountChange()

                coroutineScope.launch(Dispatchers.IO) {
                    onIsTtsPlayingChange(true)

                    textToSpeech.content?.runCatching {
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
    textToSpeech: Lce<TextToSpeechInstance>,
    roundTripState: RoundTripState,
    onCountChange: () -> Unit
) {
    val kking = remember { "끼잉!" }
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val roundTripValue = roundTripState.roundTripValue
    val roundTripAnimationOffset by animateRoundTripByDpAsState(roundTripState = roundTripState)
    val idleHamster = remember(context, lifecycleOwner) {
        ImageRequest.Builder(context)
            .data(R.drawable.idle_hamster)
            .lifecycle(lifecycleOwner)
            .build()
    }
    val jumpingHamster = remember(context, lifecycleOwner) {
        ImageRequest.Builder(context)
            .data(R.drawable.jumping_hamster)
            .lifecycle(lifecycleOwner)
            .build()
    }

    AsyncImage(
        modifier = Modifier
            .fillMaxWidth(0.4f)
            .aspectRatio(1.0f)
            .padding(bottom = 16.dp)
            .absoluteOffset(y = roundTripAnimationOffset)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = roundTripValue.isIdle && textToSpeech is Lce.Content
            ) {
                onCountChange()

                coroutineScope.launch(Dispatchers.IO) {
                    withContext(Dispatchers.Main) {
                        roundTripState.start()
                    }

                    textToSpeech.content
                        ?.runCatching {
                            say(
                                text = kking,
                                clearQueue = true
                            )
                        }
                        ?.onFailure { cause ->
                            cause.printStackTrace()
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