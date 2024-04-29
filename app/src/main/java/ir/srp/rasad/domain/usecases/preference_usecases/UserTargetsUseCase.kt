package ir.srp.rasad.domain.usecases.preference_usecases

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.ObserverTargetModel
import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class UserTargetsUseCase @Inject constructor(private val localUserDataRepo: LocalUserDataRepo) {

    suspend fun saveUserTargets(targets: HashSet<ObserverTargetModel>) =
        localUserDataRepo.saveUserTargets(targets)

    suspend fun loadUserTargets(): Resource<HashSet<ObserverTargetModel>?> =
        localUserDataRepo.loadUserTargets()
}