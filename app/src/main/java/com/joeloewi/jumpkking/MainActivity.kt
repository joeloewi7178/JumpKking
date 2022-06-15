package com.joeloewi.jumpkking

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import com.joeloewi.jumpkking.state.Lce
import com.joeloewi.jumpkking.ui.theme.JumpKkingTheme
import com.joeloewi.jumpkking.util.*
import com.joeloewi.jumpkking.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeech
import nl.marc_apps.tts.TextToSpeechInstance
import java.text.DecimalFormat

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun JumpKkingApp() {
    val mainViewModel: MainViewModel = hiltViewModel()
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current
    val lifecycle by LocalLifecycleOwner.current.lifecycle.observeAsState()
    var textToSpeech by remember { mutableStateOf<Lce<TextToSpeechInstance>>(Lce.Loading) }
    val jumpCount by mainViewModel.jumpCount.collectAsState()

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
        sheetPeekHeight = 64.dp,
        sheetContent = {
            Scaffold(
                topBar = {
                    SmallTopAppBar(
                        title = {
                            val title = when (scaffoldState.bottomSheetState.currentValue) {
                                BottomSheetValue.Collapsed -> {
                                    "당신의 현재 순위"
                                }
                                BottomSheetValue.Expanded -> {
                                    "상위 100명 랭킹"
                                }
                            }

                            Text(text = title)
                        }
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    items(50) { index ->
                        ListItem(
                            text = {
                                Text(text = "$index")
                            }
                        )
                    }
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
    val kking = "끼잉!"
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
            CircularProgressIndicator()
        }
    }
}