package com.givebox.di

import android.app.Application
import android.content.res.Resources
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.givebox.data.local.db.AppDatabase
import com.givebox.data.local.pref.AppPreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by Fasil on 20/11/22.
 */
@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Singleton
    @Provides
    fun provideAppDatabase(application: Application): AppDatabase =
        AppDatabase.getInstance(application)

    @Provides
    @Singleton
    fun provideAppPreferenceManager(application: Application) = AppPreferenceManager(application)

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()


    @Singleton
    @Provides
    fun provideFirebaseMessaging() = FirebaseMessaging.getInstance()

    @Singleton
    @Provides
    fun provideResources(application: Application): Resources = application.resources

    @Singleton
    @Provides
    fun provideGlideRequest(application: Application): RequestManager =
        Glide.with(application.applicationContext)

}