package ir.srp.rasad.domain.usecases.user_usecases

import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.repositories.UserRepo
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val userRepo: UserRepo) {

    suspend operator fun invoke(loginDataModel: LoginDataModel) = userRepo.loginUser(loginDataModel)
}