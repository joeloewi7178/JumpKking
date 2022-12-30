package com.joeloewi.jumpkking.ui.navigation

sealed class JumpKkingNavigation(
    val route: String,
) {
    object Friends : JumpKkingNavigation(
        route = "friends"
    )
}