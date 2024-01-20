package ir.srp.rasad.domain.usecases

import ir.srp.rasad.domain.repositories.UserRepo
import javax.inject.Inject

class LoginUseCase @Inject constructor(userRepo: UserRepo)