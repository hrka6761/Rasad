package ir.srp.rasad.presentation.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.BaseFragment
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.utils.Validation.checkMobilNumberValidation
import ir.srp.rasad.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : BaseFragment() {

    private lateinit var binding: FragmentLoginBinding
    private val viewModel: LoginViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }


    private fun initialize() {
        initOtpResponse()
        initEnterButton()
    }

    private fun initOtpResponse() {
        lifecycleScope.launch {
            viewModel.otpResponse.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {
                        disableViews()
                    }

                    is Resource.Success -> {
                        goToOtpFragment(binding.mobileEdt.text.toString())
                    }

                    is Resource.Error -> {
                        response.error(this@LoginFragment)
                        enableViews()
                    }
                }
            }
        }
    }

    private fun disableViews() {
        binding.progressBar.visibility = View.VISIBLE
        binding.enterBtn.isEnabled = false
        binding.mobileEdtl.isEnabled = false
    }

    private fun enableViews() {
        binding.progressBar.visibility = View.GONE
        binding.enterBtn.isEnabled = true
        binding.mobileEdtl.isEnabled = true
    }

    private fun goToOtpFragment(mobile: String) {
        val action = LoginFragmentDirections.actionLoginFragmentToOtpFragment(mobile)
        navController.navigate(action)
    }

    private fun initEnterButton() {
        binding.enterBtn.setOnClickListener { onClickEnterButton() }
    }

    private fun onClickEnterButton() {
        val mobileNumber = binding.mobileEdt.text.toString()
        if (mobileNumber.isEmpty()) {
            showError(this, getString(R.string.snackbar_empty_mobile_number))
            return
        }
        if (checkMobilNumberValidation(mobileNumber).isEmpty())
            viewModel.requestOtp(mobileNumber)
        else
            showError(this, getString(R.string.snackbar_incorrect_mobile_number))
    }
}