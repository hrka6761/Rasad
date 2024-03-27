package ir.srp.rasad.domain.repositories

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.models.WebsocketDataModel
import okhttp3.Response
import okhttp3.ResponseBody
import okio.ByteString

interface UserRepo {

    suspend fun requestOTP(mobileNumber: String): Resource<ResponseBody?>
    suspend fun loginUser(loginDataModel: LoginDataModel): Resource<UserModel?>
    suspend fun registerUser(userModel: UserModel): Resource<UserModel?>
    suspend fun editUsername(token: String, userModel: UserModel): Resource<UserModel?>
    suspend fun editEmail(token: String, userModel: UserModel): Resource<UserModel?>
}