package com.maegankullenda.carsonsunday.di

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.maegankullenda.carsonsunday.data.repository.impl.AuthRepositoryImpl
import com.maegankullenda.carsonsunday.data.source.local.EventOfflineDataSource
import com.maegankullenda.carsonsunday.data.source.local.UserLocalDataSource
import com.maegankullenda.carsonsunday.domain.repository.AuthRepository
import com.maegankullenda.carsonsunday.util.CalendarManager
import com.maegankullenda.carsonsunday.util.NotificationTestHelper
import com.maegankullenda.carsonsunday.util.PushNotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl,
    ): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {

    @Provides
    @Singleton
    fun provideEventOfflineDataSource(@ApplicationContext context: Context): EventOfflineDataSource {
        return EventOfflineDataSource(context)
    }

    @Provides
    @Singleton
    fun provideCalendarManager(@ApplicationContext context: Context): CalendarManager {
        return CalendarManager(context)
    }

    @Provides
    @Singleton
    fun providePushNotificationManager(@ApplicationContext context: Context): PushNotificationManager {
        return PushNotificationManager(context)
    }

    @Provides
    @Singleton
    fun provideNotificationTestHelper(
        firestore: FirebaseFirestore,
        authRepository: AuthRepository,
        pushNotificationManager: PushNotificationManager,
    ): NotificationTestHelper {
        return NotificationTestHelper(firestore, authRepository, pushNotificationManager)
    }
}
