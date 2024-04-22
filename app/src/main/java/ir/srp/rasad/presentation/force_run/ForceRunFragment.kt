package ir.srp.rasad.presentation.force_run

import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.BaseFragment
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.utils.Dialog
import ir.srp.rasad.core.utils.MessageViewer.showMessage
import ir.srp.rasad.core.utils.MessageViewer.showWarning
import ir.srp.rasad.core.utils.PermissionManager
import ir.srp.rasad.core.utils.Validation.checkMobilNumberValidation
import ir.srp.rasad.core.utils.Validation.checkPasswordValidation
import ir.srp.rasad.databinding.FragmentForceRunBinding
import ir.srp.rasad.domain.models.ForceRunDataModel
import kotlinx.coroutines.launch

@RequiresApi(TIRAMISU)
@AndroidEntryPoint
class ForceRunFragment : BaseFragment() {

    private lateinit var binding: FragmentForceRunBinding
    private val viewModel: ForceRunViewModel by viewModels()
    private lateinit var permissionManager: PermissionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        permissionManager = PermissionManager(this, PermissionsRequestCallback())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentForceRunBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    override fun onBackPressed() {
        navController.navigate(R.id.settingsFragment)
    }


    private fun initialize() {
        initForceRunInfoResult()
        initToolbarBackButton()
        initForceRunSwitch()
    }

    private fun initForceRunInfoResult() {
        lifecycleScope.launch {
            viewModel.forceRunInfoResult.collect { result ->
                when (result) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val data = result.data
                        if (data != null) {
                            if (data.mobileNumber.isNotEmpty()) {
                                binding.mobileEdt.setText(data.mobileNumber)
                                binding.mobileEdt.isEnabled = false
                            }

                            if (data.password.isNotEmpty()) {
                                binding.passwordEdt.setText(data.password)
                                binding.passwordEdt.isEnabled = false
                            }

                            binding.forceRunSwitch.isChecked = data.state
                        } else {
                            binding.mobileEdtl.setEndIconActivated(false)
                            binding.mobileEdtl.endIconMode = TextInputLayout.END_ICON_NONE
                            binding.passwordEdtl.endIconMode = TextInputLayout.END_ICON_NONE
                            binding.forceRunSwitch.isChecked = false
                        }
                    }

                    is Resource.Error -> {}
                }
            }
        }

        viewModel.loadForceRunInfo()
    }

    private fun initForceRunSwitch() {
        binding.forceRunSwitch.setOnClickListener { onClickForceRun() }
    }

    private fun onClickForceRun() {
        if (!binding.forceRunSwitch.isChecked) {
            binding.mobileEdt.text?.clear()
            binding.passwordEdt.text?.clear()
            binding.mobileEdt.isEnabled = true
            binding.passwordEdt.isEnabled = true
            viewModel.saveForceRunInfo(
                ForceRunDataModel(
                    state = false,
                    mobileNumber = binding.mobileEdt.text.toString(),
                    password = binding.passwordEdt.text.toString()
                )
            )
        } else {
            if (permissionManager.hasReceiveSMSPermission()) {
                val mobilValidation =
                    checkMobilNumberValidation(binding.mobileEdt.text.toString())
                if (mobilValidation.isNotEmpty()) {
                    showWarning(this, mobilValidation)
                    binding.forceRunSwitch.isChecked = false
                    return
                }

                val passwordValidation =
                    checkPasswordValidation(binding.passwordEdt.text.toString())
                if (passwordValidation.isNotEmpty()) {
                    showWarning(this, passwordValidation)
                    binding.forceRunSwitch.isChecked = false
                    return
                }
                binding.mobileEdt.isEnabled = false
                binding.passwordEdt.isEnabled = false
                viewModel.saveForceRunInfo(
                    ForceRunDataModel(
                        state = true,
                        mobileNumber = binding.mobileEdt.text.toString(),
                        password = binding.passwordEdt.text.toString()
                    )
                )
            } else {
                binding.forceRunSwitch.isChecked = false
                activity?.let {
                    Dialog.showSimpleDialog(
                        activity = it,
                        msg = "You need to grant receive SMS permission to detect sent SMS to run app",
                        negativeAction = { dialog ->
                            showWarning(
                                this,
                                "You cannot use this feature without access to receive SMS"
                            )
                            dialog.dismiss()
                        },
                        positiveAction = { _ ->
                            permissionManager.getReceiveSMSPermission()
                        }
                    )
                }
            }
        }
    }

    private fun initToolbarBackButton() {
        binding.backSettingsBtn.setOnClickListener { onBackPressed() }
    }


    private inner class PermissionsRequestCallback : ActivityResultCallback<Map<String, Boolean>> {
        override fun onActivityResult(result: Map<String, Boolean>) {
            if (permissionManager.hasReceiveSMSPermission())
                showMessage(this@ForceRunFragment, "Now, please fill mobile and password then turn on the force run")
            else
                showWarning(this@ForceRunFragment, "You cannot use this feature without access to receive SMS")
        }
    }
}