package com.abastecia.frentista.di

import android.content.Context
import br.uol.pagseguro.plugpag.PlugPag
import br.uol.pagseguro.plugpag.PlugPagAppIdentification
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlugPagModule {

    @Provides
    @Singleton
    fun providePlugPag(@ApplicationContext context: Context): PlugPag =
        PlugPag(
            context,
            PlugPagAppIdentification("AbastecIA", "1.0.0")
        )
}
