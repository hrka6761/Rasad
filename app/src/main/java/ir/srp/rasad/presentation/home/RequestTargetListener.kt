package ir.srp.rasad.presentation.home

import ir.srp.rasad.domain.models.TargetModel

interface RequestTargetListener {
    fun onRequest(isNew: Boolean, vararg targets: TargetModel)
    fun onRemoveTarget(targetName: String)
}