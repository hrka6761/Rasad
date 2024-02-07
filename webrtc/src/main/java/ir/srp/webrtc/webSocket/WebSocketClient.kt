package ir.srp.webrtc.webSocket

import ir.srp.webrtc.core.Constants.CLOSE_WEBSOCKET_STATUS_CODE
import ir.srp.webrtc.core.Constants.CLOSE_WEBSOCKET_STATUS_REASON
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class WebSocketClient(
    private val url: String,
    private val listener: WebSocketListener,
) {

    private val okHttpClient = OkHttpClient()
    private lateinit var request: Request
    private lateinit var webSocket: WebSocket
    private val isConnected = false


    fun connect() {
        request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, listener)
    }

    fun disConnect() {
        webSocket.close(CLOSE_WEBSOCKET_STATUS_CODE, CLOSE_WEBSOCKET_STATUS_REASON)
    }

    fun sendData(data: ByteString) {
        webSocket.send(data)
    }

    fun sendData(message: String) {
        webSocket.send(message)
    }
}