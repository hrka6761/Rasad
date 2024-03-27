package ir.srp.rasad.domain.usecases.track_usecases

import ir.srp.rasad.domain.models.WebsocketDataModel
import ir.srp.rasad.domain.repositories.TrackRepo
import okhttp3.Response
import okio.ByteString
import javax.inject.Inject

class TransferTrackDataUseCase @Inject constructor(private val trackRepo: TrackRepo) {

    suspend fun sendData(
        data: WebsocketDataModel,
        onSendMessageFail: ((t: Throwable, response: Response?) -> Unit)?,
    ) = trackRepo.senData(data, onSendMessageFail)

    suspend fun receiveData(
        onReceiveTextMessage: ((text: String) -> Unit)?,
        onReceiveBinaryMessage: ((bytes: ByteString) -> Unit)?,
    ) = trackRepo.receiveData(onReceiveTextMessage, onReceiveBinaryMessage)
}