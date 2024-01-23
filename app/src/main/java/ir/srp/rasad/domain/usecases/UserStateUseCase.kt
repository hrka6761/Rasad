package ir.srp.rasad.domain.usecases

import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class UserStateUseCase @Inject constructor(private val localUserDataRepo: LocalUserDataRepo) {

    suspend fun setUserLoginState(state: Boolean) = localUserDataRepo.saveUserLoginState(state)

    suspend fun getUserLoginState(): Boolean = localUserDataRepo.loadUserLoginState()
}