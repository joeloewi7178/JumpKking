package com.joeloewi.jumpkking.initializer

import android.content.Context
import androidx.startup.Initializer
import coil.Coil
import coil.ImageLoader
import coil.imageLoader
import com.joeloewi.jumpkking.di.initializerEntryPoint

class CoilInitializer : Initializer<ImageLoader> {
    override fun create(context: Context): ImageLoader {
        val imageLoader = context.initializerEntryPoint.imageLoader()

        Coil.setImageLoader(imageLoader)

        return context.imageLoader
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}