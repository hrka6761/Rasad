package ir.srp.rasad.domain.usecases.observer_usecases

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.ObserverOperationModel
import ir.srp.rasad.domain.models.PermittedObserversModel
import ir.srp.rasad.domain.repositories.ObserverRepo
import javax.inject.Inject

class GetObserversUseCase @Inject constructor(private val observerRepo: ObserverRepo) {

    suspend operator fun invoke(
        token: String,
        observerOperationModel: ObserverOperationModel
    ): Resource<List<PermittedObserversModel>?> =
        observerRepo.getObservers(token, observerOperationModel)
}