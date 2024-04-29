package ir.srp.rasad.domain.models

import ir.srp.rasad.core.WebSocketDataType

data class WebsocketDataModel(
    val type: WebSocketDataType,
    val username: String,
    val targets: Array<String>? = null,
    val data: Any? = null
)