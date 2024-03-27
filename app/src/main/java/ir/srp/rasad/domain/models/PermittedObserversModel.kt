package ir.srp.rasad.domain.models

import com.squareup.moshi.Json

data class PermittedObserversModel(
    @field:Json(name = "id") val rowId: Int? = null,
    @field:Json(name = "username") val username: String,
    @field:Json(name = "target") val target: String
)