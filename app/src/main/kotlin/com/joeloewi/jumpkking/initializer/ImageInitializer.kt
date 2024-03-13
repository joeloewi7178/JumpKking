package com.joeloewi.jumpkking.initializer

import android.content.Context
import androidx.lifecycle.ProcessLifecycleInitializer
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.startup.Initializer
import coil.request.ImageRequest
import com.joeloewi.jumpkking.R
import com.joeloewi.jumpkking.di.initializerEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class ImageInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val imageLoader = context.initializerEntryPoint.imageLoader()
        val processLifecycleOwner = ProcessLifecycleOwner.get()

        processLifecycleOwner.lifecycleScope.launch(Dispatchers.IO + CoroutineExceptionHandler { _, _ -> }) {
            awaitAll(
                *listOf(
                    R.drawable.idle_hamster,
                    R.drawable.jumping_hamster,
                    R.drawable.idle_mysterious_cat,
                    R.drawable.meowing_mysterious_cat
                ).map {
                    ImageRequest.Builder(context).data(it).build()
                }.map {
                    async(Dispatchers.IO + SupervisorJob()) { imageLoader.execute(it) }
                }.toTypedArray()
            )
        }
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> =
        mutableListOf(
            ProcessLifecycleInitializer::class.java,
            CoilInitializer::class.java
        )
}