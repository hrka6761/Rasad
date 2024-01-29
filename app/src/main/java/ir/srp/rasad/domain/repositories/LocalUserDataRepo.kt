package ir.srp.rasad.domain.repositories

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.UserModel

interface LocalUserDataRepo {

    suspend fun saveUserLoginState(state: Boolean)
    suspend fun loadUserLoginState(): Boolean
    suspend fun saveUserAccountInfo(userAccountInfo: UserModel)
    suspend fun loadUserAccountInfo(): Resource<UserModel?>
}