package ir.srp.rasad.domain.usecases

import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.UserRepo
import javax.inject.Inject

class RegisterUseCase @Inject constructor(private val userRepo: UserRepo) {

    suspend operator fun invoke(userModel: UserModel) = userRepo.registerUser(userModel)
}