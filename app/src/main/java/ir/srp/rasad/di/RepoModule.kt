package ir.srp.rasad.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ir.srp.rasad.data.repositories.LocalUserDataRepoImpl
import ir.srp.rasad.data.repositories.ObserverRepoImpl
import ir.srp.rasad.data.repositories.UserRepoImpl
import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import ir.srp.rasad.domain.repositories.ObserverRepo
import ir.srp.rasad.domain.repositories.UserRepo
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepoModule {

    @Singleton
    @Binds
    fun bindUserRepo(userRepoImpl: UserRepoImpl): UserRepo

    @Singleton
    @Binds
    fun bindLocalUserDataRepo(localUserDataRepoImpl: LocalUserDataRepoImpl): LocalUserDataRepo

    @Singleton
    @Binds
    fun bindObserverRepo(observerRepoImpl: ObserverRepoImpl): ObserverRepo
}