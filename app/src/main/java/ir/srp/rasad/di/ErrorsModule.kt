package ir.srp.rasad.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.srp.rasad.core.errors.Error
import ir.srp.rasad.core.errors.local_errors.PreferenceError
import ir.srp.rasad.core.errors.local_errors.RetrofitError
import ir.srp.rasad.core.errors.network_errors.E400
import ir.srp.rasad.core.errors.network_errors.E404
import ir.srp.rasad.core.errors.network_errors.E409
import ir.srp.rasad.core.errors.network_errors.UnknownError
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface ErrorsModule {

    @Named("E400")
    @Singleton
    @Binds
    fun bindE400(e400: E400): Error

    @Named("E404")
    @Singleton
    @Binds
    fun bindE404(e404: E404): Error

    @Named("E409")
    @Singleton
    @Binds
    fun bindE409(e409: E409): Error

    @Named("unknown")
    @Singleton
    @Binds
    fun bindUnknownError(unknownError: UnknownError): Error

    @Named("Retrofit")
    @Singleton
    @Binds
    fun bindRetrofitError(retrofitError: RetrofitError): Error

    @Named("Preference")
    @Singleton
    @Binds
    fun bindPreferenceError(preferenceError: PreferenceError): Error
}