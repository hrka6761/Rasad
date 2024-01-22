package ir.srp.rasad.domain.models

import com.squareup.moshi.Json

data class UserModel(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "username") val username: String,
    @field:Json(name = "mobile") val mobileNumber: String,
    @field:Json(name = "email") val email: String,
)
