package ir.srp.rasad.presentation.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import ir.srp.rasad.R
import ir.srp.rasad.core.Constants.EDIT_PROFILE_KEY
import ir.srp.rasad.core.Constants.USERNAME_ARG_VALUE
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.utils.Validation.isUsernameValid
import ir.srp.rasad.core.utils.Validation.isEmailValid
import ir.srp.rasad.databinding.EditProfileBottomSheetLayoutBinding

class EditProfileBottomSheet(
    private val listener: EditCLickListener,
) : BottomSheetDialogFragment() {

    private lateinit var binding: EditProfileBottomSheetLayoutBinding
    private lateinit var args: Bundle


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = requireArguments()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = EditProfileBottomSheetLayoutBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }


    private fun initialize() {
        initDescription()
        initEditText()
        initButton()
    }

    private fun initDescription() {
        binding.editDescTxt.text = getString(
            R.string.txt_edit_profile_bottom_sheet_desc,
            args.getString(EDIT_PROFILE_KEY)
        )
    }

    private fun initEditText() {
        binding.fieldEdtl.hint = args.getString(EDIT_PROFILE_KEY)
    }

    @SuppressLint("StringFormatInvalid")
    private fun initButton() {
        binding.updateBtn.text =
            getString(R.string.btn_txt_update, args.getString(EDIT_PROFILE_KEY))

        binding.updateBtn.setOnClickListener { onCLickUpdate() }
    }

    private fun onCLickUpdate() {
        val fieldText = binding.fieldEdt.text.toString()

        if (fieldText.isEmpty()) {
            showError(
                this,
                getString(R.string.snackbar_empty_field, args.getString(EDIT_PROFILE_KEY))
            )
            return
        }

        if (args.getString(EDIT_PROFILE_KEY) == USERNAME_ARG_VALUE) {
            if (isUsernameValid(fieldText)) {
                binding.fieldEdt.text?.clear()
                listener.onEditUsername(fieldText)
            } else
                showError(this, getString(R.string.snackbar_invalid_username))
        } else {
            if (isEmailValid(fieldText)) {
                binding.fieldEdt.text?.clear()
                listener.onEditEmail(fieldText)
            } else
                showError(this, getString(R.string.snackbar_invalid_email))
        }
    }
}