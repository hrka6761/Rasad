package ir.srp.rasad.data.repositories

import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.errors.ErrorDetector
import ir.srp.rasad.data.data_sources.UserApi
import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.UserRepo
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import kotlin.Exception

class UserRepoImpl @Inject constructor(
    private val userApi: UserApi,
    private val errorDetector: ErrorDetector,
) : UserRepo {

    private val retrofitError = errorDetector.retrofitError


    override suspend fun requestOTP(mobileNumber: String): Resource<ResponseBody?> {
        return try {
            userApi.requestOtp(mobileNumber).run { result(this) }
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun loginUser(loginDataModel: LoginDataModel): Resource<UserModel?> {
        return try {
            userApi.login(loginDataModel).run { result(this) }
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun registerUser(userModel: UserModel): Resource<UserModel?> {
        return try {
            userApi.register(userModel).run { result(this) }
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
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


    private fun <T> result(response: Response<T>): Resource<T?> =
        if (response.isSuccessful)
            Resource.Success(response.body())
        else
            errorDetector[response]
}