package ir.srp.rasad.domain.usecases.websocket_usecase

import ir.srp.rasad.domain.models.WebsocketDataModel
import ir.srp.rasad.domain.repositories.UserRepo
import okhttp3.Response
import okio.ByteString
import javax.inject.Inject

class TransferWebsocketDataUseCase @Inject constructor(private val userRepo: UserRepo) {

    suspend fun sendData(
        data: WebsocketDataModel,
        onSendMessageFail: ((t: Throwable, response: Response?) -> Unit)?,
    ) = userRepo.senData(data, onSendMessageFail)

    suspend fun receiveData(
        onReceiveTextMessage: ((text: String) -> Unit)?,
        onReceiveBinaryMessage: ((bytes: ByteString) -> Unit)?,
    ) = userRepo.receiveData(onReceiveTextMessage, onReceiveBinaryMessage)
}