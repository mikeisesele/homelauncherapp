package com.michael.homeapponboarding.di

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences(
            "home_app_prefs",
            Context.MODE_PRIVATE
        )
    }

    @Provides
    @Singleton
    fun providePackageManager(
        @ApplicationContext context: Context
    ): PackageManager = context.packageManager



    @Provides
    @Singleton
    fun provideApplicationContext(@ApplicationContext context: Context): Context = context
}