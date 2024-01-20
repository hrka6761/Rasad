package ir.srp.rasad.domain.models

data class UserModel(
    val id: String? = null,
    val username: String,
    val mobileNumber: String,
    val email: String,
)
