package ir.srp.rasad.presentation.register

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
import ir.srp.rasad.core.utils.Validation.isEmailValid
import ir.srp.rasad.core.utils.Validation.isUsernameValid
import ir.srp.rasad.databinding.FragmentRegisterBinding
import ir.srp.rasad.domain.models.UserModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : BaseFragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var mobile: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mobile = arguments?.getString("mobile").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

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
        initSaveUserDataResult()
        initRegisterResponse()
        initRegisterButton()
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

    private fun initRegisterResponse() {
        lifecycleScope.launch {
            viewModel.registerResponse.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {
                        disableViews()
                    }

                    is Resource.Success -> {
                        response.data?.let { saveUserDataInPreference(it) }
                    }

                    is Resource.Error -> {
                        response.error(this@RegisterFragment)
                        enableViews()
                    }
                }
            }
        }
    }

    private fun disableViews() {
        binding.progressBar.visibility = View.VISIBLE
        binding.usernameEdtl.isEnabled = false
        binding.emailEdtl.isEnabled = false
    }

    private fun enableViews() {
        binding.progressBar.visibility = View.GONE
        binding.usernameEdtl.isEnabled = true
        binding.emailEdtl.isEnabled = true
    }

    private fun saveUserDataInPreference(userData: UserModel) {
        viewModel.saveUserData(userData)
    }

    private fun goToHomeFragment() {
        navController.navigate(R.id.homeFragment)
    }

    private fun initRegisterButton() {
        binding.registerBtn.setOnClickListener { onClickRegister() }
    }

    private fun onClickRegister() {
        val username = binding.usernameEdt.text.toString()
        val email = binding.emailEdt.text.toString()

        if (username.isEmpty()) {
            showError(this, getString(R.string.snackbar_empty_username))
            return
        }
        if (!isUsernameValid(username)) {
            showError(this, getString(R.string.snackbar_invalid_username))
            return
        }
        if (email.isNotEmpty())
            if (!isEmailValid(email)) {
                showError(this, getString(R.string.snackbar_invalid_email))
                return
            }

        viewModel.register(preparingUserData(username, email))
    }

    private fun preparingUserData(username: String, email: String): UserModel =
        UserModel(username = username, mobileNumber = mobile, email = email)
}