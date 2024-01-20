package ir.srp.rasad.domain.usecases

import ir.srp.rasad.domain.repositories.UserRepo
import javax.inject.Inject

class OTPUseCase @Inject constructor(userRepo: UserRepo)