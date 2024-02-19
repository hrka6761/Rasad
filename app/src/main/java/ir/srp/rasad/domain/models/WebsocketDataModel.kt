package ir.srp.rasad.domain.models

import ir.srp.rasad.core.WebSocketDataType

data class WebsocketDataModel(
    val type: WebSocketDataType,
    val username: String,
    val target: String? = null,
    val data: String? = null
)