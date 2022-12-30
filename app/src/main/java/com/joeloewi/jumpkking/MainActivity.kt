package com.joeloewi.jumpkking

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import com.google.android.material.color.DynamicColors
import com.joeloewi.jumpkking.state.Lce
import com.joeloewi.jumpkking.ui.navigation.JumpKkingNavigation
import com.joeloewi.jumpkking.ui.navigation.friends.FriendsDestination
import com.joeloewi.jumpkking.ui.navigation.friends.screen.FriendsScreen
import com.joeloewi.jumpkking.ui.navigation.friends.screen.RankingScreen
import com.joeloewi.jumpkking.ui.theme.JumpKkingTheme
import com.joeloewi.jumpkking.util.*
import com.joeloewi.jumpkking.viewmodel.FriendsViewModel
import com.joeloewi.jumpkking.viewmodel.MainViewModel
import com.joeloewi.jumpkking.viewmodel.RankingViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

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
            when (currentUser) {
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
                CompositionLocalProvider(LocalActivity provides this) {
                    JumpKkingApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialNavigationApi::class)
@Composable
fun JumpKkingApp() {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

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
            route = "main",
            startDestination = JumpKkingNavigation.Friends.route
        ) {
            navigation(
                startDestination = FriendsDestination.FriendsScreen.route,
                route = JumpKkingNavigation.Friends.route
            ) {
                composable(route = FriendsDestination.FriendsScreen.route) {
                    val friendsViewModel: FriendsViewModel = hiltViewModel()

                    FriendsScreen(
                        navController = navController,
                        friendsViewModel = friendsViewModel
                    )
                }

                bottomSheet(route = FriendsDestination.RankingScreen.route) {
                    val rankingViewModel: RankingViewModel = hiltViewModel()

                    RankingScreen(
                        navController = navController,
                        rankingViewModel = rankingViewModel
                    )
                }
            }
        }
    }
}