package ir.srp.rasad.domain.models

data class DataModel(
    val targetUsername: String,
    var targetMarker: Int? = null,
    val latitude: Double,
    val longitude: Double
)