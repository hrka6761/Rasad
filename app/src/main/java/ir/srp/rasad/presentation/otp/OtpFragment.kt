package ir.srp.rasad.presentation.otp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.goodiebag.pinview.Pinview
import com.goodiebag.pinview.Pinview.PinViewEventListener
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.BaseFragment
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.utils.Validation.isOtpValid
import ir.srp.rasad.databinding.FragmentOtpBinding
import ir.srp.rasad.domain.models.LoginDataModel
import ir.srp.rasad.domain.models.UserModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class OtpFragment : BaseFragment() {

    private lateinit var binding: FragmentOtpBinding
    private val viewModel: OtpViewModel by viewModels()
    lateinit var mobile: String
        private set


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mobile = arguments?.getString("mobile").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentOtpBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    override fun onBackPressed() {
        navController.navigate(R.id.loginFragment)
    }


    private fun initialize() {
        initToolbarBackButton()
        initLoginResponse()
        initSaveUserDataResult()
        initPinView()
        initSendOtpButton()
        initMobileTextView()
    }

    private fun initToolbarBackButton() {
        binding.backOtpBtn.setOnClickListener { onBackPressed() }
    }

    private fun initMobileTextView() {
        binding.mobileTxt.text = mobile
    }

    private fun initSaveUserDataResult() {
        lifecycleScope.launch {
            viewModel.saveUserDataResult.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        viewModel.setUserState()
                        goToHomeFragment()
                    }

                    is Resource.Error -> {}
                }
            }
        }
    }

    private fun initLoginResponse() {
        lifecycleScope.launch {
            viewModel.sendOtpResponse.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {
                        disableViews()
                    }

                    is Resource.Success -> {
                        response.data?.let {
                            saveUserDataInPreference(it)
                        }
                    }

                    is Resource.Error -> {
                        response.error(this@OtpFragment)
                        enableViews()
                    }
                }
            }
        }
    }

    private fun disableViews() {
        binding.progressBar.visibility = View.VISIBLE
        binding.pinview.isEnabled = false
        binding.sendBtn.isEnabled = false
    }

    private fun enableViews() {
        binding.progressBar.visibility = View.GONE
        binding.pinview.isEnabled = true
        binding.sendBtn.isEnabled = true
    }

    private fun saveUserDataInPreference(userData: UserModel) {
        viewModel.saveUserData(userData)
    }

    private fun goToHomeFragment() {
        navController.navigate(R.id.homeFragment)
    }

    private fun initSendOtpButton() {
        binding.sendBtn.setOnClickListener { onClickSend() }
    }

    private fun onClickSend() {
        if (isOtpValid(binding.pinview.value))
            viewModel.login(LoginDataModel(mobile, "111111"))
        else
            showError(this, getString(R.string.snackbar_incorrect_otp))
    }

    private fun initPinView() {
        binding.pinview.setPinViewEventListener(PinEventListener())
    }


    private inner class PinEventListener() : PinViewEventListener {
        override fun onDataEntered(pinview: Pinview?, fromUser: Boolean) {
            binding.sendBtn.isEnabled = !pinview?.value.isNullOrEmpty()
        }
    }
}