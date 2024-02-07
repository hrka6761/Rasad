package ir.srp.webrtc.utils

import ir.srp.webrtc.models.DataModel
import ir.srp.webrtc.webSocket.WebSocketClient
import org.webrtc.DataChannel

interface ChannelEventsListener {

    fun onSuccessSignalingServerConnection(webSocket: WebSocketClient)
    fun onFailedSignalingServerConnection(t: Throwable)
    fun onCLoseSignalingServerConnection(code: Int, reason: String)
    fun onCreateP2PChannel(dataChannel: DataChannel?)
    fun onReceiveSignalingData(data: DataModel)
    fun onReceiveChannelData(buffer: DataChannel.Buffer?)
}