package com.joeloewi.jumpkking.ui.navigation

sealed class JumpKkingNavigation(
    val route: String,
) {
    data object Friends : JumpKkingNavigation(
        route = "friends"
    )
}