package ir.srp.rasad.presentation.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.location.LocationManager
import android.os.Build
import android.os.Build.VERSION_CODES.S
import android.os.Bundle
import android.os.Handler
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
import ir.srp.rasad.core.Constants.LOCATION_STATE
import ir.srp.rasad.core.Constants.OBSERVER_LAST_RECEIVED_DATA
import ir.srp.rasad.core.Constants.MESSENGER_TRANSFORMATION
import ir.srp.rasad.core.Constants.NOTIFICATION_CHANNEL_ID
import ir.srp.rasad.core.Constants.NOTIFICATION_ID
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
import ir.srp.rasad.core.Constants.OBSERVABLE_SEND_DATA_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_SEND_DATA_SUCCESS
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
import ir.srp.rasad.data.receivers.LocationStateReceiver
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

@Suppress(
    "DEPRECATION",
    "KDocUnresolvedReference"
)
@SuppressLint(
    "HandlerLeak",
    "StringFormatMatches",
    "RemoteViewLayout",
    "MissingPermission"
)
@RequiresApi(S)
@AndroidEntryPoint
class MainService : Service() {

    /**
     * @param notificationManager
     * @param notificationChannel
     * @param serviceMessenger A messenger for the MainService to send message to the component. it is created by the same component and sent to the MainService when the component is bond to the MainService.
     * @param homeMessenger A messenger for the component to send message to the MainService. it is created by the MainService and sent to the component when the component is bond to the MainService.
     * @param io
     * @param trackConnectionUseCase Used to establish a web socket connection with the server.
     * @param transferTrackDataUseCase Used to transfer data on web socket connection.
     * @param userInfoUseCase Used to load user data (username - userToken - userId).
     * @param jsonConverter
     * @param locationCallback
     * @param fusedLocationClient
     * @param username
     * @param userToken
     * @param userId
     * @param requestPermissionData Save the requested permission data from the observer to use in another place.
     * @param lastReceivedData Save the last received data and show when user open the app activity
     * @param appState Specifies the current state of the observable or observer.
     * @param isServiceStarted Specifies the service is started or stopped.
     * @param isObservableLogIn Specifies whether the observable is logged in to the server or logged out from the server.
     * @param isObserverLogIn Specifies whether the observer is logged in to the server or logged out from the server.
     * @param isObserverTrackingStarted Specifies whether the observer is tacking anyone or not.
     * @param observableTargets The set of the observable's targets.
     * @param observerTargets The set of the observer's targets.
     * @param isReconnectCanceled Specifies whether the reconnection attempt was canceled or not.
     */

    //Common params
    @Inject
    lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private var serviceMessenger: Messenger? = null
    private lateinit var homeMessenger: Messenger
    private lateinit var locationStateReceiver: LocationStateReceiver
    private val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)

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
    private val locationCallback = getLocationCallback()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var username: String
    private lateinit var userToken: String
    private lateinit var userId: String
    private var requestPermissionData: WebsocketDataModel? = null
    private var lastReceivedData: DataModel? = null

    //State params
    private var appState = STATE_DISABLE
    private var isServiceStarted = false
    private var isObservableLogIn = false
    private var isObserverLogIn = false
    private var isObserverTrackingStarted = false
    private val observableTargets = HashSet<String>()
    private val observerTargets = HashSet<TargetModel>()
    private var isReconnectCanceled = false


    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        CoroutineScope(io).launch {
            val userData = userInfoUseCase.loadUserAccountInfo().data
            userData?.let { userModel ->
                username = userModel.username.toString()
                userToken = userModel.token.toString()
                userId = userModel.id.toString()
            }
        }
        homeMessenger = Messenger(HomeMessengerHandler())
        locationStateReceiver = LocationStateReceiver { locationStateChangeAction(it) }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        registerReceiver(locationStateReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val bundle = intent?.getBundleExtra(SERVICE_BUNDLE_KEY)
        val action = intent?.action
        bundle?.let { handleStartServiceWithParam(it) }
        action?.let { handleStartServiceWithAction(action) }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        initReceivedTextDataActions()
        return homeMessenger.binder
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(locationStateReceiver)
    }


    // Common functions ----------------------------------------------------------------------------


    private fun locationStateChangeAction(isLocationEnable: Boolean) {
        sendMessageToHome(LOCATION_STATE, isLocationEnable)
        if (isLocationEnable) {
            when (appState) {
                OBSERVABLE_STATE_PERMISSION_REQUEST -> {
                    requestPermissionData?.let { observableReceiveRequestPermission(it) }
                }

                else -> {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        createSimpleNotification(
                            getString(R.string.app_name),
                            getString(R.string.observable_login_success_msg),
                            true
                        )
                    )
                }
            }
        } else {
            if (appState != STATE_DISABLE)
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createSimpleNotification(
                        getString(R.string.app_name),
                        getString(R.string.location_off_msg),
                        true
                    )
                )
        }
    }

    private fun createNotificationChannel() {
        notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(notificationChannel)
    }

    private fun handleStartServiceWithParam(bundle: Bundle) {
        when (bundle.getString(SERVICE_TYPE_KEY)) {

            START_SERVICE_OBSERVABLE -> {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    createSimpleNotification(
                        getString(R.string.app_name),
                        getString(R.string.connecting_msg),
                        true
                    ),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
                isServiceStarted = true
                observableConnectToServer()
            }

            STOP_SERVICE_OBSERVABLE -> {
                observableLogout()
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
                    createSimpleNotification(
                        getString(R.string.app_name),
                        getString(R.string.connecting_msg),
                        true
                    ),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )

                isServiceStarted = true
                observerConnectToServer()
            }

            STOP_SERVICE_OBSERVER -> {
                observerLogout()
            }
        }
    }

    private fun handleStartServiceWithAction(action: String) {
        when (action) {

            DENY_PERMISSION_ACTION -> {
                requestPermissionData?.username?.let { observableSendDenyData(it) }
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createSimpleNotification(
                        getString(R.string.app_name),
                        getString(R.string.observable_login_success_msg),
                        true
                    )
                )
                appState = OBSERVABLE_STATE_READY
            }

            GRANT_PERMISSION_ACTION -> {
                requestPermissionData?.username?.let { observableSendGrantData(it) }
                notificationManager.notify(
                    NOTIFICATION_ID,
                    createSimpleNotification(
                        getString(R.string.app_name),
                        getString(R.string.observable_login_success_msg),
                        true
                    )
                )
                appState = OBSERVABLE_STATE_READY
            }
        }
    }

    private fun initReceivedTextDataActions() {
        CoroutineScope(io).launch {
            transferTrackDataUseCase.receiveData(
                onReceiveTextMessage = { text -> onReceiveTextMessageData(text) },
                onReceiveBinaryMessage = null
            )
        }
    }

    private fun onReceiveTextMessageData(textData: String) {
        val websocketDataModel =
            jsonConverter.convertJsonStringToObject(
                textData,
                WebsocketDataModel::class.java
            ) as WebsocketDataModel

        when (websocketDataModel.type) {

            WebSocketDataType.Confirmation -> {

                when (websocketDataModel.data) {

                    WebSocketDataType.LogInObservable.name -> {
                        observableLoginSuccessAction()
                    }

                    WebSocketDataType.LogOutObservable.name -> {
                        observableLogoutSuccessAction()
                    }

                    WebSocketDataType.LogInObserver.name -> {
                        observerLoginSuccessAction()
                    }

                    WebSocketDataType.LogOutObserver.name -> {
                        observerLogoutSuccessAction()
                    }

                    WebSocketDataType.RequestData.name -> {
                        observerRequestDataSuccessAction()
                    }

                    WebSocketDataType.Deny.name -> {
                        observableDenySuccessAction()
                    }

                    WebSocketDataType.Grant.name -> {
                        observableGrantSuccessAction()
                    }

                    WebSocketDataType.Data.name -> {
                        observableSendDataSuccessAction(textData)
                    }
                }
            }

            WebSocketDataType.RequestPermission -> {
                if (websocketDataModel.data != null) {
                    sendMessageToHome(OBSERVABLE_RECEIVE_REQUEST_PERMISSION, websocketDataModel)
                    observableReceiveRequestPermission(websocketDataModel)
                }

                requestPermissionData = websocketDataModel
                appState = OBSERVABLE_STATE_PERMISSION_REQUEST
            }

            WebSocketDataType.RequestData -> {
                observableTargets.add(websocketDataModel.username)
                websocketDataModel.data?.let {
                    observableSendData(it)
                }
            }

            WebSocketDataType.Failed -> {
                val errorData = websocketDataModel.data?.let {
                    jsonConverter.convertJsonStringToObject(
                        it,
                        ErrorDataModel::class.java
                    ) as ErrorDataModel
                }
                sendMessageToHome(OBSERVER_FAILURE, errorData)
                cancelObservation("${errorData?.code}: ${errorData?.reason}")
            }

            WebSocketDataType.LogOutObservable -> {
                for (target in observerTargets)
                    if (target.username == websocketDataModel.username) {
                        observerTargets.remove(target)
                        break
                    }
                if (observerTargets.size <= 0) {
                    sendMessageToHome(OBSERVER_DISCONNECT_ALL_TARGETS)
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        createSimpleNotification(
                            getString(R.string.app_name),
                            getString(R.string.observer_disconnect_all_targets_msg),
                            false
                        )
                    )
                    disconnectServer()
                    isServiceStarted = false
                    isObserverLogIn = false
                    appState = STATE_DISABLE
                }
            }

            WebSocketDataType.LogOutObserver -> {
                observableTargets.remove(websocketDataModel.username)
                if (observableTargets.size <= 0)
                    sendMessageToHome(OBSERVABLE_DISCONNECT_ALL_TARGETS)

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }

            WebSocketDataType.Grant -> {
                observerReceiveGrantAction()
            }

            WebSocketDataType.Deny -> {
                observerReceiveDenyAction()
            }

            WebSocketDataType.Data -> {
                observerReceiveDataAction(websocketDataModel)
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

    private fun sendMessageToHome(msg: Int, obj: Any? = null) {
        val message = Message.obtain(null, msg, obj)
        serviceMessenger?.send(message)
    }

    private fun createSimpleNotification(
        title: String,
        message: String,
        onGoing: Boolean,
    ): Notification {
        return NotificationCompat
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setOngoing(onGoing)
            .setContentTitle(title)
            .setContentText(message)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }

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
            .Builder(this, NOTIFICATION_CHANNEL_ID)
            .setOngoing(onGoing)
            .setContentTitle(title)
            .setContentText(collapseMessage)
            .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
            .setCustomBigContentView(permissionNotificationView)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }


    // Observable functions ------------------------------------------------------------------------


    /**
     * Step 1 - Observable connect
     */
    private fun observableConnectToServer() {
        sendMessageToHome(OBSERVABLE_CONNECTING)
        CoroutineScope(io).launch {
            trackConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { observableConnectSuccessAction() },
                failCallback = { _, _ -> observableConnectFailAction() },
                serverDisconnectCallback = { _, _ -> observableDisconnectAction() },
                clientDisconnectCallback = { _, _ -> observableDisconnectAction() }
            )
        }
        appState = OBSERVABLE_STATE_LOADING
    }

    /**
     * Step 2 - Observable Login
     */
    private fun observableLogin() {
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

    /**
     * Step 3 - Observable receives the permission request
     */
    private fun observableReceiveRequestPermission(data: WebsocketDataModel) {
        var interval = 0
        val permissionsList = data.data?.split(",")
        for (permission in permissionsList!!) {
            if (permission.contains(username))
                interval = permission.replace(username, "").toInt()
        }

        val msg = if (interval == 0)
            getString(R.string.permission_extend_notification_msg1, data.username)
        else
            getString(R.string.permission_extend_notification_msg2, data.username, interval)

        val permissionNotification = createPermissionNotification(
            getString(R.string.app_name),
            getString(R.string.permission_collapse_notification_msg, data.username),
            msg,
            true
        )

        notificationManager.notify(
            NOTIFICATION_ID,
            permissionNotification
        )
    }

    /**
     * Step 4 - Observable denies the permission
     */
    private fun observableSendDenyData(target: String) {
        CoroutineScope(io).launch {
            sendMessageToHome(OBSERVABLE_SENDING_PERMISSION_RESPONSE)
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

    /**
     * Step 4 - Observable grants the permission
     */
    private fun observableSendGrantData(target: String) {
        CoroutineScope(io).launch {
            sendMessageToHome(OBSERVABLE_SENDING_PERMISSION_RESPONSE)
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

    /**
     * Step 5 - Observable sends the data
     */
    private fun observableSendData(data: String) {
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

        appState = OBSERVABLE_STATE_SENDING_DATA
        sendMessageToHome(OBSERVABLE_ADDED_NEW_OBSERVER, observableTargets)
    }

    /**
     * Step 6 - Observable logout
     */
    private fun observableLogout() {
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
        sendMessageToHome(OBSERVABLE_CONNECT_SUCCESS)
        observableLogin()
    }

    private fun observableConnectFailAction() {
        sendMessageToHome(OBSERVABLE_CONNECT_FAIL)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observable_connect_fail_msg),
                false
            )
        )
        isServiceStarted = false
        appState = STATE_DISABLE
    }

    private fun observableDisconnectAction() {
        isReconnectCanceled = false
        sendMessageToHome(DISCONNECT)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.reconnecting_msg),
                false
            )
        )
        isObservableLogIn = false
        appState = STATE_DISABLE
        reconnectObservableToServer()
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
        sendMessageToHome(OBSERVABLE_RECONNECTING)
        appState = OBSERVABLE_STATE_RELOADING
    }

    private fun observableReconnectSuccessAction() {
        appState = OBSERVABLE_STATE_SENDING_DATA
        sendMessageToHome(OBSERVABLE_RECONNECT_SUCCESS)
        if (observableTargets.size > 0)
            sendMessageToHome(OBSERVABLE_ADDED_NEW_OBSERVER, observableTargets)
        observableLogin()
    }

    private suspend fun observableReconnectFailAction() {
        sendMessageToHome(OBSERVABLE_RECONNECT_FAIL)
        delay(RECONNECT_INTERVAL)
        if (!isReconnectCanceled)
            reconnectObservableToServer()
    }

    private fun observableLoginSuccessAction() {
        sendMessageToHome(OBSERVABLE_LOGIN_SUCCESS)
        isObservableLogIn = true
        if (appState != OBSERVABLE_STATE_SENDING_DATA)
            appState = OBSERVABLE_STATE_READY
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observable_login_success_msg),
                true
            )
        )
    }

    private fun observableLoginFailAction() {
        sendMessageToHome(OBSERVABLE_LOGIN_FAIL)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observable_login_fail_msg),
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        appState = STATE_DISABLE
    }

    private fun observableGrantSuccessAction() {
        sendMessageToHome(
            OBSERVABLE_GRANT_PERMISSION_SUCCESS,
            requestPermissionData?.username
        )
        requestPermissionData = null
    }

    private fun observableGrantFailAction() {
        sendMessageToHome(
            OBSERVABLE_GRANT_PERMISSION_FAIL,
            requestPermissionData
        )
        requestPermissionData?.let { observableReceiveRequestPermission(it) }
    }

    private fun observableDenySuccessAction() {
        sendMessageToHome(OBSERVABLE_DENY_PERMISSION_SUCCESS)
    }

    private fun observableDenyFailAction() {
        sendMessageToHome(OBSERVABLE_DENY_PERMISSION_FAIL)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observable_login_success_msg),
                true
            )
        )
    }

    private fun observableSendDataSuccessAction(textData: String) {
        sendMessageToHome(OBSERVABLE_SEND_DATA_SUCCESS, textData)
    }

    private fun observableSendDataFailAction() {
        sendMessageToHome(OBSERVABLE_SEND_DATA_FAIL)
    }

    private fun observableLogoutSuccessAction() {
        sendMessageToHome(OBSERVABLE_LOGOUT_SUCCESS)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observable_logout_success_msg),
                false
            )
        )
        disconnectServer()
        appState = STATE_DISABLE
        isServiceStarted = false
        isObservableLogIn = false
        observableTargets.clear()
    }

    private fun observableLogoutFailAction() {
        sendMessageToHome(OBSERVABLE_LOGOUT_FAIL)
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
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observable_logout_success_msg),
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        isObservableLogIn = false
        appState = STATE_DISABLE
        observableTargets.clear()
        isReconnectCanceled = true
        fusedLocationClient.removeLocationUpdates(locationCallback)
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

    private fun observableSendMessageFailAction(type: WebSocketDataType) {

        when (type) {
            WebSocketDataType.LogInObservable -> {
                observableLoginFailAction()
            }

            WebSocketDataType.LogOutObservable -> {
                observableLogoutFailAction()
            }

            WebSocketDataType.Deny -> {
                observableDenyFailAction()
            }

            WebSocketDataType.Grant -> {
                observableGrantFailAction()
            }

            WebSocketDataType.Data -> {
                observableSendDataFailAction()
            }

            else -> Unit
        }
    }


    // Observer functions --------------------------------------------------------------------------


    /**
     * Step 1 -  Observer connect
     */
    private fun observerConnectToServer() {
        sendMessageToHome(OBSERVER_CONNECTING)
        CoroutineScope(io).launch {
            trackConnectionUseCase.createChannel(
                url = WEBSOCKET_URL,
                successCallback = { observerConnectSuccessAction() },
                failCallback = { _, _ -> observerConnectFailAction() },
                serverDisconnectCallback = { _, _ -> observerDisconnectAction() },
                clientDisconnectCallback = { _, _ -> observerDisconnectAction() }
            )
        }
        appState = OBSERVER_STATE_LOADING
    }

    /**
     * Step 2 -  Observer login
     */
    private fun observerLogin() {
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

    /**
     * Step 3 - Observer sends request
     */
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
            sendMessageToHome(OBSERVER_SENDING_REQUEST_DATA)
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

    /**
     * Step 4 - Observer receive grant response
     */
    private fun observerReceiveGrantAction() {

    }

    /**
     * Step 4 - Observer receive deny response
     */
    private fun observerReceiveDenyAction() {

    }

    /**
     * Step 5 - Observer receives data
     */
    private fun observerReceiveDataAction(websocketDataModel: WebsocketDataModel) {
        val dataModel = websocketDataModel.data?.let {
            jsonConverter.convertJsonStringToObject(
                it,
                DataModel::class.java
            )
        } as DataModel
        lastReceivedData = dataModel
        sendMessageToHome(OBSERVER_RECEIVE_DATA, dataModel)
        appState = OBSERVER_STATE_RECEIVING_DATA

        if (!isObserverTrackingStarted) {
            notificationManager.notify(
                NOTIFICATION_ID,
                createSimpleNotification(
                    getString(R.string.app_name),
                    getString(R.string.observer_receive_data_msg, dataModel.targetUsername),
                    true
                )
            )
            isObserverTrackingStarted = true
        }
    }

    /**
     * Step 6 - Observer logout
     */
    private fun observerLogout() {
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

    private fun observerConnectSuccessAction() {
        sendMessageToHome(OBSERVER_CONNECT_SUCCESS)
        observerLogin()
    }

    private fun observerConnectFailAction() {
        sendMessageToHome(OBSERVER_CONNECT_FAIL)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observer_connect_fail_msg),
                false
            )
        )
        isServiceStarted = false
        appState = STATE_DISABLE
        observerTargets.clear()
    }

    private fun observerDisconnectAction() {
        isReconnectCanceled = false
        sendMessageToHome(DISCONNECT)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.reconnecting_msg),
                false
            )
        )
        isObserverLogIn = false
        isObserverTrackingStarted = false
        appState = STATE_DISABLE
        reconnectObserverToServer()
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
        sendMessageToHome(OBSERVER_RECONNECTING)
        appState = OBSERVER_STATE_RELOADING
    }

    private fun observerReconnectSuccessAction() {
        sendMessageToHome(OBSERVER_RECONNECT_SUCCESS)
        observerLogin()
    }

    private suspend fun observerReconnectFailAction() {
        sendMessageToHome(OBSERVER_RECONNECT_FAIL)
        delay(RECONNECT_INTERVAL)
        if (!isReconnectCanceled)
            reconnectObserverToServer()
    }

    private fun observerLoginSuccessAction() {
        sendMessageToHome(OBSERVER_LOGIN_SUCCESS)
        isObserverLogIn = true
        observerSendRequestData(observerTargets.toTypedArray())
    }

    private fun observerLoginFailAction() {
        sendMessageToHome(OBSERVER_LOGIN_FAIL)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observer_login_fail_msg),
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        appState = STATE_DISABLE
        observerTargets.clear()
    }

    private fun observerRequestDataSuccessAction() {
        sendMessageToHome(OBSERVER_SEND_REQUEST_DATA_SUCCESS)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observer_login_success_msg),
                true
            )
        )
        appState = OBSERVER_STATE_WAITING_RESPONSE
    }

    private fun observerRequestDataFailAction() {
        sendMessageToHome(OBSERVER_SEND_REQUEST_DATA_FAIL)
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.observer_send_request_data_fail_msg),
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        isObserverLogIn = false
        appState = STATE_DISABLE
        observerTargets.clear()
    }

    private fun observerLogoutSuccessAction() {
        isObserverTrackingStarted = false
    }

    private fun observerLogoutFAilAction() {

    }

    private fun cancelObservation(msg: String) {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                msg,
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        isObserverLogIn = false
        isObserverTrackingStarted = false
        appState = STATE_DISABLE
        observerTargets.clear()
    }

    private fun observerCancelReconnect() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        notificationManager.notify(
            NOTIFICATION_ID,
            createSimpleNotification(
                getString(R.string.app_name),
                getString(R.string.cancel_observe_msg),
                false
            )
        )
        disconnectServer()
        isServiceStarted = false
        isObserverLogIn = false
        appState = STATE_DISABLE
        observerTargets.clear()
        isReconnectCanceled = true
    }

    private fun observerSendMessageFailAction(type: WebSocketDataType) {
        when (type) {

            WebSocketDataType.LogInObserver -> {
                observerLoginFailAction()
            }

            WebSocketDataType.LogOutObserver -> {
                observerLogoutFAilAction()
            }

            WebSocketDataType.RequestData -> {
                observerRequestDataFailAction()
            }

            else -> Unit
        }
    }


    private inner class HomeMessengerHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSENGER_TRANSFORMATION -> {
                    serviceMessenger = msg.replyTo
                    val appStateMsg =
                        Message.obtain(null, APP_STATE, appState)
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
                    sendMessageToHome(
                        OBSERVABLE_RECEIVE_REQUEST_PERMISSION,
                        requestPermissionData
                    )
                }

                OBSERVER_REQUEST_LAST_RECEIVED_DATA -> {
                    if (lastReceivedData != null)
                        sendMessageToHome(
                            OBSERVER_LAST_RECEIVED_DATA,
                            lastReceivedData
                        )
                }

                OBSERVABLE_REQUEST_TARGETS -> {
                    if (observableTargets.size > 0)
                        sendMessageToHome(
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