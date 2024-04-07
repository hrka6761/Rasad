package ir.srp.rasad.presentation.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.BaseFragment
import ir.srp.rasad.core.Constants.EDIT_PROFILE_KEY
import ir.srp.rasad.core.Constants.USERNAME_ARG_VALUE
import ir.srp.rasad.core.Constants.EMAIL_VALUE
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.utils.MessageViewer.showMessage
import ir.srp.rasad.databinding.FragmentProfileBinding
import ir.srp.rasad.domain.models.UserModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment(), EditCLickListener {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val editProfileBottomSheet = EditProfileBottomSheet(this)


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
        viewModel.loadUserData()
    }

    override fun onBackPressed() {
        navController.navigate(R.id.settingsFragment)
    }

    override fun onEditUsername(newUsername: String) {
        disableViews()
        editProfileBottomSheet.dismiss()
        val token = viewModel.loadUserDataResult.value.data?.token.toString()
        viewModel.updateUserName(token, preparingUserDataToUpdateUsername(newUsername))
    }

    override fun onEditEmail(newEmail: String) {
        disableViews()
        editProfileBottomSheet.dismiss()
        val token = viewModel.loadUserDataResult.value.data?.token.toString()
        viewModel.updateEmail(token, preparingUserDataToUpdateEmail(newEmail))
    }


    private fun initialize() {
        initLoadUserDataResult()
        initToolbarBackButton()
        initEmailUsernameButton()
        initEmailEditButton()
        initUpdateUsernameResponse()
        initUpdateEmailResponse()
    }

    private fun initLoadUserDataResult() {
        lifecycleScope.launch {
            viewModel.loadUserDataResult.collect { response ->
                when (response) {
                    is Resource.Initial -> {
                        disableViews()
                    }

                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        response.data?.let {
                            setUserData(response.data)
                        }
                        enableViews()
                    }

                    is Resource.Error -> {
                        response.error(this@ProfileFragment)
                    }
                }
            }
        }
    }

    private fun setUserData(userData: UserModel) {
        binding.mobileTxt.text = userData.mobile
        binding.usernameTxt.text = userData.username
        binding.emailTxt.text =
            if (userData.email.isNullOrEmpty())
                requireContext().getString(R.string.txt_no_email)
            else userData.email
    }

    private fun initToolbarBackButton() {
        binding.backProfileBtn.setOnClickListener { onBackPressed() }
    }

    private fun initEmailUsernameButton() {
        binding.username.setOnClickListener { onClickUsernameEmail() }
    }

    private fun onClickUsernameEmail() {
        val args = Bundle()
        args.putString(EDIT_PROFILE_KEY, USERNAME_ARG_VALUE)
        editProfileBottomSheet.arguments = args
        editProfileBottomSheet.show(
            requireActivity().supportFragmentManager,
            editProfileBottomSheet.tag
        )
    }

    private fun initEmailEditButton() {
        binding.email.setOnClickListener { onClickEditEmail() }
    }

    private fun onClickEditEmail() {
        val args = Bundle()
        args.putString(EDIT_PROFILE_KEY, EMAIL_VALUE)
        editProfileBottomSheet.arguments = args
        editProfileBottomSheet.show(
            requireActivity().supportFragmentManager,
            editProfileBottomSheet.tag
        )
    }

    private fun preparingUserDataToUpdateUsername(username: String): UserModel {
        val id = viewModel.loadUserDataResult.value.data?.id
        return UserModel(id = id, username = username)
    }

    private fun preparingUserDataToUpdateEmail(email: String): UserModel {
        val id = viewModel.loadUserDataResult.value.data?.id
        return UserModel(id = id, email = email)
    }

    private fun initUpdateUsernameResponse() {
        lifecycleScope.launch {
            viewModel.updateUsernameResponse.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        response.data?.let {
                            val newUserData = setUsernameAndGetUserModel(it)
                            viewModel.updateUserData(newUserData)
                            showMessage(
                                this@ProfileFragment,
                                getString(R.string.snackbar_successfully_done)
                            )
                        }
                        viewModel.loadUserData()
                    }

                    is Resource.Error -> {
                        response.error(this@ProfileFragment)
                        enableViews()
                    }
                }
            }
        }
    }

    private fun initUpdateEmailResponse() {
        lifecycleScope.launch {
            viewModel.updateEmailResponse.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        response.data?.let {
                            val newUserData = setEmailAndGetUserModel(it)
                            viewModel.updateUserData(newUserData)
                            showMessage(
                                this@ProfileFragment,
                                getString(R.string.snackbar_successfully_done)
                            )
                        }
                        viewModel.loadUserData()
                    }

                    is Resource.Error -> {
                        response.error(this@ProfileFragment)
                        enableViews()
                    }
                }
            }
        }
    }

    private fun setUsernameAndGetUserModel(userData: UserModel): UserModel {
        val id = viewModel.loadUserDataResult.value.data?.id
        val username = userData.username
        val mobile = viewModel.loadUserDataResult.value.data?.mobile
        val email = viewModel.loadUserDataResult.value.data?.email
        val token = viewModel.loadUserDataResult.value.data?.token
        return UserModel(id, username, mobile, email, token)
    }

    private fun setEmailAndGetUserModel(userData: UserModel): UserModel {
        val id = viewModel.loadUserDataResult.value.data?.id
        val username = viewModel.loadUserDataResult.value.data?.username
        val mobile = viewModel.loadUserDataResult.value.data?.mobile
        val email = userData.email
        val token = viewModel.loadUserDataResult.value.data?.token
        return UserModel(id, username, mobile, email, token)
    }

    private fun enableViews() {
        binding.progressBarContainer.visibility = View.GONE
        binding.username.isEnabled = true
        binding.email.isEnabled = true
        binding.backProfileBtn.isEnabled = true
    }

    private fun disableViews() {
        binding.progressBarContainer.visibility = View.VISIBLE
        binding.username.isEnabled = false
        binding.email.isEnabled = false
        binding.backProfileBtn.isEnabled = false
    }
}