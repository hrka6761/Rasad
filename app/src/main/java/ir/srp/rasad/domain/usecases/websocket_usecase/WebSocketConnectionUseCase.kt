package ir.srp.rasad.domain.usecases.websocket_usecase

import ir.srp.rasad.domain.repositories.UserRepo
import okhttp3.Response
import javax.inject.Inject

class WebSocketConnectionUseCase @Inject constructor(private val userRepo: UserRepo) {

    suspend fun createChannel(
        url: String,
        successCallback: ((response: Response) -> Unit)?,
        failCallback: ((t: Throwable, response: Response?) -> Unit)?,
        serverDisconnectCallback: ((t: Throwable, response: Response?) -> Unit)?,
        clientDisconnectCallback: ((t: Throwable, response: Response?) -> Unit)?,
    ) = userRepo.createChannel(
        url,
        successCallback,
        failCallback,
        serverDisconnectCallback,
        clientDisconnectCallback
    )

    suspend fun removeChannel(
        onClosingConnection: ((code: Int, reason: String) -> Unit)?,
        onClosedConnection: ((code: Int, reason: String) -> Unit)?,
    ) = userRepo.removeChannel(onClosingConnection, onClosedConnection)

    fun isChannelExist() = userRepo.isChannelExist()
}