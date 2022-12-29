package com.joeloewi.jumpkking

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.itemsIndexed
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.fade
import com.google.accompanist.placeholder.placeholder
import com.google.android.material.color.DynamicColors
import com.joeloewi.jumpkking.state.Friend
import com.joeloewi.jumpkking.state.Lce
import com.joeloewi.jumpkking.state.MainState
import com.joeloewi.jumpkking.state.rememberMainState
import com.joeloewi.jumpkking.ui.theme.JumpKkingTheme
import com.joeloewi.jumpkking.util.*
import com.joeloewi.jumpkking.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import nl.marc_apps.tts.TextToSpeechInstance
import java.text.DecimalFormat

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val _mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        var currentUser by mutableStateOf<Lce<Any>>(Lce.Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _mainViewModel.currentUser.onEach {
                    currentUser = it
                }.collect()
            }
        }

        splashScreen.setKeepOnScreenCondition {
            when (_mainViewModel.currentUser.value) {
                Lce.Loading -> {
                    true
                }
                else -> {
                    false
                }
            }
        }

        DynamicColors.applyToActivityIfAvailable(this)

        setContent {
            JumpKkingTheme(
                window = window
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalActivity provides this) {
                        JumpKkingApp(
                            mainState = rememberMainState(mainViewModel = _mainViewModel)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(
    ExperimentalPagerApi::class,
    ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun JumpKkingApp(
    mainState: MainState = rememberMainState(mainViewModel = hiltViewModel())
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val textToSpeech = mainState.textToSpeech
    val jumpCount = mainState.jumpCount
    val insertReportCardState = mainState.insertReportCardState
    val pagerState = rememberPagerState()

    LaunchedEffect(insertReportCardState) {
        with(insertReportCardState) {
            when (this) {
                is Lce.Error -> {
                    val message = error.castToQuotaReachedExceptionAndGetMessage()

                    with(scaffoldState.snackbarHostState) {
                        currentSnackbarData?.dismiss()
                        showSnackbar(
                            message = message,
                            duration = androidx.compose.material.SnackbarDuration.Indefinite
                        )
                    }
                }
                else -> {

                }
            }
        }
    }

    BottomSheetScaffold(
        sheetShape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0.dp),
            bottomEnd = CornerSize(0.dp)
        ),
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
        backgroundColor = MaterialTheme.colorScheme.background,
        contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.background),
        scaffoldState = scaffoldState,
        sheetPeekHeight = WindowInsets.navigationBars.asPaddingValues()
            .calculateBottomPadding() + 64.dp,
        sheetContent = {
            val currentBottomSheetValue by remember(scaffoldState.bottomSheetState) {
                derivedStateOf {
                    scaffoldState.bottomSheetState.currentValue
                }
            }

            AnimatedContent(
                targetState = currentBottomSheetValue.ordinal,
                transitionSpec = {
                    fadeIn() with fadeOut()
                }
            ) { targetState ->
                when (BottomSheetValue.values()[targetState]) {
                    BottomSheetValue.Collapsed -> {
                        CollapsedBottomSheet(
                            scaffoldState = scaffoldState
                        )
                    }
                    BottomSheetValue.Expanded -> {
                        ExpandedBottomSheet(
                            scaffoldState = scaffoldState,
                            mainState = mainState
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .consumedWindowInsets(innerPadding)
                .padding(
                    WindowInsets.safeDrawing
                        .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                        .asPaddingValues()
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 32.dp)
                    .fillMaxWidth()
            ) {
                AnimatedCount(count = jumpCount)
            }

            Row(
                modifier = Modifier.weight(1f),
            ) {
                HorizontalPager(
                    state = pagerState,
                    count = mainState.friends.size,
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
                                onCountChange = {
                                    mainState.setJumpCount(jumpCount + 1)
                                }
                            )
                        }
                        Friend.Cat -> {
                            CatCard(
                                textToSpeech = textToSpeech,
                                onCountChange = {
                                    mainState.setJumpCount(jumpCount + 1)
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.padding(vertical = 8.dp),
            ) {
                HorizontalPagerIndicator(
                    activeColor = androidx.compose.material3.LocalContentColor.current,
                    inactiveColor = androidx.compose.material3.LocalContentColor.current.copy(alpha = 0.38f),
                    pagerState = pagerState
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AnimatedCount(
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
            modifier = Modifier
                .fillMaxWidth(),
            text = DecimalFormat.getInstance().format(targetCount),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@Composable
fun HamsterCard(
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
fun CatCard(
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
fun CatImage(
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
            .animateContentSize()
            .fillMaxWidth(0.4f)
            .aspectRatio(1.0f)
            .padding(bottom = 16.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                enabled = textToSpeech is Lce.Content
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
fun HamsterImage(
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

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CollapsedBottomSheet(
    scaffoldState: BottomSheetScaffoldState
) {
    val coroutineScope = rememberCoroutineScope()
    val isPlaceholderVisible by remember(scaffoldState.bottomSheetState) {
        derivedStateOf {
            scaffoldState.bottomSheetState.targetValue == BottomSheetValue.Expanded
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "다 같이 점프해요.")
                },
                actions = {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                scaffoldState.bottomSheetState.expand()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = Icons.Default.ExpandLess.name
                        )
                    }
                },
                windowInsets = WindowInsets.navigationBars
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            items(
                items = (1..10).toList()
            ) {
                ReportCardListItemPlaceHolder(
                    isPlaceholderVisible = isPlaceholderVisible
                )
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun ExpandedBottomSheet(
    scaffoldState: BottomSheetScaffoldState,
    mainState: MainState,
) {
    val coroutineScope = rememberCoroutineScope()
    val pagedReportCards = mainState.pagedReportCards

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

                with(scaffoldState.snackbarHostState) {
                    currentSnackbarData?.dismiss()
                    showSnackbar(
                        message = message,
                        duration = androidx.compose.material.SnackbarDuration.Indefinite
                    )
                }
            }
        }
    }

    Scaffold(
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
                        onClick = {
                            coroutineScope.launch {
                                scaffoldState.bottomSheetState.collapse()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = Icons.Default.Expand.name
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
                        item.androidId == mainState.androidId

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