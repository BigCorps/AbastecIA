package com.abastecia.frentista.di

import com.abastecia.frentista.data.repository.FakePlugPagRepository
import com.abastecia.frentista.data.repository.IPlugPagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlugPagModule {

    @Binds
    @Singleton
    abstract fun bindPlugPagRepository(
        fake: FakePlugPagRepository
    ): IPlugPagRepository
}
