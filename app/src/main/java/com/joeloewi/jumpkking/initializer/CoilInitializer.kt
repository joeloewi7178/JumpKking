package com.joeloewi.jumpkking.initializer

import android.content.Context
import androidx.startup.Initializer
import coil.Coil
import coil.ImageLoader
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

class CoilInitializer: Initializer<Unit> {

    lateinit var imageLoader: ImageLoader

    override fun create(context: Context) {
        resolve(context)

        Coil.setImageLoader(imageLoader)
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface InitializerEntryPoint {
        fun imageLoader(): ImageLoader
    }

    private fun resolve(context: Context) {
        val initializerEntryPoint = EntryPoints.get(context, InitializerEntryPoint::class.java)

        imageLoader = initializerEntryPoint.imageLoader()
    }
}