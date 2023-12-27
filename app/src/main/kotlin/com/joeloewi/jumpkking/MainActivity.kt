package com.joeloewi.jumpkking

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.BottomSheetNavigator
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.android.material.color.DynamicColors
import com.joeloewi.jumpkking.state.Lce
import com.joeloewi.jumpkking.ui.navigation.JumpKkingNavigation
import com.joeloewi.jumpkking.ui.navigation.friends.FriendsDestination
import com.joeloewi.jumpkking.ui.navigation.friends.screen.FriendsScreen
import com.joeloewi.jumpkking.ui.navigation.friends.screen.RankingScreen
import com.joeloewi.jumpkking.ui.theme.JumpKkingTheme
import com.joeloewi.jumpkking.util.LocalActivity
import com.joeloewi.jumpkking.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val _mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        DynamicColors.applyToActivityIfAvailable(this)
        enableEdgeToEdge()

        var currentUser by mutableStateOf<Lce<Any>>(Lce.Loading)

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                _mainViewModel.currentUser.onEach {
                    currentUser = it
                }.collect()
            }
        }

        splashScreen.setKeepOnScreenCondition {
            currentUser.isLoading
        }

        setContent {
            JumpKkingTheme {
                CompositionLocalProvider(LocalActivity provides this) {
                    JumpKkingApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterialApi::class)
@Composable
fun JumpKkingApp() {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val bottomSheetNavigator = remember(sheetState) { BottomSheetNavigator(sheetState) }
    val navController = rememberNavController(bottomSheetNavigator)
    val activity = LocalActivity.current

    ModalBottomSheetLayout(
        sheetShape = MaterialTheme.shapes.large.copy(
            bottomEnd = CornerSize(0),
            bottomStart = CornerSize(0)
        ),
        bottomSheetNavigator = bottomSheetNavigator,
        sheetBackgroundColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = contentColorFor(backgroundColor = MaterialTheme.colorScheme.surface),
        scrimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.32f)
    ) {
        NavHost(
            navController = navController,
            route = activity::class.java.simpleName,
            startDestination = JumpKkingNavigation.Friends.route
        ) {
            navigation(
                startDestination = FriendsDestination.FriendsScreen.route,
                route = JumpKkingNavigation.Friends.route
            ) {
                composable(route = FriendsDestination.FriendsScreen.route) {
                    FriendsScreen(
                        onViewRankingButtonClick = {
                            navController.navigate(FriendsDestination.RankingScreen.route)
                        }
                    )
                }

                bottomSheet(route = FriendsDestination.RankingScreen.route) {
                    RankingScreen(
                        onCloseButtonClick = {
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}