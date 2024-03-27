package ir.srp.rasad.domain.usecases.observer_usecases

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.ObserverOperationModel
import ir.srp.rasad.domain.repositories.ObserverRepo
import okhttp3.ResponseBody
import javax.inject.Inject

class DeleteObserverUseCase @Inject constructor(private val observerRepo: ObserverRepo) {

    suspend operator fun invoke(
        token: String,
        observerOperationModel: ObserverOperationModel
    ): Resource<ResponseBody?> =
        observerRepo.deleteObserver(token, observerOperationModel)
}