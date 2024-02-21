package ir.srp.rasad.presentation.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.Constants.SERVICE_INTENT_DATA
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.WEBSOCKET_URL
import ir.srp.rasad.core.WebSocketDataType
import ir.srp.rasad.domain.models.WebsocketDataModel
import ir.srp.rasad.domain.usecases.preference_usecase.UserInfoUseCase
import ir.srp.rasad.domain.usecases.websocket_usecase.TransferWebsocketDataUseCase
import ir.srp.rasad.domain.usecases.websocket_usecase.WebSocketConnectionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class MainService : Service() {

    private val CHANNEL_ID = "Rasad"
    private val NOTIFICATION_ID = 110
    private lateinit var notificationChannel: NotificationChannel
    private val serviceMessenger: Messenger? = null
    private lateinit var homeMessenger: Messenger
    @Inject lateinit var notificationManager: NotificationManager
    @Named("IO") @Inject lateinit var io: CoroutineDispatcher
    @Inject lateinit var webSocketConnectionUseCase: WebSocketConnectionUseCase
    @Inject lateinit var transferWebsocketDataUseCase: TransferWebsocketDataUseCase
    @Inject lateinit var userInfoUseCase: UserInfoUseCase
    private lateinit var username: String
    private lateinit var userToken: String
    private lateinit var userId: String


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        CoroutineScope(io).launch {
            val userModel = userInfoUseCase.loadUserAccountInfo().data
            userModel?.let {
                username = it.username.toString()
                userToken = it.token.toString()
                userId = it.id.toString()
            }
        }
        homeMessenger = Messenger(Handler())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val data = intent?.getStringExtra(SERVICE_INTENT_DATA)
        data?.let { handleStartIntentData(it) }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = homeMessenger.binder

    override fun onDestroy() {
        super.onDestroy()
    }


    private fun handleStartIntentData(data: String) {
        when (data) {
            START_SERVICE_OBSERVABLE -> {
                startForeground(
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        getString(R.string.start_observable_notification_msg),
                        true
                    )
                )

                connectObservable()
            }

            STOP_SERVICE_OBSERVABLE -> {
                stopForeground(STOP_FOREGROUND_REMOVE)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        getString(R.string.stop_observable_notification_msg),
                        false
                    )
                )

                disconnectObservable()
            }
            else -> {}
        }
    }

    private fun connectObservable() {
        CoroutineScope(io).launch {
            webSocketConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { loginObservable() },
                failCallback = null
            )
        }
    }

    private fun loginObservable() {
        CoroutineScope(io).launch {
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.LogInObservable,
                    username = username,
                    targets = null,
                    data = "$userId,$userToken"
                ),
                onSendMessageFail = { t, response ->

                })
        }
    }

    private fun disconnectObservable() {
        CoroutineScope(io).launch {
            webSocketConnectionUseCase.removeChannel(
                onClosingConnection = null,
                onClosedConnection = null
            )
        }
    }

    private fun createNotification(title: String, message: String, onGoing: Boolean): Notification {
        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setOngoing(onGoing)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
    }


    @SuppressLint("HandlerLeak")
    private inner class Handler : android.os.Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg) {
                else -> super.handleMessage(msg)
            }
        }
    }
}