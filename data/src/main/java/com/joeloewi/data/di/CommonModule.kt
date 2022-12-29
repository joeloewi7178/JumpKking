package com.joeloewi.data.di

import android.app.Application
import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.joeloewi.data.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonModule {

    @Singleton
    @Provides
    fun provideFirestoreReportCardCollection(): CollectionReference =
        Firebase.firestore.collection(
            if (BuildConfig.DEBUG) {
                "reportCardDev"
            } else {
                "reportCard"
            }
        )

    @Singleton
    @Provides
    fun provideAndroidId(application: Application): String =
        Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth
}