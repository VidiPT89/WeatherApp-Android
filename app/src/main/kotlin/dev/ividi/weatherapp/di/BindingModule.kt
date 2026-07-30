package dev.ividi.weatherapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.ividi.weatherapp.data.auth.AuthRepository
import dev.ividi.weatherapp.data.auth.CurrentUserProvider
import dev.ividi.weatherapp.util.ErrorMessageProvider
import dev.ividi.weatherapp.util.ErrorMessageResolver
import javax.inject.Singleton

/**
 * Binds the narrow interfaces extracted purely for unit-testability (mirroring
 * [dev.ividi.weatherapp.data.auth.TokenStore]) to their concrete, Context-dependent
 * implementations, so Hilt can still satisfy ViewModels that depend on the interface.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    @Binds
    @Singleton
    abstract fun bindCurrentUserProvider(authRepository: AuthRepository): CurrentUserProvider

    @Binds
    @Singleton
    abstract fun bindErrorMessageResolver(errorMessageProvider: ErrorMessageProvider): ErrorMessageResolver
}
