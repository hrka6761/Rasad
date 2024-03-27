package ir.srp.rasad.domain.usecases.preference_usecases

import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class UserStateUseCase @Inject constructor(private val localUserDataRepo: LocalUserDataRepo) {

    suspend fun setUserLoginState(state: Boolean) = localUserDataRepo.saveUserLoginState(state)

    fun getUserLoginState(): Boolean = localUserDataRepo.loadUserLoginState()
}