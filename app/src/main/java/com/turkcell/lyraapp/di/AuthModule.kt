package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [AuthRepository] arayüzünü somut implementasyonuna ([AuthRepositoryImpl]) bağlar.
 *
 * `@Binds` ile yapıldığından Hilt fazladan kod üretmez; gerçek API implementasyonu
 * eklendiğinde yalnızca buradaki bağlama hedefi değiştirilir.
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule{
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}