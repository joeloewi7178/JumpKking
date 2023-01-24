package com.joeloewi.jumpkking.initializer

import android.content.Context
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ProcessLifecycleInitializer
import androidx.startup.Initializer

class ComposeViewInitializer : Initializer<ComposeView> {
    override fun create(context: Context): ComposeView {
        return ComposeView(context)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf(
        ProcessLifecycleInitializer::class.java
    )
}