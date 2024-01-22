package ir.srp.rasad.domain.repositories

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.models.UserModel
import okhttp3.ResponseBody

interface UserRepo {

    suspend fun requestOTP(mobileNumber: String): Resource<ResponseBody?>
    suspend fun loginUser(loginDataModel: LoginDataModel): Resource<UserModel?>
    fun registerUser()
    fun editMobileNumber()
    fun editUsername()
    fun editEmail()
}