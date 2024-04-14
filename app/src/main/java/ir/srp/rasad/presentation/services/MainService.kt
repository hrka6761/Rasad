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
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.Constants.APP_STATE
import ir.srp.rasad.core.Constants.CANCEL_OBSERVE
import ir.srp.rasad.core.Constants.CANCEL_RECONNECT_OBSERVABLE
import ir.srp.rasad.core.Constants.CANCEL_RECONNECT_OBSERVER
import ir.srp.rasad.core.Constants.DENY_PERMISSION_ACTION
import ir.srp.rasad.core.Constants.DISCONNECT
import ir.srp.rasad.core.Constants.GRANT_PERMISSION_ACTION
import ir.srp.rasad.core.Constants.OBSERVER_LAST_RECEIVED_DATA
import ir.srp.rasad.core.Constants.MESSENGER_TRANSFORMATION
import ir.srp.rasad.core.Constants.OBSERVABLE_ADDED_NEW_OBSERVER
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECTING
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_DENY_PERMISSION_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_DENY_PERMISSION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_DISCONNECT_ALL_TARGETS
import ir.srp.rasad.core.Constants.OBSERVABLE_GRANT_PERMISSION_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_GRANT_PERMISSION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_STATE
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_RECEIVE_REQUEST_PERMISSION
import ir.srp.rasad.core.Constants.OBSERVABLE_GET_PERMISSION_DATA
import ir.srp.rasad.core.Constants.OBSERVABLE_RECONNECTING
import ir.srp.rasad.core.Constants.OBSERVABLE_RECONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_RECONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_REQUEST_TARGETS
import ir.srp.rasad.core.Constants.OBSERVABLE_SENDING_PERMISSION_RESPONSE
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_LOADING
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_PERMISSION_REQUEST
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_READY
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_RELOADING
import ir.srp.rasad.core.Constants.OBSERVABLE_STATE_SENDING_DATA
import ir.srp.rasad.core.Constants.OBSERVER_CONNECTING
import ir.srp.rasad.core.Constants.OBSERVER_CONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_CONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_DISCONNECT_ALL_TARGETS
import ir.srp.rasad.core.Constants.OBSERVER_FAILURE
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_STATE
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_RECEIVE_DATA
import ir.srp.rasad.core.Constants.OBSERVER_RECONNECTING
import ir.srp.rasad.core.Constants.OBSERVER_RECONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_RECONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_SENDING_REQUEST_DATA
import ir.srp.rasad.core.Constants.OBSERVER_SEND_REQUEST_DATA_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_SEND_REQUEST_DATA_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_STATE_LOADING
import ir.srp.rasad.core.Constants.OBSERVER_STATE_RECEIVING_DATA
import ir.srp.rasad.core.Constants.OBSERVER_STATE_WAITING_RESPONSE
import ir.srp.rasad.core.Constants.OBSERVER_REQUEST_LAST_RECEIVED_DATA
import ir.srp.rasad.core.Constants.OBSERVER_STATE_RELOADING
import ir.srp.rasad.core.Constants.RECONNECT_INTERVAL
import ir.srp.rasad.core.Constants.SERVICE_BUNDLE_KEY
import ir.srp.rasad.core.Constants.SERVICE_DATA_KEY
import ir.srp.rasad.core.Constants.SERVICE_STATE
import ir.srp.rasad.core.Constants.SERVICE_TYPE_KEY
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVER
import ir.srp.rasad.core.Constants.STATE_DISABLE
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVER
import ir.srp.rasad.core.Constants.WEBSOCKET_URL
import ir.srp.rasad.core.WebSocketDataType
import ir.srp.rasad.core.utils.JsonConverter
import ir.srp.rasad.domain.models.DataModel
import ir.srp.rasad.domain.models.ErrorDataModel
import ir.srp.rasad.domain.models.TargetModel
import ir.srp.rasad.domain.models.WebsocketDataModel
import ir.srp.rasad.domain.usecases.preference_usecases.UserInfoUseCase
import ir.srp.rasad.domain.usecases.track_usecases.TransferTrackDataUseCase
import ir.srp.rasad.domain.usecases.track_usecases.TrackConnectionUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@Suppress("DEPRECATION", "UNCHECKED_CAST")
@RequiresApi(S)
@SuppressLint("HandlerLeak")
@AndroidEntryPoint
class MainService : Service() {

    @Inject
    lateinit var notificationManager: NotificationManager

    @Named("IO")
    @Inject
    lateinit var io: CoroutineDispatcher

    @Inject
    lateinit var trackConnectionUseCase: TrackConnectionUseCase

    @Inject
    lateinit var transferTrackDataUseCase: TransferTrackDataUseCase

    @Inject
    lateinit var userInfoUseCase: UserInfoUseCase

    @Inject
    lateinit var jsonConverter: JsonConverter
    private val CHANNEL_ID = "Rasad"
    private val NOTIFICATION_ID = 110
    private lateinit var notificationChannel: NotificationChannel
    private var serviceMessenger: Messenger? = null
    private lateinit var homeMessenger: Messenger
    private lateinit var username: String
    private lateinit var userToken: String
    private lateinit var userId: String
    private var state = STATE_DISABLE
    private var isServiceStarted = false
    private var isObservableLogIn = false
    private var isObserverLogIn = false
    private var isObserverTrackStarted = false
    private val observableTargets = HashSet<String>()
    private val observerTargets = HashSet<TargetModel>()
    private var requestPermissionData: WebsocketDataModel? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationCallback = getLocationCallback()
    private var lastReceivedData: DataModel? = null
    private var isReconnectCanceled = false


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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = intent?.getBundleExtra(SERVICE_BUNDLE_KEY)
        val action = intent?.action
        bundle?.let { handleStartIntentData(it) }
        action?.let { handleAction(action) }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        CoroutineScope(io).launch {
            transferTrackDataUseCase.receiveData(
                onReceiveTextMessage = { text -> onReceiveTextMessage(text) },
                onReceiveBinaryMessage = null
            )
        }
        return homeMessenger.binder
    }


    // Common functions ----------------------------------------------------------------------------

    private fun handleStartIntentData(bundle: Bundle) {
        when (bundle.getString(SERVICE_TYPE_KEY)) {

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
            }

            STOP_SERVICE_OBSERVABLE -> {
                logOutObservable()
            }

            START_SERVICE_OBSERVER -> {
                val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    bundle.getParcelableArray(SERVICE_DATA_KEY, TargetModel::class.java)
                else
                    bundle.getParcelableArray(SERVICE_DATA_KEY)

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
            }

            STOP_SERVICE_OBSERVER -> {
                logOutObserver()
            }

            else -> Unit
        }
    }

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
                state = OBSERVABLE_STATE_READY
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
                state = OBSERVABLE_STATE_READY
            }
        }
    }

    @SuppressLint("StringFormatMatches")
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
                        isObservableLogIn = true
                        state = OBSERVABLE_STATE_READY
                        sendSimpleMessageToHomeFragment(OBSERVABLE_LOGIN_SUCCESS)
                        notificationManager.notify(
                            NOTIFICATION_ID,
                            createNotification(
                                getString(R.string.app_name),
                                getString(R.string.observable_login_success_msg),
                                true
                            )
                        )
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
                        state = STATE_DISABLE
                        isServiceStarted = false
                        isObservableLogIn = false
                        observableTargets.clear()
                    }

                    WebSocketDataType.LogInObserver.name -> {
                        observerLoginSuccessAction()
                    }

                    WebSocketDataType.LogOutObserver.name -> {
                        isObserverTrackStarted = false
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

                    WebSocketDataType.Data.name -> {

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
                data.data?.let {
                    observableSendLocationData(it)
                }
            }

            WebSocketDataType.Failed -> {
                val errorData = data.data?.let {
                    jsonConverter.convertJsonStringToObject(
                        it,
                        ErrorDataModel::class.java
                    ) as ErrorDataModel
                }
                sendSimpleMessageToHomeFragment(OBSERVER_FAILURE, errorData)
                cancelObservation("${errorData?.code}: ${errorData?.reason}")
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
                    state = STATE_DISABLE
                }
            }

            WebSocketDataType.LogOutObserver -> {
                observableTargets.remove(data.username)
                if (observableTargets.size <= 0)
                    sendSimpleMessageToHomeFragment(OBSERVABLE_DISCONNECT_ALL_TARGETS)

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

            WebSocketDataType.Data -> {
                val dataModel = data.data?.let {
                    jsonConverter.convertJsonStringToObject(
                        it,
                        DataModel::class.java
                    )
                } as DataModel
                lastReceivedData = dataModel
                sendSimpleMessageToHomeFragment(OBSERVER_RECEIVE_DATA, dataModel)
                state = OBSERVER_STATE_RECEIVING_DATA

                if (!isObserverTrackStarted) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        createNotification(
                            getString(R.string.app_name),
                            getString(R.string.observer_receive_data_msg, dataModel.targetUsername),
                            true
                        )
                    )
                    isObserverTrackStarted = true
                }
            }

            else -> Unit
        }
    }

    private fun disconnectServer() {
        CoroutineScope(io).launch {
            trackConnectionUseCase.removeChannel(
                onClosingConnection = null,
                onClosedConnection = null
            )
        }
    }

    private fun sendSimpleMessageToHomeFragment(msg: Int, obj: Any? = null) {
        val message = Message.obtain(null, msg, obj)
        serviceMessenger?.send(message)
    }

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
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }

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
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .setCustomBigContentView(permissionNotificationView)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }


    // Observable functions ------------------------------------------------------------------------

    private fun connectObservableToServer() {
        CoroutineScope(io).launch {
            trackConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { observableConnectSuccessAction() },
                failCallback = { _, _ -> observableConnectFailAction() },
                serverDisconnectCallback = { _, _ -> observableDisconnectAction() },
                clientDisconnectCallback = { _, _ -> observableDisconnectAction() }
            )
        }
        sendSimpleMessageToHomeFragment(OBSERVABLE_CONNECTING)
        state = OBSERVABLE_STATE_LOADING
    }

    private fun reconnectObservableToServer() {
        CoroutineScope(io).launch {
            trackConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { observableReconnectSuccessAction() },
                failCallback = { _, _ ->
                    CoroutineScope(io).launch {
                        observableReconnectFailAction()
                    }
                },
                serverDisconnectCallback = { _, _ -> observableDisconnectAction() },
                clientDisconnectCallback = { _, _ -> observableDisconnectAction() }
            )
        }
        sendSimpleMessageToHomeFragment(OBSERVABLE_RECONNECTING)
        state = OBSERVABLE_STATE_RELOADING
    }

    private suspend fun observableReconnectFailAction() {
        sendSimpleMessageToHomeFragment(OBSERVABLE_RECONNECT_FAIL)
        delay(RECONNECT_INTERVAL)
        if (!isReconnectCanceled)
            reconnectObservableToServer()
    }

    private fun observableReconnectSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVABLE_RECONNECT_SUCCESS)
        loginObservable()
    }

    private fun logOutObservable() {
        CoroutineScope(io).launch {
            transferTrackDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.LogOutObservable,
                    username = username,
                    targets = observableTargets.toTypedArray(),
                    data = null
                ),
                onSendMessageFail = { _, _ -> observableSendMessageFailAction(WebSocketDataType.LogOutObservable) }
            )
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    private fun observableConnectSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVABLE_CONNECT_SUCCESS)
        loginObservable()
    }

    private fun loginObservable() {
        CoroutineScope(io).launch {
            transferTrackDataUseCase.sendData(
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
        state = STATE_DISABLE
    }

    private fun observableDisconnectAction() {
        isReconnectCanceled = false
        sendSimpleMessageToHomeFragment(DISCONNECT)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.reconnecting_msg),
                false
            )
        )
        isObservableLogIn = false
        state = STATE_DISABLE
        reconnectObservableToServer()
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
                state = STATE_DISABLE
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

            WebSocketDataType.Data -> {

            }

            else -> Unit
        }
    }

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

    private fun observableSendDenyData(target: String) {
        CoroutineScope(io).launch {
            sendSimpleMessageToHomeFragment(OBSERVABLE_SENDING_PERMISSION_RESPONSE)
            transferTrackDataUseCase.sendData(
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

    private fun observableSendGrantData(target: String) {
        CoroutineScope(io).launch {
            sendSimpleMessageToHomeFragment(OBSERVABLE_SENDING_PERMISSION_RESPONSE)
            transferTrackDataUseCase.sendData(
                data = WebsocketDataModel(
                    type = WebSocketDataType.Grant,
                    username = username,
                    targets = arrayOf(target),
                    data = requestPermissionData?.data
                ),
                onSendMessageFail = { _, _ ->
                    observableSendMessageFailAction(WebSocketDataType.Grant)
                }
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun observableSendLocationData(data: String) {
        var interval = 0
        val permissionsList = data.split(",")
        for (permission in permissionsList) {
            if (permission.contains(username))
                interval = permission.replace(username, "").toInt()
        }
        val intervalMillis = interval * 1000L

        fusedLocationClient.requestLocationUpdates(
            getLocationRequest(intervalMillis),
            locationCallback,
            Looper.getMainLooper()
        )

        state = OBSERVABLE_STATE_SENDING_DATA
        sendSimpleMessageToHomeFragment(OBSERVABLE_ADDED_NEW_OBSERVER, observableTargets)
    }

    private fun getLocationCallback(): LocationCallback {
        val locationCallback = object : LocationCallback() {
            @RequiresApi(S)
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    val dataModel = DataModel(
                        targetUsername = username,
                        latitude = latitude,
                        longitude = longitude
                    )

                    val locationData = jsonConverter.convertObjectToJsonString(dataModel)

                    CoroutineScope(io).launch {
                        if (observableTargets.size > 0) {
                            transferTrackDataUseCase.sendData(
                                data = WebsocketDataModel(
                                    type = WebSocketDataType.Data,
                                    username = username,
                                    targets = observableTargets.toTypedArray(),
                                    data = locationData
                                ),
                                onSendMessageFail = { _, _ ->
                                    observableSendMessageFailAction(WebSocketDataType.Data)
                                }
                            )
                        }
                    }
                }
            }
        }

        return locationCallback
    }

    private fun getLocationRequest(intervalMillis: Long): LocationRequest {
        return LocationRequest
            .Builder(LocationRequest.PRIORITY_HIGH_ACCURACY, intervalMillis)
            .build()
    }

    private fun observableCancelReconnect() {
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
        state = STATE_DISABLE
        observableTargets.clear()
        isReconnectCanceled = true
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // Observer functions ----------------------------------------------------------------------------

    private fun logOutObserver() {
        val targetsUserName =
            observerTargets.map { targetModel -> targetModel.username }.toTypedArray()
        CoroutineScope(io).launch {
            transferTrackDataUseCase.sendData(
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

    private fun connectObserverToServer() {
        sendSimpleMessageToHomeFragment(OBSERVER_CONNECTING)
        CoroutineScope(io).launch {
            trackConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { observerConnectSuccessAction() },
                failCallback = { _, _ -> observerConnectFailAction() },
                serverDisconnectCallback = { _, _ -> observerDisconnectAction() },
                clientDisconnectCallback = { _, _ -> observerDisconnectAction() }
            )
        }
        state = OBSERVER_STATE_LOADING
    }

    private fun reconnectObserverToServer() {
        CoroutineScope(io).launch {
            trackConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { observerReconnectSuccessAction() },
                failCallback = { _, _ ->
                    CoroutineScope(io).launch {
                        observerReconnectFailAction()
                    }
                },
                serverDisconnectCallback = { _, _ -> observerDisconnectAction() },
                clientDisconnectCallback = { _, _ -> observerDisconnectAction() }
            )
        }
        sendSimpleMessageToHomeFragment(OBSERVER_RECONNECTING)
        state = OBSERVER_STATE_RELOADING
    }

    private suspend fun observerReconnectFailAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_RECONNECT_FAIL)
        delay(RECONNECT_INTERVAL)
        if (!isReconnectCanceled)
            reconnectObserverToServer()
    }

    private fun observerReconnectSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_RECONNECT_SUCCESS)
        loginObserver()
    }

    private fun observerConnectSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_CONNECT_SUCCESS)
        loginObserver()
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
        state = STATE_DISABLE
        observerTargets.clear()
    }

    private fun loginObserver() {
        CoroutineScope(io).launch {
            transferTrackDataUseCase.sendData(
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

    private fun observerLoginSuccessAction() {
        sendSimpleMessageToHomeFragment(OBSERVER_LOGIN_SUCCESS)
        isObserverLogIn = true
        observerSendRequestData(observerTargets.toTypedArray())
    }

    private fun observerSendRequestData(targets: Array<TargetModel>) {
        val targetsUserName =
            targets.map { targetModel -> targetModel.username }.toTypedArray()
        val data = StringBuffer("")
        for ((index, target) in targets.withIndex()) {
            data.append(target.username + target.permissions.coordinate)
            if (index != targets.size - 1)
                data.append(",")
        }

        CoroutineScope(io).launch {
            sendSimpleMessageToHomeFragment(OBSERVER_SENDING_REQUEST_DATA)
            transferTrackDataUseCase.sendData(
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

    private fun observerDisconnectAction() {
        isReconnectCanceled = false
        sendSimpleMessageToHomeFragment(DISCONNECT)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                getString(R.string.reconnecting_msg),
                false
            )
        )
        isObserverLogIn = false
        isObserverTrackStarted = false
        state = STATE_DISABLE
        reconnectObserverToServer()
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
        state = STATE_DISABLE
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
        state = STATE_DISABLE
        observerTargets.clear()
    }

    private fun cancelObservation(msg: String) {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createNotification(
                getString(R.string.app_name),
                msg,
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        isObserverLogIn = false
        isObserverTrackStarted = false
        state = STATE_DISABLE
        observerTargets.clear()
    }

    private fun observerCancelReconnect() {
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
        state = STATE_DISABLE
        observerTargets.clear()
        isReconnectCanceled = true
    }


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
                    cancelObservation(getString(R.string.cancel_observe_msg))
                }

                OBSERVABLE_GET_PERMISSION_DATA -> {
                    sendSimpleMessageToHomeFragment(
                        OBSERVABLE_RECEIVE_REQUEST_PERMISSION,
                        requestPermissionData
                    )
                }

                OBSERVER_REQUEST_LAST_RECEIVED_DATA -> {
                    if (lastReceivedData != null)
                        sendSimpleMessageToHomeFragment(
                            OBSERVER_LAST_RECEIVED_DATA,
                            lastReceivedData
                        )
                }

                OBSERVABLE_REQUEST_TARGETS -> {
                    if (observableTargets.size > 0)
                        sendSimpleMessageToHomeFragment(
                            OBSERVABLE_ADDED_NEW_OBSERVER,
                            observableTargets
                        )
                }

                CANCEL_RECONNECT_OBSERVABLE -> {
                    observableCancelReconnect()
                }

                CANCEL_RECONNECT_OBSERVER -> {
                    observerCancelReconnect()
                }

                else -> super.handleMessage(msg)
            }
        }
    }
}