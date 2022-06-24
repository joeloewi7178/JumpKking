package com.joeloewi.jumpkking

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.fade
import com.google.accompanist.placeholder.placeholder
import com.google.android.material.color.DynamicColors
import com.joeloewi.jumpkking.state.Lce
import com.joeloewi.jumpkking.ui.theme.ContentAlpha
import com.joeloewi.jumpkking.ui.theme.JumpKkingTheme
import com.joeloewi.jumpkking.util.*
import com.joeloewi.jumpkking.util.ListItem
import com.joeloewi.jumpkking.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeech
import nl.marc_apps.tts.TextToSpeechInstance
import java.text.DecimalFormat

@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        installSplashScreen()

        DynamicColors.applyToActivityIfAvailable(this)

        setContent {
            JumpKkingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CompositionLocalProvider(LocalActivity provides this) {
                        JumpKkingApp()
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
@Composable
fun JumpKkingApp() {
    val mainViewModel: MainViewModel = hiltViewModel()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current
    val lifecycle by LocalLifecycleOwner.current.lifecycle.observeAsState()
    var textToSpeech by remember { mutableStateOf<Lce<TextToSpeechInstance>>(Lce.Loading) }
    val jumpCount by mainViewModel.jumpCount.collectAsState()
    val insertReportCardState by mainViewModel.insertReportCardState.collectAsState()

    LaunchedEffect(lifecycle) {
        when (lifecycle) {
            Lifecycle.Event.ON_RESUME -> {
                textToSpeech = TextToSpeech.runCatching {
                    createOrThrow(context)
                }.fold(
                    onSuccess = {
                        Lce.Content(it)
                    },
                    onFailure = {
                        Lce.Error(it)
                    }
                )
            }
            else -> {
                textToSpeech.content?.close()
                textToSpeech = Lce.Loading
            }
        }
    }

    LaunchedEffect(insertReportCardState) {
        with(insertReportCardState) {
            when (this) {
                is Lce.Error -> {
                    val message = this.error.castToQuotaReachedExceptionAndGetMessage()

                    with(scaffoldState.snackbarHostState) {
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

    Column {
        BottomSheetScaffold(
            modifier = Modifier.weight(1f),
            topBar = {
                Spacer(
                    modifier = Modifier.padding(
                        WindowInsets.statusBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            .asPaddingValues()
                    )
                )
            },
            sheetShape = MaterialTheme.shapes.large.copy(
                bottomStart = CornerSize(0.dp),
                bottomEnd = CornerSize(0.dp)
            ),
            sheetBackgroundColor = MaterialTheme.colorScheme.surface,
            sheetContentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
            backgroundColor = MaterialTheme.colorScheme.background,
            contentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.background),
            scaffoldState = scaffoldState,
            sheetPeekHeight = 64.dp,
            sheetContent = {
                when (scaffoldState.bottomSheetState.currentValue) {
                    BottomSheetValue.Collapsed -> {
                        CollapsedBottomSheet(
                            scaffoldState = scaffoldState
                        )
                    }
                    BottomSheetValue.Expanded -> {
                        ExpandedBottomSheet(
                            scaffoldState = scaffoldState,
                            mainViewModel = mainViewModel
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(0.95f),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(top = 32.dp)
                                .fillMaxWidth()
                        ) {
                            AnimatedCount(count = jumpCount)
                        }

                        HamsterImage(
                            textToSpeech = textToSpeech,
                            onCountChange = {
                                mainViewModel.setJumpCount(jumpCount + 1)
                            }
                        )
                    }
                }
            }
        }
        Surface(
            modifier = Modifier
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp
        ) {

        }
    }
}

@ExperimentalAnimationApi
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
fun HamsterImage(
    textToSpeech: Lce<TextToSpeechInstance>,
    onCountChange: () -> Unit
) {
    val kking = remember { "끼잉!" }
    val configuration = LocalConfiguration.current
    val maxOffset = (configuration.screenHeightDp * 0.3).dp
    val coroutineScope = rememberCoroutineScope()
    val roundTripState = rememberRoundTripState(maxOffset = -maxOffset)
    val roundTripValue by roundTripState.roundTripValue
    val roundTripAnimationOffset by animateRoundTripByDpAsState(roundTripState = roundTripState)

    when (textToSpeech) {
        is Lce.Content -> {
            Image(
                modifier = Modifier
                    .fillMaxSize(0.4f)
                    .absoluteOffset(y = roundTripAnimationOffset)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        enabled = roundTripValue.isIdle
                    ) {
                        onCountChange()
                        roundTripState.start()

                        coroutineScope.launch {
                            textToSpeech.content
                                .runCatching {
                                    say(
                                        text = kking,
                                        clearQueue = true
                                    )
                                }
                                .onFailure { cause ->
                                    cause.printStackTrace()
                                }
                        }
                    },
                painter = when (roundTripValue) {
                    RoundTripValue.Idle -> {
                        painterResource(id = R.drawable.idle_hamster)
                    }
                    else -> {
                        painterResource(id = R.drawable.jumping_hamster)
                    }
                },
                contentDescription = null
            )
        }
        is Lce.Error -> {

        }
        Lce.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun CollapsedBottomSheet(
    scaffoldState: BottomSheetScaffoldState
) {
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            Column {
                SmallTopAppBar(
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
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        )
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun ExpandedBottomSheet(
    scaffoldState: BottomSheetScaffoldState,
    mainViewModel: MainViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val pagedReportCards =
        mainViewModel.pagedReportCards.collectAsLazyPagingItems()

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
                        duration = SnackbarDuration.Indefinite
                    )
                }
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            val isRefreshing = pagedReportCards.loadState.refresh is LoadState.Loading
            val alpha = if (isRefreshing) {
                0.4f
            } else {
                1.0f
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
            Column {
                SmallTopAppBar(
                    modifier = Modifier.padding(
                        WindowInsets.statusBars.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            .asPaddingValues()
                    ),
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
            }
        }
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
                        item.androidId == mainViewModel.androidId

                    val backgroundColor =
                        if (isMyReportCard) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            Color.Unspecified
                        }

                    ListItem(
                        modifier = Modifier
                            .background(backgroundColor)
                            .animateItemPlacement(),
                        icon = {
                            Text(text = "${index + 1}")
                        },
                        text = {
                            Text(text = DecimalFormat.getInstance().format(item.jumpCount))
                        },
                        secondaryText = if (isMyReportCard) {
                            {
                                Text(
                                    modifier = Modifier.alpha(ContentAlpha.medium),
                                    text = "나"
                                )
                            }
                        } else {
                            null
                        }
                    )
                } else {
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        icon = {
                            AsyncImage(
                                modifier = Modifier
                                    .size(24.dp)
                                    .placeholder(
                                        visible = true,
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
                        trailing = {
                            Checkbox(
                                modifier = Modifier
                                    .placeholder(
                                        visible = true,
                                        color = MaterialTheme.colorScheme.outline,
                                        highlight = PlaceholderHighlight.fade(
                                            highlightColor = MaterialTheme.colorScheme.background,
                                        )
                                    ),
                                checked = false,
                                onCheckedChange = null
                            )
                        },
                        text = {
                            Text(
                                modifier = Modifier
                                    .placeholder(
                                        visible = true,
                                        color = MaterialTheme.colorScheme.outline,
                                        highlight = PlaceholderHighlight.fade(
                                            highlightColor = MaterialTheme.colorScheme.background,
                                        )
                                    ),
                                text = ""
                            )
                        },
                        secondaryText = {
                            Text(
                                modifier = Modifier
                                    .placeholder(
                                        visible = true,
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
            }
        }
    }
}