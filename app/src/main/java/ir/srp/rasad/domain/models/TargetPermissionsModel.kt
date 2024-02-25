package ir.srp.rasad.domain.models

import android.os.Parcelable
import ir.srp.rasad.core.utils.TargetPermissionType
import kotlinx.parcelize.Parcelize

@Parcelize
data class TargetPermissionsModel(
    val type: TargetPermissionType,
    val coordinate: String
) : Parcelable