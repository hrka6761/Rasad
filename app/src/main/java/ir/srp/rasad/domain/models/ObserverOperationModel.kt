package ir.srp.rasad.domain.models

import com.squareup.moshi.Json

data class ObserverOperationModel(
    @field:Json(name = "id") val userId: String,
    @field:Json(name = "username") val username: String,
    @field:Json(name = "target") val target: String? = null
)