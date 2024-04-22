package ir.srp.rasad.domain.usecases.preference_usecases

import ir.srp.rasad.core.Resource
import ir.srp.rasad.domain.models.ForceRunDataModel
import ir.srp.rasad.domain.repositories.LocalUserDataRepo
import javax.inject.Inject

class ForceRunInfoUseCase @Inject constructor(
    private val localUserDataRepo: LocalUserDataRepo,
) {
    suspend fun saveForceRunInfo(forceRunDataModel: ForceRunDataModel) =
        localUserDataRepo.saveForceRunInfo(forceRunDataModel)

    suspend fun loadForceRunInfo(): Resource<ForceRunDataModel?> =
        localUserDataRepo.loadForceRunInfo()
}