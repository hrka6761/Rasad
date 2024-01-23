package ir.srp.rasad.domain.usecases.api_usecases

import ir.srp.rasad.domain.repositories.UserRepo
import javax.inject.Inject

class EditUserNameUseCase @Inject constructor(userRepo: UserRepo)