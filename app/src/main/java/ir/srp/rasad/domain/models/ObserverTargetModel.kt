package ir.srp.rasad.domain.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ObserverTargetModel(
    val name: String,
    val targetUsername: String,
    val permissions: TargetPermissionsModel
) : Parcelable