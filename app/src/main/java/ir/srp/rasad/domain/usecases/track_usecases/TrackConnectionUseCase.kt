package ir.srp.rasad.domain.usecases.track_usecases

import ir.srp.rasad.domain.repositories.TrackRepo
import okhttp3.Response
import javax.inject.Inject

class TrackConnectionUseCase @Inject constructor(private val trackRepo: TrackRepo) {

    suspend fun createChannel(
        url: String,
        successCallback: ((response: Response) -> Unit)?,
        failCallback: ((t: Throwable?, response: Response?) -> Unit)?,
        serverDisconnectCallback: ((t: Throwable?, response: Response?) -> Unit)?,
        clientDisconnectCallback: ((t: Throwable?, response: Response?) -> Unit)?,
        pingAttemptCount: Int,
        pingAttemptInterval: Long,
    ) = trackRepo.createChannel(
        url,
        successCallback,
        failCallback,
        serverDisconnectCallback,
        clientDisconnectCallback,
        pingAttemptCount,
        pingAttemptInterval,
    )

    suspend fun removeChannel(
        onClosingConnection: ((code: Int, reason: String) -> Unit)?,
        onClosedConnection: ((code: Int, reason: String) -> Unit)?,
    ) = trackRepo.removeChannel(onClosingConnection, onClosedConnection)

    fun isChannelExist() = trackRepo.isChannelExist()
}