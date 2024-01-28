package ir.srp.rasad.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.BaseFragment
import ir.srp.rasad.databinding.FragmentSettingsBinding

@AndroidEntryPoint
class SettingsFragment : BaseFragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    override fun onBackPressed() {
        navController.navigate(R.id.homeFragment)
    }


    private fun initialize() {
        initProfileField()
        initToolbarBackButton()
    }

    private fun initProfileField() {
        binding.profile.setOnClickListener { onClickProfile() }
    }

    private fun onClickProfile() {
        navController.navigate(R.id.profileFragment)
    }

    private fun initToolbarBackButton() {
        binding.backSettingsBtn.setOnClickListener { onBackPressed() }
    }
}