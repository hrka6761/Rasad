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
import ir.srp.rasad.core.Resource
import ir.srp.rasad.databinding.FragmentProfileBinding
import ir.srp.rasad.domain.models.UserModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : BaseFragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()


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


    private fun initialize() {
        initLoadUserDataResult()
        initToolbarBackButton()
    }

    private fun initLoadUserDataResult() {
        lifecycleScope.launch {
            viewModel.loadUserDataResult.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        response.data?.let {
                            setUserData(response.data)
                        }
                    }

                    is Resource.Error -> {
                        response.error(this@ProfileFragment)
                    }
                }
            }
        }
    }

    private fun setUserData(userData: UserModel) {
        binding.mobileTxt.text = userData.mobileNumber
        binding.usernameTxt.text = userData.username
        binding.emailTxt.text =
            if (userData.email.isNullOrEmpty())
                requireContext().getString(R.string.txt_no_email)
            else userData.email
    }

    private fun initToolbarBackButton() {
        binding.backProfileBtn.setOnClickListener { onBackPressed() }
    }
}