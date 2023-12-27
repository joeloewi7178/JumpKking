package com.joeloewi.data.di

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.joeloewi.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Provides
    fun provideFirestoreReportCardCollection(): CollectionReference =
        Firebase.firestore.collection(
            if (BuildConfig.DEBUG) {
                "reportCardDev"
            } else {
                "reportCard"
            }
        )

    @SuppressLint("HardwareIds")
    @Provides
    fun provideAndroidId(application: Application): String =
        Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
}