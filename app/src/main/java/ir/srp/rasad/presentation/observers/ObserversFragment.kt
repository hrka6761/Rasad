package ir.srp.rasad.presentation.observers

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import ir.srp.rasad.R
import ir.srp.rasad.core.BaseFragment
import ir.srp.rasad.core.Resource
import ir.srp.rasad.databinding.FragmentObserversBinding
import ir.srp.rasad.domain.models.ObserverOperationModel
import ir.srp.rasad.domain.models.PermittedObserversModel
import ir.srp.rasad.domain.models.UserModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ObserversFragment : BaseFragment(), ObserversAdapter.ObserverClickListener {

    private lateinit var binding: FragmentObserversBinding
    private val viewModel: ObserversViewModel by viewModels()
    private lateinit var adapter: ObserversAdapter
    private lateinit var userData: UserModel
    private var clickedObserver: PermittedObserversModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = ObserversAdapter(requireContext(), this@ObserversFragment)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentObserversBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }

    override fun onBackPressed() {
        navController.navigate(R.id.settingsFragment)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClickDelete(observer: PermittedObserversModel) {
        clickedObserver = observer
        val observerOperationModel = ObserverOperationModel(
            userData.id!!,
            observer.username,
            observer.target
        )
        viewModel.deleteObserver(userData.token!!, observerOperationModel)
    }

    override fun onEmptyList() {
        noObserverAction()
    }


    private fun initialize() {
        disableViews()
        initGetObserversResponse()
        initDeleteObserverResponse()
        initLoadUserDataResult()
        initToolbarBackButton()
    }

    private fun initDeleteObserverResponse() {
        lifecycleScope.launch {
            viewModel.deleteObserverResponse.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {
                        disableViews()
                    }

                    is Resource.Success -> {
                        clickedObserver?.let {
                            adapter.deleteRow(it)
                            clickedObserver = null
                        }
                        enableViews()
                    }

                    is Resource.Error -> {
                        clickedObserver = null
                        enableViews()
                    }
                }
            }
        }
    }

    private fun initGetObserversResponse() {
        lifecycleScope.launch {
            viewModel.observers.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if (response.data != null) {
                            if (response.data.isNotEmpty()) {
                                adapter.setList(response.data.toMutableList())
                                binding.rv.layoutManager = LinearLayoutManager(
                                    requireContext(),
                                    LinearLayoutManager.VERTICAL,
                                    false
                                )
                                binding.rv.adapter = adapter
                            } else
                                binding.noList.visibility = View.VISIBLE
                        } else
                            binding.noList.visibility = View.VISIBLE

                        enableViews()
                    }

                    is Resource.Error -> {
                        response.error(this@ObserversFragment)
                    }
                }
            }
        }
    }

    private fun initLoadUserDataResult() {
        lifecycleScope.launch {
            viewModel.loadUserDataResult.collect { result ->
                when (result) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        result.data?.let { userModel ->
                            userData = userModel
                            val observerOperationModel = ObserverOperationModel(
                                userId = userModel.id!!,
                                username = userModel.username!!
                            )
                            viewModel.getObservers(userModel.token!!, observerOperationModel)
                        }
                    }

                    is Resource.Error -> {
                        result.error(this@ObserversFragment)
                    }
                }
            }
        }

        viewModel.loadUserData()
    }

    private fun initToolbarBackButton() {
        binding.backSettingsBtn.setOnClickListener { onBackPressed() }
    }

    private fun disableViews() {
        binding.progressBar.visibility = View.VISIBLE
        binding.addObserverFab.isEnabled = false
    }

    private fun enableViews() {
        binding.progressBar.visibility = View.GONE
        binding.addObserverFab.isEnabled = true
    }


    fun noObserverAction() {
        binding.noList.visibility = View.VISIBLE
        enableViews()
    }

    fun loadUserDataFiledAction() {
        binding.noListImg.setImageResource(R.drawable.warning)
        binding.noListTxt.text = getString(R.string.txt_loading_error)
        binding.noList.visibility = View.VISIBLE
        enableViews()
    }
}