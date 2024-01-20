package ir.srp.rasad.domain.repositories

interface UserRepo {

    fun requestOTP()
    fun loginUser()
    fun registerUser()
    fun editMobileNumber()
    fun editUsername()
    fun editEmail()
}