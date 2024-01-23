package ir.srp.rasad.data.repositories

import ir.srp.rasad.core.Constants.USER_ACCOUNT_INFO_PREFERENCE_KEY
import ir.srp.rasad.core.Constants.USER_STATE_PREFERENCE_KEY
import ir.srp.rasad.core.utils.JsonConverter
import ir.srp.rasad.data.data_sources.UserLocalDataSource
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class LocalUserDataRepoImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val jsonConverter: JsonConverter,
) : LocalUserDataRepo {

    override suspend fun saveUserLoginState(state: Boolean) =
        userLocalDataSource.saveBoolean(USER_STATE_PREFERENCE_KEY, state)

    override suspend fun loadUserLoginState() =
        userLocalDataSource.loadBoolean(USER_STATE_PREFERENCE_KEY, false)

    override suspend fun saveUserAccountInfo(userAccountInfo: UserModel) =
        userLocalDataSource.saveString(
            USER_ACCOUNT_INFO_PREFERENCE_KEY,
            jsonConverter.convertObjectToJsonString(userAccountInfo)
        )

    override suspend fun loadUserAccountInfo(): UserModel? {
        val userAccountInfo = userLocalDataSource.loadString(USER_ACCOUNT_INFO_PREFERENCE_KEY, null)
        return if (userAccountInfo != null)
            jsonConverter.convertJsonStringToObject(
                userAccountInfo,
                UserModel::class.java
            ) as UserModel
        else
            null
    }
}