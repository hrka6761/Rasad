package ir.srp.rasad.presentation.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.Constants
import ir.srp.rasad.core.Constants.APP_STATE
import ir.srp.rasad.core.Constants.CANCEL_OBSERVE
import ir.srp.rasad.core.Constants.DISCONNECT
import ir.srp.rasad.core.Constants.MESSENGER_TRANSFORMATION
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECTING
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_STATE
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_LOADING
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_READY
import ir.srp.rasad.core.Constants.OBSERVER_CONNECTING
import ir.srp.rasad.core.Constants.OBSERVER_CONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_CONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_STATE
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_SENDING_REQUEST_DATA
import ir.srp.rasad.core.Constants.OBSERVER_SEND_REQUEST_DATA_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_SEND_REQUEST_DATA_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_STATE_LOADING
import ir.srp.rasad.core.Constants.OBSERVER_STATE_WAITING_RESPONSE
import ir.srp.rasad.core.Constants.SERVICE_DATA
import ir.srp.rasad.core.Constants.SERVICE_STATE
import ir.srp.rasad.core.Constants.SERVICE_TYPE
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVER
import ir.srp.rasad.core.Constants.STATE_START
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVER
import ir.srp.rasad.core.Constants.WEBSOCKET_URL
import ir.srp.rasad.core.WebSocketDataType
import ir.srp.rasad.core.utils.JsonConverter
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

@AndroidEntryPoint
class MainService : Service() {

    private val TAG = "hamidreza"
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
    @Inject lateinit var jsonConverter: JsonConverter
    private lateinit var username: String
    private lateinit var userToken: String
    private lateinit var userId: String
    private var state = STATE_START
    private var isServiceStarted = false
    private var isObservableLogIn = false
    private var isObserverLogIn = false
    private val observableTargets = HashSet<String>()
    private val observerTargets = HashSet<String>()

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

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = intent?.getBundleExtra(Constants.SERVICE_BUNDLE)
        bundle?.let { handleStartIntentData(it) }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        CoroutineScope(io).launch {
            transferWebsocketDataUseCase.receiveData(
                onReceiveTextMessage = { text -> onReceiveTextMessage(text) },
                onReceiveBinaryMessage = null
            )
        }
        return homeMessenger.binder
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    private fun handleStartIntentData(bundle: Bundle) {
        when (bundle.getString(SERVICE_TYPE)) {

            START_SERVICE_OBSERVABLE -> {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        getString(R.string.connecting_msg),
                        true
                    ),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )

                isServiceStarted = true
                connectObservableToServer()
                state = OBSERVABLE_STATE_LOADING
            }

            STOP_SERVICE_OBSERVABLE -> {
                logOutObservable()
            }

            START_SERVICE_OBSERVER -> {
                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    bundle.getParcelableArray(SERVICE_DATA, TargetModel::class.java)
                else {
                    bundle.getParcelableArray(SERVICE_DATA)
                }

                for (item in data!!) {
                    val target = item as TargetModel
                    observerTargets.add(target.username)
                }

                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        getString(R.string.connecting_msg),
                        true
                    ),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )

                isServiceStarted = true
                connectObserver()
                state = OBSERVER_STATE_LOADING
            }

            STOP_SERVICE_OBSERVER -> {
                logOutObserver()
            }

            else -> Unit
        }
    }

    private fun onReceiveTextMessage(text: String) {
        val data =
            jsonConverter.convertJsonStringToObject(
                text,
                WebsocketDataModel::class.java
            ) as WebsocketDataModel

        when (data.type) {

            WebSocketDataType.Confirmation -> {
                when (data.data) {

                    WebSocketDataType.LogInObservable.name -> {
                        sendSimpleMessageToHomeFragment(OBSERVABLE_LOGIN_SUCCESS)
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            createNotification(
                                getString(R.string.app_name),
                                getString(R.string.observable_login_success_msg),
                                true
                            )
                        )
                        isObservableLogIn = true
                        state = OBSERVABLE_STATE_READY
                    }

                    WebSocketDataType.LogOutObservable.name -> {
                        sendSimpleMessageToHomeFragment(OBSERVABLE_LOGOUT_SUCCESS)
                        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            createNotification(
                                getString(R.string.app_name),
                                getString(R.string.observable_logout_success_msg),
                                false
                            )
                        )
                        disconnectServer()
                        isServiceStarted = false
                        isObservableLogIn = false
                    }

                    WebSocketDataType.LogInObserver.name -> {
                        observerLoginSuccessAction()
                    }

                    WebSocketDataType.LogOutObserver.name -> {

                    }

                    WebSocketDataType.RequestData.name -> {
                        sendSimpleMessageToHomeFragment(OBSERVER_SEND_REQUEST_DATA_SUCCESS)
                        state = OBSERVER_STATE_WAITING_RESPONSE
                    }
                }
            }

            WebSocketDataType.Failed -> {
                Log.i(TAG, "fail: $data")
            }

            else -> Unit
        }
    }

    private fun disconnectServer() {
        CoroutineScope(io).launch {
            webSocketConnectionUseCase.removeChannel(
                onClosingConnection = null,
                onClosedConnection = null
            )
        }
    }

    private fun sendSimpleMessageToHomeFragment(msg: Int, obj: Any? = null) {
        val message = Message.obtain(null, msg, obj)
        serviceMessenger?.send(message)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        notificationChannel = NotificationChannel(
            CHANNEL_ID, CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(notificationChannel)
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


    // Observable functions ------------------------------------------------------------------------
    private fun connectObservableToServer() {
        sendSimpleMessageToHomeFragment(OBSERVABLE_CONNECTING)
        CoroutineScope(io).launch {
            webSocketConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { observableConnectSuccessAction() },
                failCallback = { _, _ -> observableConnectFailAction() },
                serverDisconnectCallback = { _, _ -> observableDisconnectAction() },
                clientDisconnectCallback = { _, _ -> observableDisconnectAction() }
            )
        }
    }

    private fun logOutObservable() {
        CoroutineScope(io).launch {
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.LogOutObservable,
                    username = username,
                    targets = null,
                    data = null
                ),
                onSendMessageFail = { _, _ -> observableSendMessageFailAction(WebSocketDataType.LogOutObservable) }
            )
        }
    }

    private fun observableConnectSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVABLE_CONNECT_SUCCESS)
        loginObservable()
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

    private fun observableConnectFailAction() {
        sendSimpleMessageToHomeFragment(OBSERVABLE_CONNECT_FAIL)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.observable_connect_fail_msg),
                false
            )
        )
        isServiceStarted = false
    }

    private fun observableDisconnectAction() {
        sendSimpleMessageToHomeFragment(DISCONNECT)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.disconnect_msg),
                false
            )
        )
        isServiceStarted = false
        isObservableLogIn = false
    }

    private fun observableSendMessageFailAction(type: WebSocketDataType) {
        when (type) {
            WebSocketDataType.LogInObservable -> {
                sendSimpleMessageToHomeFragment(OBSERVABLE_LOGIN_FAIL)
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        getString(R.string.observable_login_fail_msg),
                        false
                    )
                )
                disconnectServer()
                isServiceStarted = false
            }

            WebSocketDataType.LogOutObservable -> {
                sendSimpleMessageToHomeFragment(OBSERVABLE_LOGOUT_FAIL)
            }

            else -> Unit
        }
    }


    // Observer functions ------------------------------------------------------------------------
    private fun logOutObserver() {
        CoroutineScope(io).launch {
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.LogOutObserver,
                    username = username,
                    targets = observerTargets.toTypedArray(),
                    data = null
                ),
                onSendMessageFail = { _, _ -> observerSendMessageFailAction(WebSocketDataType.LogOutObserver) }
            )
        }
    }

    private fun observerLoginSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_LOGIN_SUCCESS)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.observer_login_success_msg),
                true
            )
        )
        isObserverLogIn = true

        observerSendRequestData(observerTargets.toTypedArray())
    }

    private fun observerSendRequestData(targets: Array<String>) {
        CoroutineScope(io).launch {
            sendSimpleMessageToHomeFragment(OBSERVER_SENDING_REQUEST_DATA)
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.RequestData,
                    username = username,
                    targets = targets,
                    data = null
                ),
                onSendMessageFail = { _, _ ->
                    observerSendMessageFailAction(WebSocketDataType.RequestData)
                }
            )
        }
    }

    private fun connectObserver() {
        sendSimpleMessageToHomeFragment(OBSERVER_CONNECTING)
        CoroutineScope(io).launch {
            webSocketConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { observerConnectSuccessAction() },
                failCallback = { _, _ -> observerConnectFailAction() },
                serverDisconnectCallback = { _, _ -> observerDisconnectAction() },
                clientDisconnectCallback = { _, _ -> observerDisconnectAction() }
            )
        }
    }

    private fun observerConnectSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_CONNECT_SUCCESS)
        loginObserver()
    }

    private fun loginObserver() {
        CoroutineScope(io).launch {
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.LogInObserver,
                    username = username,
                    targets = null,
                    data = "$userId,$userToken"
                ),
                onSendMessageFail = { _, _ ->
                    observerSendMessageFailAction(WebSocketDataType.LogInObserver)
                }
            )
        }
    }

    private fun observerConnectFailAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_CONNECT_FAIL)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.observer_connect_fail_msg),
                false
            )
        )
        isServiceStarted = false
        observerTargets.clear()
    }

    private fun observerDisconnectAction() {
        sendSimpleMessageToHomeFragment(DISCONNECT)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        isServiceStarted = false
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.disconnect_msg),
                false
            )
        )
        isServiceStarted = false
        isObserverLogIn = false
        observerTargets.clear()
    }

    private fun observerSendMessageFailAction(type: WebSocketDataType) {
        when (type) {
            WebSocketDataType.LogInObserver -> {
                observerLoginFailAction()
            }

            WebSocketDataType.LogOutObserver -> {

            }

            WebSocketDataType.RequestData -> {
                observerRequestDataFailAction()
            }

            else -> Unit
        }
    }

    private fun observerLoginFailAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_LOGIN_FAIL)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.observer_login_fail_msg),
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        observerTargets.clear()
    }

    private fun observerRequestDataFailAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_SEND_REQUEST_DATA_FAIL)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.observer_send_request_data_fail_msg),
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        isObserverLogIn = false
        observerTargets.clear()
    }

    private fun cancelObservation() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.cancel_observe_msg),
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        isObserverLogIn = false
        observerTargets.clear()
    }


    @SuppressLint("HandlerLeak")
    private inner class Handler : android.os.Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSENGER_TRANSFORMATION -> {
                    serviceMessenger = msg.replyTo
                    val appStateMsg =
                        Message.obtain(null, APP_STATE, state)
                    val observableLoginStateMsg =
                        Message.obtain(null, OBSERVABLE_LOGIN_STATE, isObservableLogIn)
                    val observerLoginStateMsg =
                        Message.obtain(null, OBSERVER_LOGIN_STATE, isObserverLogIn)
                    val serviceStateMsg =
                        Message.obtain(null, SERVICE_STATE, isServiceStarted)
                    serviceMessenger?.send(appStateMsg)
                    serviceMessenger?.send(observableLoginStateMsg)
                    serviceMessenger?.send(observerLoginStateMsg)
                    serviceMessenger?.send(serviceStateMsg)
                }

                CANCEL_OBSERVE -> {
                    cancelObservation()
                }

                else -> super.handleMessage(msg)
            }
        }
    }
}