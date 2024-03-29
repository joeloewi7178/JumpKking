package com.joeloewi.jumpkking.initializer

import android.content.Context
import androidx.startup.Initializer
import com.google.android.material.color.DynamicColors
import com.joeloewi.jumpkking.di.initializerEntryPoint

class DynamicColorInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val application = context.initializerEntryPoint.application()

        DynamicColors.applyToActivitiesIfAvailable(application)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}