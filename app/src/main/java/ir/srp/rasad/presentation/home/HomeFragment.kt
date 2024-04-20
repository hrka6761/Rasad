package ir.srp.rasad.presentation.home

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.content.ComponentName
import android.content.Context
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.LocationManager
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.carto.styles.AnimationStyleBuilder
import com.carto.styles.AnimationType
import com.carto.styles.MarkerStyleBuilder
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.BaseFragment
import ir.srp.rasad.core.Constants.APP_STATE
import ir.srp.rasad.core.Constants.CANCEL_OBSERVE
import ir.srp.rasad.core.Constants.CANCEL_RECONNECT_OBSERVABLE
import ir.srp.rasad.core.Constants.CANCEL_RECONNECT_OBSERVER
import ir.srp.rasad.core.Constants.COARSE_RESULT_KEY
import ir.srp.rasad.core.Constants.DENY_PERMISSION_ACTION
import ir.srp.rasad.core.Constants.DISCONNECT
import ir.srp.rasad.core.Constants.FINE_RESULT_KEY
import ir.srp.rasad.core.Constants.GRANT_PERMISSION_ACTION
import ir.srp.rasad.core.Constants.LOCATION_OFF_DIALOG_LABEL
import ir.srp.rasad.core.Constants.LOCATION_STATE
import ir.srp.rasad.core.Constants.MESSENGER_TRANSFORMATION
import ir.srp.rasad.core.Constants.OBSERVABLE_ADDED_NEW_OBSERVER
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECTING
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_DENY_PERMISSION_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_DENY_PERMISSION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_DISCONNECT_ALL_TARGETS
import ir.srp.rasad.core.Constants.OBSERVABLE_GET_PERMISSION_DATA
import ir.srp.rasad.core.Constants.OBSERVABLE_GRANT_PERMISSION_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_GRANT_PERMISSION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_STATE
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_RECEIVE_REQUEST_PERMISSION
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
import ir.srp.rasad.core.Constants.OBSERVER_LAST_RECEIVED_DATA
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_STATE
import ir.srp.rasad.core.Constants.OBSERVER_LOGIN_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_RECEIVE_DATA
import ir.srp.rasad.core.Constants.OBSERVER_RECONNECTING
import ir.srp.rasad.core.Constants.OBSERVER_RECONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_RECONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_REQUEST_LAST_RECEIVED_DATA
import ir.srp.rasad.core.Constants.OBSERVER_SENDING_REQUEST_DATA
import ir.srp.rasad.core.Constants.OBSERVER_SEND_REQUEST_DATA_FAIL
import ir.srp.rasad.core.Constants.OBSERVER_SEND_REQUEST_DATA_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVER_STATE_LOADING
import ir.srp.rasad.core.Constants.OBSERVER_STATE_RECEIVING_DATA
import ir.srp.rasad.core.Constants.OBSERVER_STATE_RELOADING
import ir.srp.rasad.core.Constants.OBSERVER_STATE_WAITING_RESPONSE
import ir.srp.rasad.core.Constants.SAVED_TARGETS_KEY
import ir.srp.rasad.core.Constants.SERVICE_BUNDLE_KEY
import ir.srp.rasad.core.Constants.SERVICE_DATA_KEY
import ir.srp.rasad.core.Constants.SERVICE_STATE
import ir.srp.rasad.core.Constants.SERVICE_TYPE_KEY
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVER
import ir.srp.rasad.core.Constants.STATE_DISABLE
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.utils.Dialog.dialogLabel
import ir.srp.rasad.core.utils.Dialog.hideSimpleDialog
import ir.srp.rasad.core.utils.Dialog.showSimpleDialog
import ir.srp.rasad.core.utils.JsonConverter
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.utils.MessageViewer.showMessage
import ir.srp.rasad.core.utils.MessageViewer.showWarning
import ir.srp.rasad.core.utils.PermissionManager
import ir.srp.rasad.databinding.FragmentHomeBinding
import ir.srp.rasad.domain.models.DataModel
import ir.srp.rasad.domain.models.ErrorDataModel
import ir.srp.rasad.domain.models.TargetModel
import ir.srp.rasad.domain.models.WebsocketDataModel
import ir.srp.rasad.presentation.services.MainService
import kotlinx.coroutines.launch
import org.neshan.common.model.LatLng
import org.neshan.mapsdk.internal.utils.BitmapUtils
import org.neshan.mapsdk.model.Marker
import javax.inject.Inject


@Suppress(
    "UNCHECKED_CAST",
    "HandlerLeak"
)
@RequiresApi(TIRAMISU)
@AndroidEntryPoint
class HomeFragment : BaseFragment(), RequestTargetListener {

    /**
     * @param jsonConverter
     * @param binding
     * @param viewModel
     * @param permissionManager
     * @param homeMessenger
     * @param serviceMessenger
     * @param serviceConnection
     * @param savedTargets
     * @param trackUserBottomSheet
     * @param marker
     * @param isServiceBound Specifies whether HomeFragment is bound to the MainService or not.
     * @param isServiceStarted Specifies whether the service is started or stopped.
     * @param isObservableLogIn Specifies whether the observable is logged in to the server or logged out from the server.
     * @param isObserverLogIn Specifies whether the observer is logged in to the server or logged out from the server.
     * @param isObserverTrackingStarted Specifies whether the observer is tacking anyone or not.
     */

    //Common params
    @Inject
    lateinit var jsonConverter: JsonConverter
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var permissionManager: PermissionManager
    private var homeMessenger: Messenger? = null
    private lateinit var serviceMessenger: Messenger
    private val serviceConnection = ServiceConnection()
    private lateinit var savedTargets: HashSet<TargetModel>
    val trackUserBottomSheet = TrackUserBottomSheet(this)
    private var marker: Marker? = null

    @Inject
    lateinit var locationManager: LocationManager

    //State params
    private var isServiceBound = false
    private var isServiceStarted = false
    private var isObservableLogIn = false
    private var isObserverLogIn = false
    private var isObserverTrackStarted = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this, PermissionsRequestCallback())
        serviceMessenger = Messenger(ServiceMessengerHandler())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        bindService()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    override fun onDestroy() {
        super.onDestroy()

        unBindService()
    }

    override fun onRequest(isNewTarget: Boolean, vararg targets: TargetModel) {
        if (isNewTarget)
            for (target in targets)
                saveNewTarget(target)

        startServiceWithParam(START_SERVICE_OBSERVER, targets)
        disableViews()
        trackUserBottomSheet.dismiss()
    }

    override fun onRemoveTarget(targetName: String) {
        deleteTarget(targetName)
    }


    private fun initialize() {
        initGetSavedTargetsResult()
        initSettingsButton()
        initTrackMeButton()
        initTrackOtherButton()
    }

    private fun checkLocationState() {
        if (!locationManager.isLocationEnabled && isObservableLogIn)
            activity?.let {
                showSimpleDialog(
                    activity = it,
                    msg = "Location is off\nYou have to turn on it.",
                    negativeButton = "",
                    positiveButton = "Turn on location",
                    negativeAction = {},
                    positiveAction = { openLocationSettings() }
                )
            }
    }

    private fun initGetSavedTargetsResult() {
        lifecycleScope.launch {
            viewModel.targets.collect { result ->
                when (result) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        savedTargets = HashSet()
                        savedTargets = result.data!!

                        val args = Bundle()
                        val targets = mutableListOf<TargetModel>()
                        for (target in savedTargets)
                            targets.add(target)

                        args.putParcelableArray(SAVED_TARGETS_KEY, targets.toTypedArray())
                        trackUserBottomSheet.arguments = args
                        trackUserBottomSheet.show(
                            requireActivity().supportFragmentManager,
                            trackUserBottomSheet.tag
                        )
                    }

                    is Resource.Error -> {
                        result.error(this@HomeFragment)
                    }
                }
            }
        }
    }

    private fun initSettingsButton() {
        binding.settingsImg.setOnClickListener { onClickSettings() }
    }

    private fun onClickSettings() {
        if (isObservableLogIn)
            showError(this, getString(R.string.observable_disable_setting_msg))
        else if (isObserverLogIn)
            showError(this, getString(R.string.observer_disable_setting_msg))
        else
            navController.navigate(R.id.settingsFragment)
    }

    private fun initTrackMeButton() {
        binding.onOffFab.setOnClickListener { onShortClickOnOff() }
        binding.onOffFab.setOnLongClickListener {
            onLongClickOnOff()
            true
        }
    }

    private fun onLongClickOnOff() {
        if (!permissionManager.hasPreciseLocationPermission() &&
            !permissionManager.hasApproximateLocationPermission()
        ) {
            activity?.let {
                showSimpleDialog(
                    activity = it,
                    msg = getString(R.string.dialog_basic_location_dialog_msg),
                    negativeAction = { dialog ->
                        showWarning(
                            this,
                            getString(R.string.snackbar_approximate_location_negative_msg)
                        )
                        dialog.dismiss()
                    },
                    positiveAction = { _ ->
                        permissionManager.getBasicLocationPermission()
                    }
                )
            }

            return
        }

        if (!permissionManager.hasBackgroundLocationPermission()) {
            activity?.let {
                showSimpleDialog(
                    activity = it,
                    msg = getString(R.string.dialog_background_location_dialog_msg),
                    negativeAction = { dialog ->
                        showWarning(
                            this,
                            getString(R.string.snackbar_background_location_negative_msg)
                        )
                        dialog.dismiss()
                    }, positiveAction = { _ ->
                        permissionManager.getBackgroundLocationPermission()
                    }
                )
            }

            return
        }

        if (!permissionManager.hasNotificationPermission()) {
            activity?.let {
                showSimpleDialog(
                    activity = it,
                    msg = getString(R.string.dialog_notification_dialog_msg),
                    negativeAction = { dialog ->
                        showWarning(this, getString(R.string.snackbar_notification_negative_msg))
                        dialog.dismiss()
                    },
                    positiveAction = { _ ->
                        permissionManager.getNotificationPermission()
                    }
                )
            }

            return
        }

        if (isObservableLogIn)
            startServiceWithParam(STOP_SERVICE_OBSERVABLE)
        else {
            if (!locationManager.isLocationEnabled) {
                activity?.let {
                    showSimpleDialog(
                        activity = it,
                        msg = "Location is off\nYou have to turn on it.",
                        positiveButton = "Turn on location",
                        negativeAction = {},
                        positiveAction = { openLocationSettings() }
                    )
                }

                return
            }

            startServiceWithParam(START_SERVICE_OBSERVABLE)
        }

        disableViews()
    }

    private fun onShortClickOnOff() {
        showWarning(this, getString(R.string.snackbar_short_click_msg))
    }

    private fun initTrackOtherButton() {
        binding.addMemberFab.setOnClickListener { showTrackUserSheet() }
    }

    private fun showTrackUserSheet() {
        if (!permissionManager.hasNotificationPermission()) {
            activity?.let {
                showSimpleDialog(
                    activity = it,
                    msg = getString(R.string.dialog_notification_dialog_msg),
                    negativeAction = { dialog ->
                        showWarning(this, getString(R.string.snackbar_notification_negative_msg))
                        dialog.dismiss()
                    },
                    positiveAction = { _ ->
                        permissionManager.getNotificationPermission()
                    }
                )
            }

            return
        }

        if (isObserverTrackStarted) {
            val msg = Message.obtain(null, CANCEL_OBSERVE)
            homeMessenger?.send(msg)
            enableObservable()
            binding.map.removeMarker(marker)
            binding.addMemberFab.text = getString(R.string.btn_txt_track_other)
            binding.addMemberFab.setIconResource(R.drawable.add)
            isObserverTrackStarted = false
            isObserverLogIn = false
            isServiceStarted = false
            marker = null

            return
        }

        if (this::savedTargets.isInitialized) {
            val args = Bundle()
            val targets = mutableListOf<TargetModel>()

            for (target in savedTargets)
                targets.add(target)

            args.putParcelableArray(SAVED_TARGETS_KEY, targets.toTypedArray())
            trackUserBottomSheet.arguments = args
            trackUserBottomSheet.show(
                requireActivity().supportFragmentManager,
                trackUserBottomSheet.tag
            )
        } else
            viewModel.loadTargets()
    }

    private fun bindService() {
        if (!isServiceBound) {
            val intent = Intent(requireContext(), MainService::class.java)
            requireContext().bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        }
    }

    private fun unBindService() {
        if (isServiceBound) {
            requireContext().unbindService(serviceConnection)
            isServiceBound = false
        }
    }

    private fun startServiceWithParam(type: String, data: Any? = null) {
        val intent = Intent(requireContext(), MainService::class.java)
        val bundle = Bundle()
        bundle.putString(SERVICE_TYPE_KEY, type)
        data?.let { bundle.putParcelableArray(SERVICE_DATA_KEY, data as Array<Parcelable>) }
        intent.putExtra(SERVICE_BUNDLE_KEY, bundle)
        requireContext().startForegroundService(intent)
    }

    private fun startServiceWithAction(action: String) {
        val intent = Intent(requireContext(), MainService::class.java)
        intent.action = action
        requireContext().startService(intent)
    }

    private fun saveNewTarget(target: TargetModel) {
        if (!this::savedTargets.isInitialized)
            savedTargets = HashSet()
        savedTargets.add(target)
        viewModel.saveTargets(savedTargets)
    }

    private fun deleteTarget(targetName: String) {
        for (target in savedTargets) {
            if (target.name == targetName) {
                savedTargets.remove(target)
                break
            }
        }
        viewModel.saveTargets(savedTargets)
    }

    private fun disableViews() {
        binding.progressBar.visibility = View.VISIBLE
        binding.onOffFab.isEnabled = false
        binding.addMemberFab.isEnabled = false
        binding.settingsImg.isEnabled = false
    }

    private fun enableViews() {
        binding.progressBar.visibility = View.GONE
        binding.onOffFab.isEnabled = true
        binding.addMemberFab.isEnabled = true
        binding.settingsImg.isEnabled = true
    }

    private fun enableObservable() {
        binding.onOffFab.isEnabled = true
    }

    private fun disableObservable() {
        binding.onOffFab.isEnabled = false
    }

    private fun enableObserver() {
        binding.addMemberFab.isEnabled = true
    }

    private fun disableObserver() {
        binding.addMemberFab.isEnabled = false
    }


    // Observable functions ------------------------------------------------------------------------


    /**
     * Connecting
     */
    private fun observableConnectingAction() {
        isServiceStarted = true
        binding.onOffFab.setImageResource(R.drawable.powering)
    }

    /**
     * Connect success
     */
    private fun observableConnectSuccessAction() {

    }

    /**
     * Connect fail
     */
    private fun observableConnectFailAction() {
        isServiceStarted = false
        enableViews()
        binding.onOffFab.setImageResource(R.drawable.power_off)
        showError(this, getString(R.string.observable_connect_fail_msg))
    }

    /**
     * Reconnecting
     */
    private fun observableReconnectingActions() {
        binding.onOffFab.setImageResource(R.drawable.powering)
        binding.waitingTxt.text = getString(R.string.reconnecting_msg)
        binding.cancelWaitingBtn.visibility = View.VISIBLE
        binding.cancelWaitingBtn.setOnClickListener { observableCancelReconnect() }
        disableViews()
    }

    /**
     * Reconnect success
     */
    private fun observableReconnectSuccessActions() {

    }

    /**
     * Reconnect fail
     */
    private fun observableReconnectFailActions() {

    }

    /**
     * Login success
     */
    private fun observableLogInSuccessAction() {
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        isObservableLogIn = true
        enableViews()
        disableObserver()
        binding.onOffFab.setImageResource(R.drawable.power_on)
    }

    /**
     * Login fail
     */
    private fun observableLogInFailAction() {
        isServiceStarted = false
        enableViews()
        binding.onOffFab.setImageResource(R.drawable.power_off)
        showError(this, getString(R.string.observable_login_fail_msg))
    }

    /**
     * Receive Request
     */
    private fun observableReceiveRequestPermissionAction(data: WebsocketDataModel) {
        var interval = 0
        val permissionsList = data.data?.split(",")
        for (permission in permissionsList!!) {
            if (permission.contains(data.username))
                interval = permission.replace(data.username, "").toInt()
        }

        val msg = if (interval == 0)
            getString(R.string.permission_extend_notification_msg1, data.username)
        else
            getString(
                R.string.permission_extend_notification_msg2,
                data.username,
                interval.toString()
            )

        activity?.let {
            showSimpleDialog(
                activity = it,
                msg = msg,
                negativeAction = { _ ->
                    startServiceWithAction(DENY_PERMISSION_ACTION)
                },
                positiveAction = { _ ->
                    startServiceWithAction(GRANT_PERMISSION_ACTION)
                }
            )
        }
    }

    /**
     * Sending response (Grant or Deny)
     */
    private fun observableSendingPermissionResponseAction() {
        activity?.let { hideSimpleDialog(it) }
    }

    /**
     * Grant success
     */
    private fun observableGrantPermissionSuccessAction(target: String) {
        showMessage(this, getString(R.string.snackbar_grant_success, target))
    }

    /**
     * Grant fail
     */
    private fun observableGrantPermissionFailAction(data: WebsocketDataModel) {
        observableReceiveRequestPermissionAction(data)
        showError(this, getString(R.string.snackbar_grant_failed))
    }

    /**
     * Deny success
     */
    private fun observableDenyPermissionSuccessAction() {

    }

    /**
     * Deny fail
     */
    private fun observableDenyPermissionFailAction() {
        showError(this, getString(R.string.snackbar_deny_fail))
    }

    /**
     * Send data success
     */
    private fun observableSendDataSuccessAction(data: String) {
        val dataModel = jsonConverter.convertJsonStringToObject(
            data,
            DataModel::class.java
        ) as DataModel
    }

    /**
     * Send data fail
     */
    private fun observableSendDataFailAction() {

    }

    /**
     * Add new observer
     */
    private fun newObserverAddedAction(observableTargets: HashSet<String>) {
        binding.trackersContainer.visibility = View.VISIBLE
        binding.trackersNumber.text = observableTargets.size.toString()
    }

    /**
     * Disconnect all observers
     */
    private fun disconnectAllObserverAction() {
        binding.trackersContainer.visibility = View.GONE
    }

    /**
     * Logout success
     */
    private fun observableLogOutSuccessAction() {
        isServiceStarted = false
        isObservableLogIn = false
        binding.onOffFab.setImageResource(R.drawable.power_off)
        binding.trackersContainer.visibility = View.GONE
        enableViews()
        enableObserver()
    }

    /**
     * Logout fail
     */
    private fun observableLogOutFailAction() {
        enableViews()
        showError(this, getString(R.string.observable_logout_fail_msg))
    }

    /**
     * Cancel reconnect
     */
    private fun observableCancelReconnect() {
        isServiceStarted = false
        isObservableLogIn = false
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        binding.onOffFab.setImageResource(R.drawable.power_off)
        enableViews()
        val msg = Message.obtain(null, CANCEL_RECONNECT_OBSERVABLE)
        homeMessenger?.send(msg)
    }


    // Observer functions --------------------------------------------------------------------------


    /**
     * Connecting
     */
    private fun observerConnectingAction() {
        isServiceStarted = true
    }

    /**
     * Connect success
     */
    private fun observerConnectSuccessAction() {

    }

    /**
     * Connect fail
     */
    private fun observerConnectFailAction() {
        enableViews()
        isServiceStarted = false
        showError(this, getString(R.string.observer_connect_fail_msg))
    }

    /**
     * Reconnecting
     */
    private fun observerReconnectingActions() {
        binding.waitingTxt.text = getString(R.string.reconnecting_msg)
        binding.cancelWaitingBtn.visibility = View.VISIBLE
        binding.cancelWaitingBtn.setOnClickListener { observerCancelReconnect() }
        disableViews()
    }

    private fun observerCancelReconnect() {
        isObserverTrackStarted = false
        isObserverLogIn = false
        isServiceStarted = false
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        enableViews()
        val msg = Message.obtain(null, CANCEL_RECONNECT_OBSERVER)
        homeMessenger?.send(msg)
    }

    /**
     * Reconnect success
     */
    private fun observerReconnectSuccessActions() {

    }

    /**
     * Reconnect fail
     */
    private fun observerReconnectFailActions() {

    }

    /**
     * Login Success
     */
    private fun observerLoginSuccessAction() {
        isObserverLogIn = true
    }

    /**
     * Login fail
     */
    private fun observerLoginFailAction() {
        enableViews()
        isServiceStarted = false
        showError(this, getString(R.string.observer_login_fail_msg))
    }

    /**
     * Sending request
     */
    private fun observerSendingRequestDataAction() {

    }

    /**
     * Send Request success
     */
    private fun observerSendRequestDataSuccessAction() {
        binding.waitingTxt.text = getString(R.string.txt_response_waiting)
        binding.cancelWaitingBtn.setOnClickListener { onClickCancelResponseWaiting() }
        binding.cancelWaitingBtn.visibility = View.VISIBLE
    }

    private fun onClickCancelResponseWaiting() {
        isServiceStarted = false
        isObserverLogIn = false
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        enableViews()
        val msg = Message.obtain(null, CANCEL_OBSERVE)
        homeMessenger?.send(msg)
    }

    /**
     * Send Request fail
     */
    private fun observerSendRequestDataFailAction() {
        isServiceStarted = false
        isObserverLogIn = false
        enableViews()
        showError(this, getString(R.string.observer_send_request_data_fail_msg))
    }

    /**
     * Receive fail
     */
    private fun failedObserveAction(errorData: ErrorDataModel) {
        isServiceStarted = false
        isObserverLogIn = false
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        enableViews()
        showError(this, "${errorData.code}: ${errorData.reason}")
    }

    /**
     * Receive data
     */
    private fun observerReceiveDataAction(data: DataModel) {
        if (marker == null) {
            val markerIcon = getTargetMarkerIcon(data.targetUsername)
            binding.map.moveCamera(LatLng(data.latitude, data.longitude), 2f)
            marker = createMarker(LatLng(data.latitude, data.longitude), markerIcon)
            binding.map.addMarker(marker)
            enableViews()
            disableObservable()
            binding.cancelWaitingBtn.visibility = View.GONE
            binding.waitingTxt.text = getString(R.string.txt_waiting)
            binding.addMemberFab.text = getString(R.string.btn_txt_stop_track)
            binding.addMemberFab.setIconResource(R.drawable.stop)
            isObserverTrackStarted = true
        } else {
            binding.map.moveCamera(LatLng(data.latitude, data.longitude), 2f)
            marker?.latLng = LatLng(data.latitude, data.longitude)
        }
    }

    /**
     * Receive last data
     */
    private fun lastReceivedDataAction(lastReceivedData: DataModel) {
        if (marker == null) {
            val markerIcon = getTargetMarkerIcon(lastReceivedData.targetUsername)
            binding.map.moveCamera(
                LatLng(
                    lastReceivedData.latitude,
                    lastReceivedData.longitude
                ), 2f
            )
            marker = createMarker(
                LatLng(
                    lastReceivedData.latitude,
                    lastReceivedData.longitude
                ), markerIcon
            )
            binding.map.addMarker(marker)
        }
    }

    private fun createMarker(loc: LatLng, markerIcon: Int): Marker {
        val animStBl = AnimationStyleBuilder()
        animStBl.fadeAnimationType = AnimationType.ANIMATION_TYPE_SMOOTHSTEP
        animStBl.sizeAnimationType = AnimationType.ANIMATION_TYPE_SPRING
        animStBl.phaseInDuration = 0.8f
        animStBl.phaseOutDuration = 0.8f
        val animSt = animStBl.buildStyle()

        val markStCr = MarkerStyleBuilder()
        markStCr.size = 30f
        markStCr.bitmap = BitmapUtils.createBitmapFromAndroidBitmap(
            BitmapFactory.decodeResource(
                resources, markerIcon
            )
        )

        markStCr.animationStyle = animSt
        val markSt = markStCr.buildStyle()

        return Marker(loc, markSt)
    }

    private fun getTargetMarkerIcon(username: String): Int {
        var icon: Int = R.drawable.marker10

        if (this::savedTargets.isInitialized) {
            for (target in savedTargets) {
                if (target.username == username) {
                    icon = target.markerIcon
                    break
                }
            }
        }

        return icon
    }

    /**
     * Disconnect all observables
     */
    private fun observerDisconnectAllTargetAction() {
        if (marker != null)
            binding.map.removeMarker(marker)
        isServiceStarted = false
        isObserverLogIn = false
        isObserverTrackStarted = false
        binding.addMemberFab.text = getString(R.string.btn_txt_track_other)
        binding.addMemberFab.setIconResource(R.drawable.add)
        marker = null
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        enableObservable()
        enableViews()
    }


    // Common functions ----------------------------------------------------------------------------


    private fun disconnectAction() {
        isServiceStarted = false
        isObservableLogIn = false
        isObserverLogIn = false
        isObserverTrackStarted = false
        if (dialogLabel != LOCATION_OFF_DIALOG_LABEL)
            activity?.let { hideSimpleDialog(it) }
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.addMemberFab.setIconResource(R.drawable.add)
        if (marker != null) {
            binding.map.removeMarker(marker)
            marker = null
        }
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.addMemberFab.text = getString(R.string.btn_txt_track_other)
        binding.onOffFab.setImageResource(R.drawable.power_off)
        binding.trackersContainer.visibility = View.GONE
        enableObservable()
        enableObserver()
        enableViews()
    }

    private fun locationChangeStateAction(isLocationEnable: Boolean) {
        if (isLocationEnable) {
            activity?.let { hideSimpleDialog(it) }
        } else {
            if (isObservableLogIn) {
                activity?.let {
                    showSimpleDialog(
                        label = LOCATION_OFF_DIALOG_LABEL,
                        activity = it,
                        msg = "Location is off\nYou have to turn on it.",
                        negativeButton = "",
                        positiveButton = "Turn on location",
                        negativeAction = { requireActivity().finish() },
                        positiveAction = { openLocationSettings() }
                    )
                }
            }
        }
    }

    private fun openLocationSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        requireContext().startActivity(intent)
    }

    private fun processAppState(state: Int) {
        when (state) {
            STATE_DISABLE -> {

            }

            OBSERVER_STATE_LOADING -> {
                disableViews()
            }

            OBSERVER_STATE_WAITING_RESPONSE -> {
                disableViews()
                observerSendRequestDataSuccessAction()
            }

            OBSERVER_STATE_RECEIVING_DATA -> {
                enableViews()
                disableObservable()
                binding.cancelWaitingBtn.visibility = View.GONE
                binding.waitingTxt.text = getString(R.string.txt_waiting)
                binding.addMemberFab.text = getString(R.string.btn_txt_stop_track)
                binding.addMemberFab.setIconResource(R.drawable.stop)
                isObserverTrackStarted = true

                val msg = Message.obtain(null, OBSERVER_REQUEST_LAST_RECEIVED_DATA)
                homeMessenger?.send(msg)
            }

            OBSERVABLE_STATE_LOADING -> {
                disableViews()
            }

            OBSERVABLE_STATE_READY -> {
                binding.onOffFab.setImageResource(R.drawable.power_on)
                disableObserver()
            }

            OBSERVABLE_STATE_PERMISSION_REQUEST -> {
                binding.onOffFab.setImageResource(R.drawable.power_on)
                val requestPermissionDataMsg =
                    Message.obtain(null, OBSERVABLE_GET_PERMISSION_DATA)
                homeMessenger?.send(requestPermissionDataMsg)
                binding.cancelWaitingBtn.visibility = View.GONE
                binding.waitingTxt.text = getString(R.string.txt_waiting)
                enableViews()
            }

            OBSERVABLE_STATE_SENDING_DATA -> {
                binding.onOffFab.setImageResource(R.drawable.power_on)
                val msg = Message.obtain(null, OBSERVABLE_REQUEST_TARGETS)
                homeMessenger?.send(msg)
            }

            OBSERVABLE_STATE_RELOADING -> {
                observableReconnectingActions()
            }

            OBSERVER_STATE_RELOADING -> {
                observerReconnectingActions()
            }
        }
    }


    private inner class PermissionsRequestCallback : ActivityResultCallback<Map<String, Boolean>> {
        override fun onActivityResult(result: Map<String, Boolean>) {
            for (item in result) {
                if (!item.value) {
                    val message = when (item.key) {
                        COARSE_RESULT_KEY -> getString(R.string.snackbar_approximate_location_negative_msg)
                        FINE_RESULT_KEY -> getString(R.string.snackbar_precise_location_negative_msg)
                        ACCESS_BACKGROUND_LOCATION -> getString(R.string.snackbar_background_location_negative_msg)
                        else -> getString(R.string.snackbar_notification_negative_msg)
                    }

                    showWarning(this@HomeFragment, message)

                    if (item.key != FINE_RESULT_KEY)
                        return
                }
            }

            if (permissionManager.hasBackgroundLocationPermission() && permissionManager.hasNotificationPermission())
                if (isServiceStarted)
                    startServiceWithParam(STOP_SERVICE_OBSERVABLE)
                else
                    startServiceWithParam(START_SERVICE_OBSERVABLE)
        }
    }

    private inner class ServiceConnection : android.content.ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            homeMessenger = Messenger(service)
            val msg = Message.obtain(null, MESSENGER_TRANSFORMATION)
            msg.replyTo = serviceMessenger
            homeMessenger?.send(msg)
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            homeMessenger = null
            isServiceBound = false
        }
    }

    private inner class ServiceMessengerHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {

                APP_STATE -> {
                    processAppState(msg.obj as Int)
                }

                OBSERVABLE_LOGIN_STATE -> {
                    isObservableLogIn = msg.obj as Boolean
                    checkLocationState()
                }

                OBSERVER_LOGIN_STATE -> {
                    isObserverLogIn = msg.obj as Boolean
                }

                SERVICE_STATE -> {
                    isServiceStarted = msg.obj as Boolean
                }

                DISCONNECT -> {
                    disconnectAction()
                }

                LOCATION_STATE -> {
                    locationChangeStateAction(msg.obj as Boolean)
                }


                OBSERVABLE_CONNECTING -> {
                    observableConnectingAction()
                }

                OBSERVABLE_CONNECT_SUCCESS -> {
                    observableConnectSuccessAction()
                }

                OBSERVABLE_CONNECT_FAIL -> {
                    observableConnectFailAction()
                }

                OBSERVABLE_LOGIN_SUCCESS -> {
                    observableLogInSuccessAction()
                }

                OBSERVABLE_LOGIN_FAIL -> {
                    observableLogInFailAction()
                }

                OBSERVABLE_LOGOUT_SUCCESS -> {
                    observableLogOutSuccessAction()
                }

                OBSERVABLE_LOGOUT_FAIL -> {
                    observableLogOutFailAction()
                }

                OBSERVABLE_RECEIVE_REQUEST_PERMISSION -> {
                    observableReceiveRequestPermissionAction(msg.obj as WebsocketDataModel)
                }

                OBSERVABLE_SENDING_PERMISSION_RESPONSE -> {
                    observableSendingPermissionResponseAction()
                }

                OBSERVABLE_GRANT_PERMISSION_FAIL -> {
                    observableGrantPermissionFailAction(msg.obj as WebsocketDataModel)
                }

                OBSERVABLE_DENY_PERMISSION_FAIL -> {
                    observableDenyPermissionFailAction()
                }

                OBSERVABLE_DENY_PERMISSION_SUCCESS -> {
                    observableDenyPermissionSuccessAction()
                }

                OBSERVABLE_GRANT_PERMISSION_SUCCESS -> {
                    observableGrantPermissionSuccessAction(msg.obj as String)
                }

                OBSERVABLE_SEND_DATA_SUCCESS -> {
                    observableSendDataSuccessAction(msg.obj as String)
                }

                OBSERVABLE_SEND_DATA_FAIL -> {
                    observableSendDataFailAction()
                }

                OBSERVABLE_ADDED_NEW_OBSERVER -> {
                    newObserverAddedAction(msg.obj as HashSet<String>)
                }

                OBSERVABLE_DISCONNECT_ALL_TARGETS -> {
                    disconnectAllObserverAction()
                }

                OBSERVABLE_RECONNECTING -> {
                    observableReconnectingActions()
                }

                OBSERVABLE_RECONNECT_SUCCESS -> {
                    observableReconnectSuccessActions()
                }

                OBSERVABLE_RECONNECT_FAIL -> {
                    observableReconnectFailActions()
                }


                OBSERVER_CONNECTING -> {
                    observerConnectingAction()
                }

                OBSERVER_CONNECT_SUCCESS -> {
                    observerConnectSuccessAction()
                }

                OBSERVER_CONNECT_FAIL -> {
                    observerConnectFailAction()
                }

                OBSERVER_LOGIN_SUCCESS -> {
                    observerLoginSuccessAction()
                }

                OBSERVER_LOGIN_FAIL -> {
                    observerLoginFailAction()
                }

                OBSERVER_SENDING_REQUEST_DATA -> {
                    observerSendingRequestDataAction()
                }

                OBSERVER_SEND_REQUEST_DATA_SUCCESS -> {
                    observerSendRequestDataSuccessAction()
                }

                OBSERVER_SEND_REQUEST_DATA_FAIL -> {
                    observerSendRequestDataFailAction()
                }

                OBSERVER_DISCONNECT_ALL_TARGETS -> {
                    observerDisconnectAllTargetAction()
                }

                OBSERVER_RECEIVE_DATA -> {
                    observerReceiveDataAction(msg.obj as DataModel)
                }

                OBSERVER_LAST_RECEIVED_DATA -> {
                    lastReceivedDataAction(msg.obj as DataModel)
                }

                OBSERVER_FAILURE -> {
                    failedObserveAction(msg.obj as ErrorDataModel)
                }

                OBSERVER_RECONNECTING -> {
                    observerReconnectingActions()
                }

                OBSERVER_RECONNECT_SUCCESS -> {
                    observerReconnectSuccessActions()
                }

                OBSERVER_RECONNECT_FAIL -> {
                    observerReconnectFailActions()
                }

                else -> super.handleMessage(msg)
            }
        }
    }
}