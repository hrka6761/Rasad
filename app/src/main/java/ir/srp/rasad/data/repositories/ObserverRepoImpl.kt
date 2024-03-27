package ir.srp.rasad.data.repositories

import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.errors.ErrorDetector
import ir.srp.rasad.data.data_sources.ObserverApi
import ir.srp.rasad.domain.models.ObserverOperationModel
import ir.srp.rasad.domain.models.PermittedObserversModel
import ir.srp.rasad.domain.repositories.ObserverRepo
import okhttp3.ResponseBody
import javax.inject.Inject

class ObserverRepoImpl @Inject constructor(
    private val observerApi: ObserverApi,
    private val errorDetector: ErrorDetector
) : ObserverRepo {

    private val retrofitError = errorDetector.retrofitError


    override suspend fun getObservers(
        token: String,
        observerOperationModel: ObserverOperationModel
    ): Resource<List<PermittedObserversModel>?> {
        return try {
            val response = observerApi.getObservers(token, observerOperationModel)
            if (response.isSuccessful)
                Resource.Success(response.body())
            else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun deleteObserver(
        token: String,
        observerOperationModel: ObserverOperationModel
    ): Resource<ResponseBody?> {
        return try {
            val response = observerApi.deleteObserver(token, observerOperationModel)
            if (response.isSuccessful)
                Resource.Success(response.body())
            else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }

    override suspend fun addObserver(
        token: String,
        observerOperationModel: ObserverOperationModel
    ): Resource<ResponseBody?> {
        return try {
            val response = observerApi.addObserver(token, observerOperationModel)
            if (response.isSuccessful)
                Resource.Success(response.body())
            else
                errorDetector[response]
        } catch (e: Exception) {
            retrofitError.errorMessage = e.message.toString()
            Resource.Error(retrofitError)
        }
    }
}