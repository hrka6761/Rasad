package ir.srp.rasad.domain.repositories

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.TargetModel
import ir.srp.rasad.domain.models.UserModel

interface LocalUserDataRepo {

    suspend fun saveUserLoginState(state: Boolean)
    fun loadUserLoginState(): Boolean
    suspend fun saveUserAccountInfo(userAccountInfo: UserModel)
    suspend fun loadUserAccountInfo(): Resource<UserModel?>
    suspend fun saveUserTargets(targets: HashSet<TargetModel>)
    suspend fun loadUserTargets(): Resource<HashSet<TargetModel>?>
    suspend fun clearAllUserData()
    suspend fun clearData(preferenceKey: String)
}