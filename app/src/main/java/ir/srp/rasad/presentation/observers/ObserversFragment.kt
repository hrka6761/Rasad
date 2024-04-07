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
import ir.srp.rasad.core.Constants.ADD_OBSERVER_REQ_TYPE
import ir.srp.rasad.core.Constants.GET_OBSERVER_REQ_TYPE
import ir.srp.rasad.core.Resource
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.databinding.FragmentObserversBinding
import ir.srp.rasad.domain.models.ObserverOperationModel
import ir.srp.rasad.domain.models.PermittedObserversModel
import ir.srp.rasad.domain.models.UserModel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ObserversFragment :
    BaseFragment(),
    ObserversAdapter.ObserverClickListener,
    AddObserverListener {

    private lateinit var binding: FragmentObserversBinding
    private val viewModel: ObserversViewModel by viewModels()
    private lateinit var adapter: ObserversAdapter
    private lateinit var userData: UserModel
    private var clickedObserver: PermittedObserversModel? = null
    private val addObserverBottomSheet = AddObserverBottomSheet(this)
    private var requestType: String = GET_OBSERVER_REQ_TYPE


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

    override fun onClickAddObserver(observer: String) {
        disableViews()
        val observerOperationModel = ObserverOperationModel(
            userData.id!!,
            observer,
            userData.username!!
        )
        viewModel.addObserver(userData.token!!, observerOperationModel)
        requestType = ADD_OBSERVER_REQ_TYPE
        addObserverBottomSheet.dismiss()
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
        error404Action()
    }


    private fun initialize() {
        disableViews()
        initGetObserversResponse()
        initDeleteObserverResponse()
        initAddObserverResponse()
        initLoadUserDataResult()
        initToolbarBackButton()
        initAddObserverButton()
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

    @SuppressLint("NotifyDataSetChanged")
    private fun initGetObserversResponse() {
        lifecycleScope.launch {
            viewModel.observers.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        if (response.data != null) {
                            if (!adapter.isAdapterInitialized) {
                                if (response.data.isNotEmpty()) {
                                    adapter.setList(response.data.toMutableList())
                                    binding.rv.layoutManager = LinearLayoutManager(
                                        requireContext(),
                                        LinearLayoutManager.VERTICAL,
                                        false
                                    )
                                    binding.rv.adapter = adapter
                                }
                            } else {
                                adapter.setList(response.data.toMutableList())
                                adapter.notifyDataSetChanged()
                            }
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

    private fun initAddObserverResponse() {
        lifecycleScope.launch {
            viewModel.addObserverResponse.collect { response ->
                when (response) {
                    is Resource.Initial -> {}
                    is Resource.Loading -> {}
                    is Resource.Success -> {
                        val observerOperationModel = ObserverOperationModel(
                            userData.id!!,
                            userData.username!!
                        )
                        viewModel.getObservers(userData.token!!, observerOperationModel)
                        requestType = GET_OBSERVER_REQ_TYPE
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
                            requestType = GET_OBSERVER_REQ_TYPE
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

    private fun initAddObserverButton() {
        binding.addObserverFab.setOnClickListener { onClickAddObserver() }
    }

    private fun onClickAddObserver() {
        addObserverBottomSheet.show(
            requireActivity().supportFragmentManager,
            addObserverBottomSheet.tag
        )
    }

    private fun disableViews() {
        binding.progressBarContainer.visibility = View.VISIBLE
        binding.addObserverFab.isEnabled = false
    }

    private fun enableViews() {
        binding.progressBarContainer.visibility = View.GONE
        binding.addObserverFab.isEnabled = true
    }


    fun error404Action() {
        if (requestType == GET_OBSERVER_REQ_TYPE)
            binding.noList.visibility = View.VISIBLE
        else
            showError(this, getString(R.string.snackbar_not_found_observer))

        enableViews()
    }

    fun networkErrorAction() {
        showError(this, getString(R.string.txt_network_error))
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