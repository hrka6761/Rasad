package ir.srp.rasad.presentation.home

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build.VERSION_CODES.S
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Parcelable
import ir.srp.rasad.core.Constants.COARSE_RESULT_KEY
import ir.srp.rasad.core.Constants.FINE_RESULT_KEY
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
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_STATE
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGIN_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_LOGOUT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_DENY_PERMISSION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_DISCONNECT_ALL_TARGETS
import ir.srp.rasad.core.Constants.OBSERVABLE_GRANT_PERMISSION_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_GRANT_PERMISSION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_RECEIVE_REQUEST_PERMISSION
import ir.srp.rasad.core.Constants.OBSERVABLE_GET_PERMISSION_DATA
import ir.srp.rasad.core.Constants.OBSERVABLE_RECONNECTING
import ir.srp.rasad.core.Constants.OBSERVABLE_RECONNECT_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_RECONNECT_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_REQUEST_TARGETS
import ir.srp.rasad.core.Constants.OBSERVABLE_SENDING_PERMISSION_RESPONSE
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
import ir.srp.rasad.core.Constants.SERVICE_BUNDLE_KEY
import ir.srp.rasad.core.Constants.SERVICE_DATA_KEY
import ir.srp.rasad.core.Constants.SERVICE_STATE
import ir.srp.rasad.core.Constants.SERVICE_TYPE_KEY
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVER
import ir.srp.rasad.core.Constants.STATE_DISABLE
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.TARGETS_KEY
import ir.srp.rasad.core.Resource
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

@Suppress("UNCHECKED_CAST", "HandlerLeak")
@RequiresApi(S)
@AndroidEntryPoint
class HomeFragment : BaseFragment(), RequestTargetListener {

    @Inject
    lateinit var jsonConverter: JsonConverter
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var permissionManager: PermissionManager
    private var homeMessenger: Messenger? = null
    private lateinit var serviceMessenger: Messenger
    private var isServiceBound = false
    private var isServiceStarted = false
    private var isObservableLogIn = false
    private var isObserverLogIn = false
    private var isObserverTrackStarted = false
    private val serviceConnection = ServiceConnection()
    private lateinit var savedTargets: HashSet<TargetModel>
    val trackUserBottomSheet = TrackUserBottomSheet(this)
    private var marker: Marker? = null
    private var permissionDialog: AlertDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this, PermissionsRequestCallback())
        serviceMessenger = Messenger(Handler())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        bindService()
        return binding.root
    }

    @RequiresApi(TIRAMISU)
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

        startService(START_SERVICE_OBSERVER, targets)
        disableViews()
        trackUserBottomSheet.dismiss()
    }

    override fun onRemoveTarget(targetName: String) {
        removeTarget(targetName)
    }


    @RequiresApi(TIRAMISU)
    private fun initialize() {
        initGetTargetsResult()
        initSettingsButton()
        initTrackMeButton()
        initTrackOtherButton()
    }

    private fun initGetTargetsResult() {
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

                        args.putParcelableArray(TARGETS_KEY, targets.toTypedArray())
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

    @RequiresApi(TIRAMISU)
    private fun initTrackMeButton() {
        binding.onOffFab.setOnClickListener { onShortClickOnOff() }
        binding.onOffFab.setOnLongClickListener {
            onLongClickOnOff()
            true
        }
    }

    @RequiresApi(TIRAMISU)
    private fun onLongClickOnOff() {
        if (!permissionManager.hasPreciseLocationPermission() && !permissionManager.hasApproximateLocationPermission()) {
            showSimpleDialog(
                context = requireContext(),
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

            return
        }

        if (!permissionManager.hasBackgroundLocationPermission()) {
            showSimpleDialog(
                context = requireContext(),
                msg = getString(R.string.dialog_background_location_dialog_msg),
                negativeAction = { dialog ->
                    showWarning(this, getString(R.string.snackbar_background_location_negative_msg))
                    dialog.dismiss()
                }, positiveAction = { _ ->
                    permissionManager.getBackgroundLocationPermission()
                }
            )

            return
        }

        if (!permissionManager.hasNotificationPermission()) {
            showSimpleDialog(
                context = requireContext(),
                msg = getString(R.string.dialog_notification_dialog_msg),
                negativeAction = { dialog ->
                    showWarning(this, getString(R.string.snackbar_notification_negative_msg))
                    dialog.dismiss()
                },
                positiveAction = { _ ->
                    permissionManager.getNotificationPermission()
                }
            )

            return
        }

        if (isObservableLogIn)
            startService(STOP_SERVICE_OBSERVABLE)
        else
            startService(START_SERVICE_OBSERVABLE)

        disableViews()
    }

    private fun onShortClickOnOff() {
        showWarning(this, getString(R.string.snackbar_short_click_msg))
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

    @RequiresApi(TIRAMISU)
    private fun initTrackOtherButton() {
        binding.addMemberFab.setOnClickListener { showTrackUserSheet() }
    }

    @RequiresApi(TIRAMISU)
    private fun showTrackUserSheet() {
        if (!permissionManager.hasNotificationPermission()) {
            showSimpleDialog(
                context = requireContext(),
                msg = getString(R.string.dialog_notification_dialog_msg),
                negativeAction = { dialog ->
                    showWarning(this, getString(R.string.snackbar_notification_negative_msg))
                    dialog.dismiss()
                },
                positiveAction = { _ ->
                    permissionManager.getNotificationPermission()
                }
            )

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

            args.putParcelableArray(TARGETS_KEY, targets.toTypedArray())
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

    private fun startService(type: String, data: Any? = null) {
        val intent = Intent(requireContext(), MainService::class.java)
        val bundle = Bundle()
        bundle.putString(SERVICE_TYPE_KEY, type)
        data?.let { bundle.putParcelableArray(SERVICE_DATA_KEY, data as Array<Parcelable>) }
        intent.putExtra(SERVICE_BUNDLE_KEY, bundle)
        requireContext().startForegroundService(intent)
    }

    private fun startServiceForAction(action: String) {
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

    private fun removeTarget(targetName: String) {
        for (target in savedTargets) {
            if (target.name == targetName) {
                savedTargets.remove(target)
                break
            }
        }
        viewModel.saveTargets(savedTargets)
    }

    private fun onClickCancelWaiting() {
        isServiceStarted = false
        isObserverLogIn = false
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        enableViews()
        val msg = Message.obtain(null, CANCEL_OBSERVE)
        homeMessenger?.send(msg)
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


    private fun observableConnectionSuccessAction() {

    }

    private fun observableConnectingAction() {
        isServiceStarted = true
        binding.onOffFab.setImageResource(R.drawable.powering)
    }

    private fun observableConnectionFailAction() {
        isServiceStarted = false
        enableViews()
        binding.onOffFab.setImageResource(R.drawable.power_off)
        showError(this, getString(R.string.observable_connect_fail_msg))
    }

    private fun observableLogInSuccessAction() {
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        isObservableLogIn = true
        enableViews()
        disableObserver()
        binding.onOffFab.setImageResource(R.drawable.power_on)
    }

    private fun observableLogInFailAction() {
        isServiceStarted = false
        enableViews()
        binding.onOffFab.setImageResource(R.drawable.power_off)
        showError(this, getString(R.string.observable_login_fail_msg))
    }

    private fun observableLogOutSuccessAction() {
        isServiceStarted = false
        isObservableLogIn = false
        binding.onOffFab.setImageResource(R.drawable.power_off)
        binding.trackersContainer.visibility = View.GONE
        enableViews()
        enableObserver()
    }

    private fun observableLogOutFailAction() {
        enableViews()
        showError(this, getString(R.string.observable_logout_fail_msg))
    }

    private fun observableReceiveRequestPermissionAction(data: WebsocketDataModel) {
        var interval = 0
        val permissionsList = data.data?.split(",")
        for (permission in permissionsList!!) {
            if (permission.contains(data.username))
                interval = permission.replace(data.username, "").toInt()
        }

        val msg = if (interval == 0)
            "${data.username} want to track your location when it changes.\nAre you ok ?"
        else
            "${data.username} want to track your location every $interval minutes.\nAre you ok ?"

        permissionDialog = showSimpleDialog(
            context = requireContext(),
            msg = msg,
            negativeAction = { _ ->
                startServiceForAction(DENY_PERMISSION_ACTION)
            },
            positiveAction = { _ ->
                startServiceForAction(GRANT_PERMISSION_ACTION)
            }
        )
    }

    private fun observableSendingPermissionResponseAction() {
        permissionDialog?.dismiss()
        permissionDialog = null
    }

    private fun observableGrantPermissionFailAction(data: WebsocketDataModel) {
        observableReceiveRequestPermissionAction(data)
        showError(this, "Failed to send permission response !!!")
    }

    private fun observableDenyPermissionFailAction() {
        showError(this, "Failed to send permission response !!!")
    }

    private fun observableGrantPermissionSuccessAction(target: String) {
        showMessage(this, "You Grant permission for $target successfully.")
    }

    private fun observableDenyPermissionSuccessAction() {

    }

    private fun observableSendDataSuccessAction(data: DataModel) {

    }

    private fun newObserverAddedAction(observableTargets: HashSet<String>) {
        binding.trackersContainer.visibility = View.VISIBLE
        binding.trackersNumber.text = observableTargets.size.toString()
    }

    private fun disconnectAllObserverAction() {
        binding.trackersContainer.visibility = View.GONE
    }

    private fun observableReconnectingActions() {
        binding.onOffFab.setImageResource(R.drawable.powering)
        binding.waitingTxt.text = getString(R.string.reconnecting_msg)
        binding.cancelWaitingBtn.visibility = View.VISIBLE
        binding.cancelWaitingBtn.setOnClickListener { onClickCancelReconnectObservable() }
        disableViews()
    }

    private fun onClickCancelReconnectObservable() {
        isServiceStarted = false
        isObservableLogIn = false
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        binding.onOffFab.setImageResource(R.drawable.power_off)
        enableViews()
        val msg = Message.obtain(null, CANCEL_RECONNECT_OBSERVABLE)
        homeMessenger?.send(msg)
    }

    private fun observableReconnectSuccessActions() {

    }

    private fun observableReconnectFailActions() {

    }


    private fun observerConnectingAction() {
        isServiceStarted = true
    }

    private fun observerConnectSuccessAction() {

    }

    private fun observerConnectFailAction() {
        enableViews()
        isServiceStarted = false
        showError(this, getString(R.string.observer_connect_fail_msg))
    }

    private fun observerLoginSuccessAction() {
        isObserverLogIn = true
    }

    private fun observerLoginFailAction() {
        enableViews()
        isServiceStarted = false
        showError(this, getString(R.string.observer_login_fail_msg))
    }

    private fun observerSendingRequestDataAction() {

    }

    private fun observerSendRequestDataSuccessAction() {
        binding.waitingTxt.text = getString(R.string.txt_response_waiting)
        binding.cancelWaitingBtn.setOnClickListener { onClickCancelWaiting() }
        binding.cancelWaitingBtn.visibility = View.VISIBLE
    }

    private fun observerSendRequestDataFailAction() {
        isServiceStarted = false
        isObserverLogIn = false
        enableViews()
        showError(this, getString(R.string.observer_send_request_data_fail_msg))
    }

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

    private fun receiveLastDataAction(lastReceivedData: DataModel) {
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

    private fun failedObserveAction(errorData: ErrorDataModel) {
        isServiceStarted = false
        isObserverLogIn = false
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        enableViews()
        showError(this, "${errorData.code}: ${errorData.reason}")
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

    private fun observerReconnectingActions() {
        binding.waitingTxt.text = getString(R.string.reconnecting_msg)
        binding.cancelWaitingBtn.visibility = View.VISIBLE
        binding.cancelWaitingBtn.setOnClickListener { onClickCancelReconnectObserver() }
        disableViews()
    }

    private fun onClickCancelReconnectObserver() {
        isObserverTrackStarted = false
        isObserverLogIn = false
        isServiceStarted = false
        binding.cancelWaitingBtn.visibility = View.GONE
        binding.waitingTxt.text = getString(R.string.txt_waiting)
        enableViews()
        val msg = Message.obtain(null, CANCEL_RECONNECT_OBSERVER)
        homeMessenger?.send(msg)
    }

    private fun observerReconnectSuccessActions() {

    }

    private fun observerReconnectFailActions() {

    }


    private fun disconnectAction() {
        isServiceStarted = false
        isObservableLogIn = false
        isObserverLogIn = false
        isObserverTrackStarted = false
        permissionDialog?.dismiss()
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

            }

            OBSERVABLE_STATE_PERMISSION_REQUEST -> {
                val requestPermissionDataMsg =
                    Message.obtain(null, OBSERVABLE_GET_PERMISSION_DATA)
                homeMessenger?.send(requestPermissionDataMsg)
                binding.cancelWaitingBtn.visibility = View.GONE
                binding.waitingTxt.text = getString(R.string.txt_waiting)
                enableViews()
            }

            OBSERVABLE_STATE_SENDING_DATA -> {
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
                    startService(STOP_SERVICE_OBSERVABLE)
                else
                    startService(START_SERVICE_OBSERVABLE)
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

    private inner class Handler : android.os.Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {

                APP_STATE -> {
                    processAppState(msg.obj as Int)
                }

                OBSERVABLE_LOGIN_STATE -> {
                    isObservableLogIn = msg.obj as Boolean
                    if (isObservableLogIn) {
                        binding.onOffFab.setImageResource(R.drawable.power_on)
                        disableObserver()
                    } else
                        binding.onOffFab.setImageResource(R.drawable.power_off)
                }

                OBSERVER_LOGIN_STATE -> {
                    isObserverLogIn = msg.obj as Boolean
                    if (isObserverLogIn)
                        disableObservable()
                }

                SERVICE_STATE -> {
                    isServiceStarted = msg.obj as Boolean
                }

                DISCONNECT -> {
                    disconnectAction()
                }


                OBSERVABLE_CONNECTING -> {
                    observableConnectingAction()
                }

                OBSERVABLE_CONNECT_SUCCESS -> {
                    observableConnectionSuccessAction()
                }

                OBSERVABLE_CONNECT_FAIL -> {
                    observableConnectionFailAction()
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
                    val dataModel = jsonConverter.convertJsonStringToObject(
                        msg.obj as String,
                        DataModel::class.java
                    ) as DataModel

                    observableSendDataSuccessAction(dataModel)
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
                    receiveLastDataAction(msg.obj as DataModel)
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