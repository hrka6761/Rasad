package ir.srp.rasad.data.repositories

import ir.srp.rasad.core.Constants.USER_ACCOUNT_INFO_PREFERENCE_KEY
import ir.srp.rasad.core.Constants.USER_STATE_PREFERENCE_KEY
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.errors.local_errors.PreferenceError
import ir.srp.rasad.core.utils.JsonConverter
import ir.srp.rasad.data.data_sources.UserLocalDataSource
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class LocalUserDataRepoImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val jsonConverter: JsonConverter,
    private val preferenceError: PreferenceError,
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

    override suspend fun loadUserAccountInfo(): Resource<UserModel?> {
        val userAccountInfo = userLocalDataSource.loadString(USER_ACCOUNT_INFO_PREFERENCE_KEY, null)
        return if (userAccountInfo != null)
            Resource.Success(
                jsonConverter.convertJsonStringToObject(
                    userAccountInfo,
                    UserModel::class.java
                ) as UserModel
            )
        else
            Resource.Error(preferenceError)
    }
}