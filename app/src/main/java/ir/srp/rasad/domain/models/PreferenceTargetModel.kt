package ir.srp.rasad.domain.models

data class PreferenceTargetModel(
    val name: String,
    val username: String,
    val markerIcon: Int,
    val permissions: String
)