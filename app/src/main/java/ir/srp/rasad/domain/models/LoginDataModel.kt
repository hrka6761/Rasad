package ir.srp.rasad.domain.models

import com.squareup.moshi.Json

data class LoginDataModel(
    @field:Json(name = "mobile") val mobile: String,
    @field:Json(name = "otp") val otp: String,
)
