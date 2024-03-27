package ir.srp.rasad.data.repositories

import ir.srp.rasad.data.data_sources.UserWebsocket
import ir.srp.rasad.domain.models.WebsocketDataModel
import ir.srp.rasad.domain.repositories.TrackRepo
import okhttp3.Response
import okio.ByteString
import javax.inject.Inject

class TrackRepoImpl @Inject constructor(
    private val userWebsocket: UserWebsocket
) : TrackRepo {

    override suspend fun createChannel(
        url: String,
        successCallback: ((response: Response) -> Unit)?,
        failCallback: ((t: Throwable, response: Response?) -> Unit)?,
        serverDisconnectCallback: ((t: Throwable, response: Response?) -> Unit)?,
        clientDisconnectCallback: ((t: Throwable, response: Response?) -> Unit)?,
    ) {
        userWebsocket.createConnection(
            url,
            successCallback,
            failCallback,
            serverDisconnectCallback,
            clientDisconnectCallback
        )
    }

    override suspend fun removeChannel(
        onClosingConnection: ((code: Int, reason: String) -> Unit)?,
        onClosedConnection: ((code: Int, reason: String) -> Unit)?,
    ) {
        userWebsocket.removeConnection(onClosingConnection, onClosedConnection)
    }

    override suspend fun senData(
        data: WebsocketDataModel,
        onSendMessageFail: ((t: Throwable, response: Response?) -> Unit)?,
    ) {
        userWebsocket.sendData(data, onSendMessageFail)
    }

    override suspend fun receiveData(
        onReceiveTextMessage: ((text: String) -> Unit)?,
        onReceiveBinaryMessage: ((bytes: ByteString) -> Unit)?,
    ) {
        userWebsocket.receiveData(onReceiveTextMessage, onReceiveBinaryMessage)
    }

    override fun isChannelExist(): Boolean = userWebsocket.isConnected()
}