package ir.srp.rasad.presentation.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Build.VERSION_CODES.O
import android.os.Build.VERSION_CODES.Q
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.Constants.APP_STATE
import ir.srp.rasad.core.Constants.CANCEL_OBSERVE
import ir.srp.rasad.core.Constants.DENY_PERMISSION_ACTION
import ir.srp.rasad.core.Constants.DISCONNECT
import ir.srp.rasad.core.Constants.GRANT_PERMISSION_ACTION
import ir.srp.rasad.core.Constants.MESSENGER_TRANSFORMATION
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECTING
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_DENY_PERMISSION_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_STATE
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_DENY_PERMISSION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_GRANT_PERMISSION_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_GRANT_PERMISSION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_RECEIVE_REQUEST_PERMISSION
import ir.srp.rasad.core.Constants.OBSERVABLE_REQUEST_PERMISSION_DATA
import ir.srp.rasad.core.Constants.OBSERVABLE_SENDING_PERMISSION_RESPONSE
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_LOADING
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_PERMISSION_REQUEST
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_READY
import ir.srp.rasad.core.Constants.OBSERVER_CONNECTING
import ir.srp.rasad.core.Constants.OBSERVER_CONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_CONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_DISCONNECT_ALL_TARGETS
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_STATE
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_SENDING_REQUEST_DATA
import ir.srp.rasad.core.Constants.OBSERVER_SEND_REQUEST_DATA_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_SEND_REQUEST_DATA_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_STATE_LOADING
import ir.srp.rasad.core.Constants.OBSERVER_STATE_WAITING_RESPONSE
import ir.srp.rasad.core.Constants.SERVICE_BUNDLE
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
    private val observerTargets = HashSet<TargetModel>()
    private var requestPermissionData: WebsocketDataModel? = null

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

    @RequiresApi(Q)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = intent?.getBundleExtra(SERVICE_BUNDLE)
        val action = intent?.action
        bundle?.let { handleStartIntentData(it) }
        action?.let { handleAction(action) }

        return START_STICKY
    }

    @RequiresApi(O)
    override fun onBind(intent: Intent?): IBinder? {
        CoroutineScope(io).launch {
            transferWebsocketDataUseCase.receiveData(
                onReceiveTextMessage = { text -> onReceiveTextMessage(text) },
                onReceiveBinaryMessage = null
            )
        }
        return homeMessenger.binder
    }


    @RequiresApi(Q)
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
                else
                    bundle.getParcelableArray(SERVICE_DATA)

                for (item in data!!) {
                    val target = item as TargetModel
                    observerTargets.add(target)
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
                connectObserverToServer()
                state = OBSERVER_STATE_LOADING
            }

            STOP_SERVICE_OBSERVER -> {
                logOutObserver()
            }

            else -> Unit
        }
    }

    @RequiresApi(O)
    private fun handleAction(action: String) {
        when (action) {

            DENY_PERMISSION_ACTION -> {
                requestPermissionData?.username?.let { observableSendDenyData(it) }
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        getString(R.string.observable_login_success_msg),
                        true
                    )
                )
            }

            GRANT_PERMISSION_ACTION -> {
                requestPermissionData?.username?.let { observableSendGrantData(it) }
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        getString(R.string.observable_login_success_msg),
                        true
                    )
                )
            }
        }
    }

    @RequiresApi(O)
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
                        state = STATE_START
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
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            createNotification(
                                getString(R.string.app_name),
                                getString(R.string.observer_login_success_msg),
                                true
                            )
                        )
                        state = OBSERVER_STATE_WAITING_RESPONSE
                    }

                    WebSocketDataType.Deny.name -> {
                        sendSimpleMessageToHomeFragment(OBSERVABLE_DENY_PERMISSION_SUCCESS)
                    }

                    WebSocketDataType.Grant.name -> {
                        sendSimpleMessageToHomeFragment(
                            OBSERVABLE_GRANT_PERMISSION_SUCCESS,
                            requestPermissionData?.username
                        )
                        requestPermissionData = null
                    }
                }
            }

            WebSocketDataType.RequestPermission -> {
                if (data.data != null) {
                    sendSimpleMessageToHomeFragment(OBSERVABLE_RECEIVE_REQUEST_PERMISSION, data)
                    handleRequestPermission(data)
                }

                requestPermissionData = data
                state = OBSERVABLE_STATE_PERMISSION_REQUEST
            }

            WebSocketDataType.RequestData -> {
                observableTargets.add(data.username)
            }

            WebSocketDataType.Failed -> {

            }

            WebSocketDataType.LogOutObservable -> {
                for (target in observerTargets)
                    if (target.username == data.username) {
                        observerTargets.remove(target)
                        break
                    }
                if (observerTargets.size <= 0) {
                    sendSimpleMessageToHomeFragment(OBSERVER_DISCONNECT_ALL_TARGETS)
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        createNotification(
                            getString(R.string.app_name),
                            getString(R.string.observer_disconnect_all_targets_msg),
                            false
                        )
                    )
                    disconnectServer()
                    isServiceStarted = false
                    isObserverLogIn = false
                    state = STATE_START
                }
            }

            WebSocketDataType.LogOutObserver -> {
                observableTargets.remove(data.username)
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

    @RequiresApi(O)
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

    @RequiresApi(O)
    @SuppressLint("RemoteViewLayout")
    private fun createPermissionNotification(
        title: String,
        collapseMessage: String,
        extendMessage: String,
        onGoing: Boolean,
    ): Notification {
        val permissionNotificationView =
            RemoteViews(packageName, R.layout.permission_notification_view)
        permissionNotificationView.setTextViewText(
            R.id.permission_notification_message_txt,
            extendMessage
        )
        val denyIntent = Intent(this, MainService::class.java)
        denyIntent.action = DENY_PERMISSION_ACTION
        val denyPendingIntent =
            PendingIntent.getService(this, 1001, denyIntent, PendingIntent.FLAG_IMMUTABLE)
        permissionNotificationView.setOnClickPendingIntent(
            R.id.permission_notification_cancel_btn,
            denyPendingIntent
        )

        val grantIntent = Intent(this, MainService::class.java)
        grantIntent.action = GRANT_PERMISSION_ACTION
        val grantPendingIntent =
            PendingIntent.getService(this, 1002, grantIntent, PendingIntent.FLAG_IMMUTABLE)
        permissionNotificationView.setOnClickPendingIntent(
            R.id.permission_notification_ok_btn,
            grantPendingIntent
        )


        return NotificationCompat
            .Builder(this, CHANNEL_ID)
            .setOngoing(onGoing)
            .setContentTitle(title)
            .setContentText(collapseMessage)
            .setCustomBigContentView(permissionNotificationView)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }


    // Observable functions ------------------------------------------------------------------------
    @RequiresApi(O)
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

    @RequiresApi(O)
    private fun logOutObservable() {
        CoroutineScope(io).launch {
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.LogOutObservable,
                    username = username,
                    targets = observableTargets.toTypedArray(),
                    data = null
                ),
                onSendMessageFail = { _, _ -> observableSendMessageFailAction(WebSocketDataType.LogOutObservable) }
            )
        }
    }

    @RequiresApi(O)
    private fun observableConnectSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVABLE_CONNECT_SUCCESS)
        loginObservable()
    }

    @RequiresApi(O)
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
        state = STATE_START
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
        state = STATE_START
        observableTargets.clear()
    }

    @RequiresApi(O)
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
                state = STATE_START
            }

            WebSocketDataType.LogOutObservable -> {
                sendSimpleMessageToHomeFragment(OBSERVABLE_LOGOUT_FAIL)
            }

            WebSocketDataType.Deny -> {
                sendSimpleMessageToHomeFragment(OBSERVABLE_DENY_PERMISSION_FAIL)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createNotification(
                        getString(R.string.app_name),
                        getString(R.string.observable_login_success_msg),
                        true
                    )
                )
            }

            WebSocketDataType.Grant -> {
                sendSimpleMessageToHomeFragment(
                    OBSERVABLE_GRANT_PERMISSION_FAIL,
                    requestPermissionData
                )
                requestPermissionData?.let { handleRequestPermission(it) }
            }

            else -> Unit
        }
    }

    @RequiresApi(O)
    private fun handleRequestPermission(data: WebsocketDataModel) {
        var interval = 0
        val permissionsList = data.data?.split(",")
        for (permission in permissionsList!!) {
            if (permission.contains(username))
                interval = permission.replace(username, "").toInt()
        }

        val msg = if (interval == 0)
            "${data.username} want to track your location when it changes.\nAre you ok ?"
        else
            "${data.username} want to track your location every $interval minutes.\nAre you ok ?"

        val permissionNotification = createPermissionNotification(
            getString(R.string.app_name),
            "Incoming Request permission from ${data.username}.",
            msg,
            true
        )

        notificationManager.notify(
            NOTIFICATION_ID,
            permissionNotification
        )
    }

    @RequiresApi(O)
    private fun observableSendDenyData(target: String) {
        CoroutineScope(io).launch {
            sendSimpleMessageToHomeFragment(OBSERVABLE_SENDING_PERMISSION_RESPONSE)
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.Deny,
                    username = username,
                    targets = arrayOf(target),
                    data = null
                ),
                onSendMessageFail = { _, _ ->
                    observableSendMessageFailAction(WebSocketDataType.Deny)
                }
            )
        }
    }

    @RequiresApi(O)
    private fun observableSendGrantData(target: String) {
        CoroutineScope(io).launch {
            sendSimpleMessageToHomeFragment(OBSERVABLE_SENDING_PERMISSION_RESPONSE)
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.Grant,
                    username = username,
                    targets = arrayOf(target),
                    data = null
                ),
                onSendMessageFail = { t, _ ->
                    observableSendMessageFailAction(WebSocketDataType.Grant)
                }
            )
        }
    }


    // Observer functions ------------------------------------------------------------------------
    private fun logOutObserver() {
        val targetsUserName =
            observerTargets.map { targetModel -> targetModel.username }.toTypedArray()
        CoroutineScope(io).launch {
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.LogOutObserver,
                    username = username,
                    targets = targetsUserName,
                    data = null
                ),
                onSendMessageFail = { _, _ -> observerSendMessageFailAction(WebSocketDataType.LogOutObserver) }
            )
        }
    }

    private fun observerLoginSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_LOGIN_SUCCESS)
        isObserverLogIn = true
        observerSendRequestData(observerTargets.toTypedArray())
    }

    private fun observerSendRequestData(targets: Array<TargetModel>) {
        val targetsUserName =
            targets.map { targetModel -> targetModel.username }.toTypedArray()
        val data = StringBuffer("")
        for (target in targets)
            data.append(target.username + target.permissions.coordinate + ",")

        CoroutineScope(io).launch {
            sendSimpleMessageToHomeFragment(OBSERVER_SENDING_REQUEST_DATA)
            transferWebsocketDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.RequestData,
                    username = username,
                    targets = targetsUserName,
                    data = data.toString()
                ),
                onSendMessageFail = { _, _ ->
                    observerSendMessageFailAction(WebSocketDataType.RequestData)
                }
            )
        }
    }

    private fun connectObserverToServer() {
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
        state = STATE_START
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
        state = STATE_START
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
        state = STATE_START
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
        state = STATE_START
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
        state = STATE_START
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

                OBSERVABLE_REQUEST_PERMISSION_DATA -> {
                    sendSimpleMessageToHomeFragment(
                        OBSERVABLE_RECEIVE_REQUEST_PERMISSION,
                        requestPermissionData
                    )
                }

                else -> super.handleMessage(msg)
            }
        }
    }
}