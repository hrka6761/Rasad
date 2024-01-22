package ir.srp.rasad.data.data_sources

import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.models.UserModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

    @GET("otp/{mobile}")
    suspend fun requestOtp(@Path("mobile") mobileNumber: String): Response<ResponseBody>

    @Headers("Accept: application/json")
    @POST("login")
    suspend fun login(@Body loginDataModel: LoginDataModel): Response<UserModel>

    @Headers("Accept: application/json")
    @POST("register")
    suspend fun register(@Body userModel: UserModel): Response<UserModel>
}