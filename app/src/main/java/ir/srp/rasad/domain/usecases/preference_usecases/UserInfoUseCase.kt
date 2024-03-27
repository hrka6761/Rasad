package ir.srp.rasad.domain.usecases.preference_usecases

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class UserInfoUseCase @Inject constructor(private val localUserDataRepo: LocalUserDataRepo) {

    suspend fun saveUserAccountInfo(userAccountInfo: UserModel) =
        localUserDataRepo.saveUserAccountInfo(userAccountInfo)

    suspend fun loadUserAccountInfo(): Resource<UserModel?> = localUserDataRepo.loadUserAccountInfo()

    suspend fun clearAllUserData() = localUserDataRepo.clearAllUserData()

    suspend fun clearUserData(preferenceKey: String) = localUserDataRepo.clearData(preferenceKey)
}