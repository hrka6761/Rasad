package ir.srp.rasad.presentation.home

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context.BIND_AUTO_CREATE
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Build.VERSION_CODES.O
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
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.BaseFragment
import ir.srp.rasad.core.Constants.MESSENGER_TRANSFORMATION
import ir.srp.rasad.core.Constants.OBSERVABLE_CLOSED_CONNECTION
import ir.srp.rasad.core.Constants.OBSERVABLE_CLOSING_CONNECTION
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECTION_FAIL
import ir.srp.rasad.core.Constants.OBSERVABLE_CONNECTION_SUCCESS
import ir.srp.rasad.core.Constants.OBSERVABLE_SEND_MESSAGE_FAIL
import ir.srp.rasad.core.Constants.SERVICE_BUNDLE
import ir.srp.rasad.core.Constants.SERVICE_DATA
import ir.srp.rasad.core.Constants.SERVICE_TYPE
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.START_SERVICE_OBSERVER
import ir.srp.rasad.core.Constants.STOP_SERVICE_OBSERVABLE
import ir.srp.rasad.core.Constants.TARGETS_PREFERENCE_KEY
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.WebSocketDataType
import ir.srp.rasad.core.utils.Dialog.showSimpleDialog
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.utils.MessageViewer.showWarning
import ir.srp.rasad.core.utils.PermissionManager
import ir.srp.rasad.databinding.FragmentHomeBinding
import ir.srp.rasad.domain.models.TargetModel
import ir.srp.rasad.presentation.services.MainService
import kotlinx.coroutines.launch

@Suppress("UNCHECKED_CAST")
@AndroidEntryPoint
class HomeFragment : BaseFragment(), RequestTargetListener {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var permissionManager: PermissionManager
    private var homeMessenger: Messenger? = null
    private lateinit var serviceMessenger: Messenger
    private var isServiceBound = false
    private var isServiceStarted = false
    private val serviceConnection = ServiceConnection()
    private lateinit var savedTargets: HashSet<TargetModel>
    val trackUserBottomSheet = TrackUserBottomSheet(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this, PermissionsRequestCallback())
        serviceMessenger = Messenger(Handler())
        bindService()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    override fun onStop() {
        super.onStop()
        if (isServiceBound)
            unBindService()
    }

    @RequiresApi(O)
    override fun onRequest(isNew: Boolean, vararg targets: TargetModel) {
        if (isNew)
            for (target in targets)
                saveNewTarget(target)

        startService(START_SERVICE_OBSERVER, targets)
        trackUserBottomSheet.dismiss()
    }

    override fun onRemoveTarget(targetName: String) {
        removeTarget(targetName)
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

                        args.putParcelableArray(TARGETS_PREFERENCE_KEY, targets.toTypedArray())
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
        navController.navigate(R.id.settingsFragment)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun initTrackMeButton() {
        binding.onOffFab.setOnClickListener { onShortClickOnOff() }
        binding.onOffFab.setOnLongClickListener {
            onLongClickOnOff()
            true
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
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

        if (isServiceStarted)
            stopService(STOP_SERVICE_OBSERVABLE)
        else
            startService(START_SERVICE_OBSERVABLE)

        disableViews()
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

    private fun onShortClickOnOff() {
        showWarning(this, getString(R.string.snackbar_short_click_msg))
    }

    private fun initTrackOtherButton() {
        binding.addMemberFab.setOnClickListener { showTrackUserDialog() }
    }

    private fun showTrackUserDialog() {
        if (this::savedTargets.isInitialized) {
            val args = Bundle()
            val targets = mutableListOf<TargetModel>()

            for (target in savedTargets)
                targets.add(target)

            args.putParcelableArray(TARGETS_PREFERENCE_KEY, targets.toTypedArray())
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
        requireContext().unbindService(serviceConnection)
        isServiceBound = false
    }

    @RequiresApi(O)
    private fun startService(type: String, data: Any? = null) {
        val intent = Intent(requireContext(), MainService::class.java)
        val bundle = Bundle()
        bundle.putString(SERVICE_TYPE, type)
        data?.let { bundle.putParcelableArray(SERVICE_DATA, data as Array<Parcelable>) }
        intent.putExtra(SERVICE_BUNDLE, bundle)
        requireContext().startForegroundService(intent)
    }

    @RequiresApi(O)
    private fun stopService(type: String, data: Any? = null) {
        val intent = Intent(requireContext(), MainService::class.java)
        val bundle = Bundle()
        bundle.putString(SERVICE_TYPE, type)
        data?.let { bundle.putParcelableArray(SERVICE_DATA, data as Array<Parcelable>) }
        intent.putExtra(SERVICE_BUNDLE, bundle)
        requireContext().startForegroundService(intent)
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

    private fun observableConnectionFailAction() {
        enableViews()
        binding.onOffFab.backgroundTintList =
            ColorStateList.valueOf(requireContext().resources.getColor(R.color.red))
        isServiceStarted = false
        showError(this, "Failed to connect to the server !!!")
    }

    private fun observableConnectionSuccessAction() {
        enableViews()
        binding.onOffFab.backgroundTintList =
            ColorStateList.valueOf(requireContext().resources.getColor(R.color.green))
        isServiceStarted = true
    }

    private fun observableSendMessageFailAction(type: String) {
        enableViews()
        when(type) {
            WebSocketDataType.LogInObservable.name -> {
                binding.onOffFab.backgroundTintList =
                    ColorStateList.valueOf(requireContext().resources.getColor(R.color.red))
                isServiceStarted = false
            }
        }
        showError(this, "Failed to login to the server !!!")
    }

    private fun observableClosingConnectionAction() {
        binding.onOffFab.backgroundTintList =
            ColorStateList.valueOf(requireContext().resources.getColor(R.color.orange))
    }

    private fun observableClosedConnectionAction() {
        enableViews()
        binding.onOffFab.backgroundTintList =
            ColorStateList.valueOf(requireContext().resources.getColor(R.color.red))
        isServiceStarted = false
    }


    private inner class PermissionsRequestCallback : ActivityResultCallback<Map<String, Boolean>> {
        @RequiresApi(Build.VERSION_CODES.Q)
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
                    stopService(STOP_SERVICE_OBSERVABLE)
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

    @SuppressLint("HandlerLeak")
    private inner class Handler : android.os.Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                OBSERVABLE_CONNECTION_SUCCESS -> { observableConnectionSuccessAction() }
                OBSERVABLE_CONNECTION_FAIL -> { observableConnectionFailAction() }
                OBSERVABLE_SEND_MESSAGE_FAIL -> { observableSendMessageFailAction(msg.obj as String) }
                OBSERVABLE_CLOSING_CONNECTION -> { observableClosingConnectionAction() }
                OBSERVABLE_CLOSED_CONNECTION -> { observableClosedConnectionAction() }
                else -> super.handleMessage(msg)
            }
        }
    }
}