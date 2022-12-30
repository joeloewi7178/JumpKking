package com.joeloewi.jumpkking.di

import android.content.Context
import coil.ImageLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UtilModule {

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context) =
        ImageLoader.Builder(context).build()
}