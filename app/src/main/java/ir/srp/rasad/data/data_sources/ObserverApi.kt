package ir.srp.rasad.data.data_sources

import ir.srp.rasad.domain.models.ObserverOperationModel
import ir.srp.rasad.domain.models.PermittedObserversModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT

interface ObserverApi {

    @Headers("Accept: application/json")
    @POST("get_observers")
    suspend fun getObservers(
        @Header("Authorization") token: String,
        @Body observerOperationModel: ObserverOperationModel,
    ): Response<List<PermittedObserversModel>>

    @Headers("Accept: application/json")
    @POST("delete_observer")
    suspend fun deleteObserver(
        @Header("Authorization") token: String,
        @Body observerOperationModel: ObserverOperationModel,
    ): Response<ResponseBody>

    @Headers("Accept: application/json")
    @PUT("add_observer")
    suspend fun addObserver(
        @Header("Authorization") token: String,
        @Body observerOperationModel: ObserverOperationModel,
    ): Response<ResponseBody>
}