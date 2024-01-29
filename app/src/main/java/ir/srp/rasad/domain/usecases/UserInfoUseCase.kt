package ir.srp.rasad.domain.usecases

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class UserInfoUseCase @Inject constructor(private val localUserDataRepo: LocalUserDataRepo) {

    suspend fun saveUserAccountInfo(userAccountInfo: UserModel) =
        localUserDataRepo.saveUserAccountInfo(userAccountInfo)

    suspend fun loadUserAccountInfo(): Resource<UserModel?> = localUserDataRepo.loadUserAccountInfo()
}