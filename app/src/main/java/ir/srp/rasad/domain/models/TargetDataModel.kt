package ir.srp.rasad.domain.models

data class TargetDataModel(
    val targetUsername: String,
    val permissions: TargetPermissionsModel,
)