package ir.srp.rasad.domain.usecases.user_usecases

import ir.srp.rasad.domain.repositories.UserRepo
import javax.inject.Inject

class OTPUseCase @Inject constructor(private val userRepo: UserRepo) {

    suspend operator fun invoke(mobileNumber: String) = userRepo.requestOTP(mobileNumber)
}