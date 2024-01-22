package ir.srp.rasad.data.repositories

import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.errors.local_errors.RetrofitError
import ir.srp.rasad.core.errors.network_errors.UnknownError
import ir.srp.rasad.data.data_sources.UserApi
import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.UserRepo
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import kotlin.Exception

class UserRepoImpl @Inject constructor(private val userApi: UserApi) : UserRepo {

    override suspend fun requestOTP(mobileNumber: String): Resource<ResponseBody?> {
        return try {
            userApi.requestOtp(mobileNumber).run { result(this) }
        } catch (e: Exception) {
            Resource.Error(RetrofitError())
        }
    }

    override suspend fun loginUser(loginDataModel: LoginDataModel): Resource<UserModel?> {
        return try {
            userApi.login(loginDataModel).run { result(this) }
        } catch (e: Exception) {
            Resource.Error(RetrofitError())
        }
    }

    override suspend fun registerUser(userModel: UserModel): Resource<UserModel?> {
        return try {
            userApi.register(userModel).run { result(this) }
        } catch (e: Exception) {
            Resource.Error(RetrofitError())
        }
    }

    override fun editMobileNumber() {
        TODO("Not yet implemented")
    }

    override fun editUsername() {
        TODO("Not yet implemented")
    }

    override fun editEmail() {
        TODO("Not yet implemented")
    }


    private fun <T> result(response: Response<T>): Resource<T?> {
        return if (response.isSuccessful)
            Resource.Success(response.body())
        else
            Resource.Error(UnknownError())
    }
}