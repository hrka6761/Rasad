package ir.srp.rasad.domain.usecases

import ir.srp.rasad.domain.repositories.UserRepo
import javax.inject.Inject

class RegisterUseCase @Inject constructor(userRepo: UserRepo)