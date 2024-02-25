package ir.srp.rasad.domain.usecases.preference_usecase

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.TargetModel
import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class UserTargetsUseCase @Inject constructor(private val localUserDataRepo: LocalUserDataRepo) {

    suspend fun saveUserTargets(targets: HashSet<TargetModel>) =
        localUserDataRepo.saveUserTargets(targets)

    suspend fun loadUserTargets(): Resource<HashSet<TargetModel>?> =
        localUserDataRepo.loadUserTargets()
}