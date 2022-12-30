package com.joeloewi.jumpkking.ui.navigation.friends

import androidx.navigation.NavType

sealed class FriendsDestination {
    abstract val arguments: List<Pair<String, NavType<*>>>
    protected abstract val plainRoute: String
    val route: String
        get() = "${plainRoute}${
            arguments.map { it.first }.joinToString(
                separator = "/",
                prefix = if (arguments.isEmpty()) {
                    ""
                } else {
                    "/"
                }
            ) { "{$it}" }
        }"

    object FriendsScreen : FriendsDestination() {
        override val arguments: List<Pair<String, NavType<*>>>
            get() = listOf()
        override val plainRoute: String
            get() = "friendsScreen"
    }

    object RankingScreen : FriendsDestination() {
        override val arguments: List<Pair<String, NavType<*>>>
            get() = listOf()
        override val plainRoute: String
            get() = "rankingScreen"
    }
}