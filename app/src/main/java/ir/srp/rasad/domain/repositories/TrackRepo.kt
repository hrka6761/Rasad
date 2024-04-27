package ir.srp.rasad.domain.repositories

import ir.srp.rasad.domain.models.WebsocketDataModel
import okhttp3.Response
import okio.ByteString

interface TrackRepo {

    suspend fun createChannel(
        url: String,
        successCallback: ((response: Response) -> Unit)?,
        failCallback: ((t: Throwable?, response: Response?) -> Unit)?,
        serverDisconnectCallback: ((t: Throwable?, response: Response?) -> Unit)?,
        clientDisconnectCallback: ((t: Throwable?, response: Response?) -> Unit)?,
    )

    suspend fun removeChannel(
        onClosingConnection: ((code: Int, reason: String) -> Unit)?,
        onClosedConnection: ((code: Int, reason: String) -> Unit)?,
    )

    suspend fun senData(
        data: WebsocketDataModel,
        onSendMessageFail: ((t: Throwable?, response: Response?) -> Unit)?,
    )

    suspend fun receiveData(
        onReceiveTextMessage: ((text: String) -> Unit)?,
        onReceiveBinaryMessage: ((bytes: ByteString) -> Unit)?,
    )

    fun isChannelExist(): Boolean
}