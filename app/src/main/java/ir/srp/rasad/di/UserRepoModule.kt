package ir.srp.rasad.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.srp.rasad.data.repositories.UserRepoImpl
import ir.srp.rasad.domain.repositories.UserRepo

@Module
@InstallIn(SingletonComponent::class)
interface UserRepoModule {

    @Binds
    fun provideUserRepo(userRepoImpl: UserRepoImpl): UserRepo
}