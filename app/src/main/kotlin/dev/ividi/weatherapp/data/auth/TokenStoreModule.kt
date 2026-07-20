package dev.ividi.weatherapp.data.auth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Binds the [TokenStore] contract to its real, Keystore-backed [TokenStorage] implementation. */
@Module
@InstallIn(SingletonComponent::class)
abstract class TokenStoreModule {
    @Binds
    abstract fun bindTokenStore(impl: TokenStorage): TokenStore
}
