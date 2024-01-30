package ir.srp.rasad.domain.models

import com.squareup.moshi.Json

data class UserModel(
    @field:Json(name = "id") val id: String? = null,
    @field:Json(name = "username") val username: String? = null,
    @field:Json(name = "mobile") val mobile: String? = null,
    @field:Json(name = "email") val email: String? = null,
    @field:Json(name = "token") var token: String? = null,
)
