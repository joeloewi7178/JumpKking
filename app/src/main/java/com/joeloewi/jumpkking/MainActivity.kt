package com.joeloewi.jumpkking

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.Lifecycle
import com.joeloewi.jumpkking.state.Lce
import com.joeloewi.jumpkking.ui.theme.JumpKkingTheme
import com.joeloewi.jumpkking.util.*
import kotlinx.coroutines.launch
import nl.marc_apps.tts.TextToSpeech
import nl.marc_apps.tts.TextToSpeechInstance

@ExperimentalMaterialApi
@ExperimentalMaterial3Api
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

@ExperimentalMaterialApi
@ExperimentalMaterial3Api
@Composable
fun JumpKkingApp() {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current
    val lifecycle by LocalLifecycleOwner.current.lifecycle.observeAsState()
    var textToSpeech by remember { mutableStateOf<Lce<TextToSpeechInstance>>(Lce.Loading) }
    val (count, onCountChange) = rememberSaveable { mutableStateOf(100000000000L) }

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

    Scaffold() { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HamsterImage(textToSpeech = textToSpeech)
        }
    }
}

@Composable
fun HamsterImage(
    textToSpeech: Lce<TextToSpeechInstance>,
) {
    val kking = "끼잉!"
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val roundTripState = rememberRoundTripState()
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