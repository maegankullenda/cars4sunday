package com.maegankullenda.carsonsunday.di

import com.maegankullenda.carsonsunday.data.repository.impl.EventRepositoryImpl
import com.maegankullenda.carsonsunday.data.repository.impl.NoticeRepositoryImpl
import com.maegankullenda.carsonsunday.domain.repository.EventRepository
import com.maegankullenda.carsonsunday.domain.repository.NoticeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindEventRepository(
        impl: EventRepositoryImpl,
    ): EventRepository

    @Binds
    @Singleton
    abstract fun bindNoticeRepository(
        impl: NoticeRepositoryImpl,
    ): NoticeRepository
}
