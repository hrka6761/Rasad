package ir.srp.rasad.domain.usecases.user_usecases

import ir.srp.rasad.domain.models.UserModel
import ir.srp.rasad.domain.repositories.UserRepo
import javax.inject.Inject

class EditUserNameUseCase @Inject constructor(private val userRepo: UserRepo) {

    suspend operator fun invoke(token: String, userModel: UserModel) =
        userRepo.editUsername(token, userModel)
}