package ir.srp.rasad.data.repositories

import ir.srp.rasad.core.Constants.TOKEN_HEADER_KEY
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.errors.ErrorDetector
import ir.srp.rasad.data.data_sources.UserApi
import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.UserRepo
import okhttp3.ResponseBody
import javax.inject.Inject
import kotlin.Exception

class UserRepoImpl @Inject constructor(
    private val userApi: UserApi,
    private val errorDetector: ErrorDetector,
) : UserRepo {

    private val retrofitError = errorDetector.retrofitError


    override suspend fun requestOTP(mobileNumber: String): Resource<ResponseBody?> {
        return try {
            val response = userApi.requestOtp(mobileNumber)
            if (response.isSuccessful)
                Resource.Success(response.body())
            else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun loginUser(loginDataModel: LoginDataModel): Resource<UserModel?> {
        return try {
            val response = userApi.login(loginDataModel)
            if (response.isSuccessful) {
                (response.body() as UserModel).token =
                    response.headers()[TOKEN_HEADER_KEY].toString()
                Resource.Success(response.body())
            } else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun registerUser(userModel: UserModel): Resource<UserModel?> {
        return try {
            val response = userApi.register(userModel)
            if (response.isSuccessful) {
                (response.body() as UserModel).token =
                    response.headers()[TOKEN_HEADER_KEY].toString()
                Resource.Success(response.body())
            } else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun editUsername(token: String, userModel: UserModel): Resource<UserModel?> {
        return try {
            val response =
                userApi.updateUsername(token, userModel)
            if (response.isSuccessful) {
                (response.body() as UserModel).token =
                    response.headers()[TOKEN_HEADER_KEY].toString()
                Resource.Success(response.body())
            } else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun editEmail(token: String, userModel: UserModel): Resource<UserModel?> {
        return try {
            val response =
                userApi.updateEmail(token, userModel)
            if (response.isSuccessful) {
                (response.body() as UserModel).token =
                    response.headers()[TOKEN_HEADER_KEY].toString()
                Resource.Success(response.body())
            } else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }
}