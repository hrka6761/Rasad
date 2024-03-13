package ir.srp.rasad.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TargetModel(
    val name: String,
    val username: String,
    val markerIcon: Int,
    val permissions: TargetPermissionsModel
) : Parcelable