package ir.srp.rasad.data.data_sources

import ir.srp.rasad.core.Constants.CLOSE_WEBSOCKET_STATUS_CODE
import ir.srp.rasad.core.Constants.CLOSE_WEBSOCKET_STATUS_REASON
import ir.srp.rasad.core.utils.JsonConverter
import ir.srp.rasad.domain.models.WebsocketDataModel
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserWebsocket @Inject constructor(private val jsonConverter: JsonConverter) {

    private var _connectRetriesNum: Int = 0
    private var _connectRetriesInterval: Long = 0
    private var failedPing = 0
    private val okHttpClient = OkHttpClient()
    private lateinit var request: Request
    private lateinit var webSocket: WebSocket
    private val listener = ChannelListener()
    private var isConnected = false
    private var pingTimer: Timer? = null
    private var pong = true


    fun createConnection(
        url: String,
        onConnectSuccess: ((response: Response) -> Unit)?,
        onConnectFail: ((t: Throwable?, response: Response?) -> Unit)?,
        onServerDisconnect: ((t: Throwable?, response: Response?) -> Unit)?,
        onClientDisconnect: ((t: Throwable?, response: Response?) -> Unit)?,
        connectRetriesNum: Int = 3,
        connectRetriesInterval: Long = 3000,
    ) {
        _connectRetriesNum = connectRetriesNum
        _connectRetriesInterval = connectRetriesInterval

        listener.onConnectSuccess = onConnectSuccess
        listener.onConnectionFail = onConnectFail
        listener.onServerDisconnect = onServerDisconnect
        listener.onClientDisconnect = onClientDisconnect

        request = Request.Builder().url(url).build()
        webSocket = okHttpClient.newWebSocket(request, listener)
    }

    fun removeConnection(
        onClosingConnection: ((code: Int, reason: String) -> Unit)?,
        onClosedConnection: ((code: Int, reason: String) -> Unit)?,
    ) {
        listener.onClosingConnection = onClosingConnection
        listener.onClosedConnection = onClosedConnection

        webSocket.close(
            CLOSE_WEBSOCKET_STATUS_CODE,
            CLOSE_WEBSOCKET_STATUS_REASON
        )
    }

    fun sendData(
        data: WebsocketDataModel,
        onSendMessageFail: ((t: Throwable?, response: Response?) -> Unit)?,
    ) {
        listener.onConnectionFail = onSendMessageFail
        webSocket.send(jsonConverter.convertObjectToJsonString(data))
    }

    fun receiveData(
        onReceiveTextMessage: ((text: String) -> Unit)?,
        onReceiveBinaryMessage: ((bytes: ByteString) -> Unit)?,
    ) {
        listener.onReceiveTextMessage = onReceiveTextMessage
        listener.onReceiveBinaryMessage = onReceiveBinaryMessage
    }

    fun isConnected() = isConnected


    private fun ping(period: Long) {
        if (pingTimer == null)
            pingTimer = Timer()
        pingTimer?.schedule(object : TimerTask() {
            override fun run() {
                if (isConnected) {
                    if (pong) {
                        webSocket.send("ping")
                        pong = false
                        failedPing = 0
                    } else {
                        failedPing++
                        webSocket.send("ping")
                        if (failedPing == _connectRetriesNum) {
                            pingTimer?.purge()
                            pingTimer?.cancel()
                            pingTimer = null
                            pong = true
                            failedPing = 0
                            if (isConnected)
                                listener.onClientDisconnect?.let { it(null, null) }
                        }
                    }
                } else {
                    pingTimer?.purge()
                    pingTimer?.cancel()
                    pingTimer = null
                    pong = true
                    failedPing = 0
                }
            }
        }, 0, period)
    }


    private inner class ChannelListener : WebSocketListener() {

        var onConnectSuccess: ((response: Response) -> Unit)? = null
        var onConnectionFail: ((t: Throwable?, response: Response?) -> Unit)? = null
        var onServerDisconnect: ((t: Throwable?, response: Response?) -> Unit)? = null
        var onClientDisconnect: ((t: Throwable?, response: Response?) -> Unit)? = null
        var onClosingConnection: ((code: Int, reason: String) -> Unit)? = null
        var onClosedConnection: ((code: Int, reason: String) -> Unit)? = null
        var onReceiveTextMessage: ((text: String) -> Unit)? = null
        var onReceiveBinaryMessage: ((bytes: ByteString) -> Unit)? = null


        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)

            onConnectSuccess?.let { it(response) }
            this.onConnectionFail = null
            isConnected = true
            ping(_connectRetriesInterval)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)

            if (t.message == "Connection reset") {
                onServerDisconnect?.let {
                    it(t, response)
                    isConnected = false
                }
                return
            }

            if (t.message == "Software caused connection abort") {
                onClientDisconnect?.let {
                    it(t, response)
                    isConnected = false
                }
                return
            }

            this.onConnectionFail?.let { it(t, response) }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)

            onClosingConnection?.let { it(code, reason) }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)

            onClosedConnection?.let { it(code, reason) }
            isConnected = false
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)

            if (text == "pong") {
                pong = true
                return
            }

            onReceiveTextMessage?.let { it(text) }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)

            onReceiveBinaryMessage?.let { it(bytes) }
        }
    }
}