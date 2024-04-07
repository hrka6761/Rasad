package ir.srp.rasad.presentation.observers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.srp.rasad.R
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.utils.Validation.checkUsernameValidation
import ir.srp.rasad.databinding.AddObserverBottomSheetLayoutBinding

class AddObserverBottomSheet(
    private val listener: AddObserverListener,
) : BottomSheetDialogFragment() {

    private lateinit var binding: AddObserverBottomSheetLayoutBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = AddObserverBottomSheetLayoutBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }


    private fun initialize() {
        initAddButton()
    }

    private fun initAddButton() {
        binding.addBtn.setOnClickListener { onClickAdd() }
    }

    private fun onClickAdd() {
        val username = binding.usernameEdt.text.toString()
        if (binding.usernameEdt.text.toString().isEmpty()) {
            showError(this, getString(R.string.snackbar_empty_observer))
            return
        }

        if (checkUsernameValidation(username).isEmpty()) {
            listener.onClickAddObserver(username)
            binding.usernameEdt.text?.clear()
        } else
            showError(this, getString(R.string.snackbar_invalid_username))
    }
}