package ir.srp.rasad.presentation.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.Service
import android.content.Intent
import android.os.Build.VERSION.*
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.Constants.MESSENGER_TRANSFORMATION
import ir.srp.rasad.core.Constants.OBSERVABLE_CLOSED_CONNECTION
import ir.srp.rasad.core.Constants.OBSERVABLE_CLOSING_CONNECTION
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECTION_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECTION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_SEND_MESSAGE_FAIL
import ir.srp.rasad.core.Constants.SERVICE_DATA
import ir.srp.rasad.core.Constants.SERVICE_BUNDLE
import ir.srp.rasad.core.Constants.SERVICE_TYPE
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVER
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVER
import ir.srp.rasad.core.Constants.WEBSOCKET_URL
import ir.srp.rasad.core.WebSocketDataType
import ir.srp.rasad.domain.models.TargetModel
import ir.srp.rasad.domain.models.WebsocketDataModel
import ir.srp.rasad.domain.usecases.preference_usecase.UserInfoUseCase
import ir.srp.rasad.domain.usecases.websocket_usecase.TransferWebsocketDataUseCase
import ir.srp.rasad.domain.usecases.websocket_usecase.WebSocketConnectionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@Suppress("DEPRECATION")
@AndroidEntryPoint
class MainService : Service() {

    private val CHANNEL_ID = "Rasad"
    private val NOTIFICATION_ID = 110
    private lateinit var notificationChannel: NotificationChannel
    private var serviceMessenger: Messenger? = null
    private lateinit var homeMessenger: Messenger
    @Inject lateinit var notificationManager: NotificationManager
    @Named("IO") @Inject lateinit var io: CoroutineDispatcher
    @Inject lateinit var webSocketConnectionUseCase: WebSocketConnectionUseCase
    @Inject lateinit var transferWebsocketDataUseCase: TransferWebsocketDataUseCase
    @Inject lateinit var userInfoUseCase: UserInfoUseCase
    private lateinit var username: String
    private lateinit var userToken: String
    private lateinit var userId: String


    @RequiresApi(O)
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
        val bundle = intent?.getBundleExtra(SERVICE_BUNDLE)
        bundle?.let { handleStartIntentData(it) }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = homeMessenger.binder


    private fun handleStartIntentData(bundle: Bundle) {
        when (bundle.getString(SERVICE_TYPE)) {
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
                disconnectObservable()
            }

            START_SERVICE_OBSERVER -> {
                val data = if (SDK_INT >= TIRAMISU)
                    bundle.getParcelableArray(SERVICE_DATA, TargetModel::class.java)
                else
                    bundle.getParcelableArray(SERVICE_DATA)
            }

            STOP_SERVICE_OBSERVER -> {}

            else -> Unit
        }
    }

    private fun connectObservable() {
        CoroutineScope(io).launch {
            webSocketConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { observableConnectionSuccessAction() },
                failCallback = { _, _ -> observableConnectionFailAction() }
            )
        }
    }

    private fun disconnectObservable() {
        CoroutineScope(io).launch {
            webSocketConnectionUseCase.removeChannel(
                onClosingConnection = { _, _ -> observableClosingConnectionAction() },
                onClosedConnection = { _, _ -> observableClosedConnectionAction() }
            )
        }
    }

    private fun observableConnectionSuccessAction() {
        loginObservable()
        sendSimpleMessageToHomeFragment(OBSERVABLE_CONNECTION_SUCCESS)
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
                onSendMessageFail = { _, _ ->
                    observableSendMessageFailAction(WebSocketDataType.LogInObservable)
                }
            )
        }
    }

    private fun observableConnectionFailAction() {
        sendSimpleMessageToHomeFragment(OBSERVABLE_CONNECTION_FAIL)
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.stop_observable_notification_msg),
                false
            )
        )
    }

    private fun observableSendMessageFailAction(type: WebSocketDataType) {
        when (type) {
            WebSocketDataType.LogInObservable -> {
                sendSimpleMessageToHomeFragment(OBSERVABLE_SEND_MESSAGE_FAIL, obj = type.name)
                disconnectObservable()
            }
            else -> Unit
        }
    }

    private fun observableClosingConnectionAction() {
        sendSimpleMessageToHomeFragment(OBSERVABLE_CLOSING_CONNECTION)
    }

    private fun observableClosedConnectionAction() {
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.stop_observable_notification_msg),
                false
            )
        )
        sendSimpleMessageToHomeFragment(OBSERVABLE_CLOSED_CONNECTION)
    }

    private fun sendSimpleMessageToHomeFragment(msg: Int, obj: Any? = null) {
        val message = Message.obtain(null, msg, obj)
        serviceMessenger?.send(message)
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

    @RequiresApi(O)
    private fun createNotificationChannel() {
        notificationChannel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)
    }


    @SuppressLint("HandlerLeak")
    private inner class Handler : android.os.Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSENGER_TRANSFORMATION -> { serviceMessenger = msg.replyTo }
                else -> super.handleMessage(msg)
            }
        }
    }
}