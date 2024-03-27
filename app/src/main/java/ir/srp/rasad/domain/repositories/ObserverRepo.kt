package ir.srp.rasad.domain.repositories

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.ObserverOperationModel
import ir.srp.rasad.domain.models.PermittedObserversModel
import okhttp3.ResponseBody

interface ObserverRepo {

    suspend fun getObservers(
        token: String,
        observerOperationModel: ObserverOperationModel
    ): Resource<List<PermittedObserversModel>?>

    suspend fun deleteObserver(
        token: String,
        observerOperationModel: ObserverOperationModel
    ): Resource<ResponseBody?>

    suspend fun addObserver(
        token: String,
        observerOperationModel: ObserverOperationModel
    ): Resource<ResponseBody?>
}