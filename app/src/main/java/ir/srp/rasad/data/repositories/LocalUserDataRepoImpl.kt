package ir.srp.rasad.data.repositories

import ir.srp.rasad.core.Constants.TARGETS_PREFERENCE_KEY
import ir.srp.rasad.core.Constants.USER_ACCOUNT_INFO_PREFERENCE_KEY
import ir.srp.rasad.core.Constants.USER_STATE_PREFERENCE_KEY
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.errors.local_errors.LoadLocalDataError
import ir.srp.rasad.core.errors.local_errors.NotFoundTargetError
import ir.srp.rasad.core.utils.JsonConverter
import ir.srp.rasad.data.data_sources.UserLocalDataSource
import ir.srp.rasad.domain.models.PreferenceTargetModel
import ir.srp.rasad.domain.models.TargetModel
import ir.srp.rasad.domain.models.TargetPermissionsModel
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class LocalUserDataRepoImpl @Inject constructor(
    private val userLocalDataSource: UserLocalDataSource,
    private val jsonConverter: JsonConverter,
    private val loadLocalDataError: LoadLocalDataError,
    private val notFoundTargetError: NotFoundTargetError,
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
            Resource.Error(loadLocalDataError)
    }

    override suspend fun saveUserTargets(targets: HashSet<TargetModel>) {
        val targetsString = HashSet<String>()

        for (target in targets) {
            val permission = jsonConverter.convertObjectToJsonString(target.permissions)
            val model =
                PreferenceTargetModel(target.name, target.username, target.markerIcon, permission)
            val targetString = jsonConverter.convertObjectToJsonString(model)
            targetsString.add(targetString)
        }

        userLocalDataSource.saveSet(TARGETS_PREFERENCE_KEY, targetsString)
    }

    override suspend fun loadUserTargets(): Resource<HashSet<TargetModel>?> {
        val targetsModel = HashSet<TargetModel>()
        val targetsString = userLocalDataSource.loadSet(TARGETS_PREFERENCE_KEY, null)

        return if (targetsString != null) {
            for (target in targetsString) {
                val model = jsonConverter.convertJsonStringToObject(
                    target,
                    PreferenceTargetModel::class.java
                ) as PreferenceTargetModel
                val permission = jsonConverter.convertJsonStringToObject(
                    model.permissions,
                    TargetPermissionsModel::class.java
                ) as TargetPermissionsModel
                val targetModel =
                    TargetModel(model.name, model.username, model.markerIcon, permission)

                targetsModel.add(targetModel)
            }

            Resource.Success(targetsModel)
        } else
            Resource.Error(notFoundTargetError)
    }

    override suspend fun clearAllUserData() =
        userLocalDataSource.clearAllSharedPreferences()

    override suspend fun clearData(preferenceKey: String) =
        userLocalDataSource.clearSharedPreferences(preferenceKey)
}