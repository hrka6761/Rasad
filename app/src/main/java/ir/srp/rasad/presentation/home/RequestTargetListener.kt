package ir.srp.rasad.presentation.home

import ir.srp.rasad.domain.models.ObserverTargetModel

interface RequestTargetListener {
    fun onRequest(isNewTarget: Boolean, vararg targets: ObserverTargetModel)
    fun onRemoveTarget(targetName: String)
}