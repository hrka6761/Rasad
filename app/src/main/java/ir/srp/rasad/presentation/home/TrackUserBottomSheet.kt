package ir.srp.rasad.presentation.home

import android.annotation.SuppressLint
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import ir.srp.rasad.R
import ir.srp.rasad.core.Constants.LOCATION_PERMISSION_TYPE_CHANGES
import ir.srp.rasad.core.Constants.LOCATION_PERMISSION_TYPE_EVERY_1_D
import ir.srp.rasad.core.Constants.LOCATION_PERMISSION_TYPE_EVERY_1_H
import ir.srp.rasad.core.Constants.LOCATION_PERMISSION_TYPE_EVERY_30_M
import ir.srp.rasad.core.Constants.LOCATION_PERMISSION_TYPE_EVERY_3_H
import ir.srp.rasad.core.Constants.LOCATION_PERMISSION_TYPE_EVERY_5_M
import ir.srp.rasad.core.Constants.TARGETS_KEY
import ir.srp.rasad.core.utils.Dialog.showSimpleDialog
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.utils.TargetPermissionType
import ir.srp.rasad.databinding.TrackUserBottomSheetLayoutBinding
import ir.srp.rasad.domain.models.TargetModel
import ir.srp.rasad.domain.models.TargetPermissionsModel

@Suppress("UNCHECKED_CAST", "DEPRECATION")
class TrackUserBottomSheet(
    private val listener: RequestTargetListener,
) : BottomSheetDialogFragment() {

    private lateinit var binding: TrackUserBottomSheetLayoutBinding
    private lateinit var savedTargets: Array<TargetModel>
    private var savedTargetsSize: Int = 0
    private var locationSendingInterval: Int = -1
    private lateinit var selectedChips: MutableList<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = if (SDK_INT >= TIRAMISU)
            requireArguments().getParcelableArray(TARGETS_KEY, TargetModel::class.java)
        else
            requireArguments().getParcelableArray(TARGETS_KEY)

        savedTargets = args as Array<TargetModel>
        savedTargetsSize = savedTargets.size
        selectedChips = mutableListOf()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = TrackUserBottomSheetLayoutBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initialize()
    }


    private fun initialize() {
        initSheet()
        initSpinner()
        initRequestBottom()
        initNewTargetButton()
    }

    private fun initSheet() {
        if (savedTargets.isEmpty()) {
            binding.newTargetContainer.visibility = View.VISIBLE
            binding.allTargetContainer.visibility = View.GONE
            binding.newTargetBtn.visibility = View.GONE
        } else {
            binding.newTargetContainer.visibility = View.GONE
            binding.allTargetContainer.visibility = View.VISIBLE
            binding.newTargetBtn.visibility = View.VISIBLE
            showTargetsChip()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showTargetsChip() {
        for (target in savedTargets) {
            val chip = Chip(requireContext())
            chip.text = target.name
            chip.isCloseIconVisible = true
            chip.isCheckedIconVisible = true
            chip.isChecked = false
            chip.isCheckable = true
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    if (selectedChips.size < 1) {
                        chip.isCloseIconVisible = false
                        chip.isCheckedIconVisible = true
                        selectedChips.add(chip.text.toString())
                    } else {
                        chip.isCheckedIconVisible = false
                        chip.isChecked = false
                        showError(
                            this,
                            "You can choose one target only."
                        )
                    }
                } else {
                    chip.isCloseIconVisible = true
                    chip.isCheckedIconVisible = false
                    selectedChips.remove(chip.text.toString())
                }
            }
            chip.setOnCloseIconClickListener {
                showSimpleDialog(
                    context = requireContext(),
                    msg = getString(R.string.dialog_remove_saved_target_msg, chip.text),
                    negativeAction = {},
                    positiveAction = {
                        listener.onRemoveTarget(chip.text.toString())
                        binding.targetsChips.removeView(chip)
                        savedTargetsSize--

                        if (savedTargetsSize == 0) {
                            binding.newTargetContainer.visibility = View.VISIBLE
                            binding.allTargetContainer.visibility = View.GONE
                            binding.newTargetBtn.visibility = View.GONE
                        }
                    }
                )
            }
            binding.targetsChips.addView(chip)
        }
    }

    private fun initSpinner() {
        binding.permissionTypes.onItemSelectedListener = SpinnerListener()
    }

    private fun initRequestBottom() {
        binding.requestBtn.setOnClickListener { onClickRequest() }
    }

    private fun onClickRequest() {
        if (binding.newTargetContainer.visibility == View.VISIBLE) {
            if (binding.targetEdt.text.toString().isEmpty()) {
                showError(this, getString(R.string.snackbar_empty_target))
                return
            }

            if (locationSendingInterval == -1) {
                showError(this, getString(R.string.snackbar_not_selected_location_interval))
                return
            }

            val permission = TargetPermissionsModel(
                TargetPermissionType.Location,
                locationSendingInterval
            )

            val name = binding.nameEdt.text.toString().ifEmpty { binding.targetEdt.text.toString() }
                .replace(" ", "")
            val username = binding.targetEdt.text.toString()
                .replace(" ", "")

            val target = TargetModel(
                name,
                username,
                R.drawable.marker10,
                permission
            )

            listener.onRequest(true, target)
        } else
            if (selectedChips.isNotEmpty()) {
                val targets = mutableListOf<TargetModel>()
                for (chipName in selectedChips) {
                    for (target in savedTargets) {
                        if (target.name == chipName) {
                            targets.add(target)
                            break
                        }
                    }
                }
                listener.onRequest(false, *targets.toTypedArray())
            } else
                showError(
                    this,
                    getString(R.string.snackbar_not_selected_targets)
                )

        binding.nameEdt.text?.clear()
        binding.targetEdt.text?.clear()
        binding.permissionTypes.setSelection(0)
    }

    private fun initNewTargetButton() {
        binding.newTargetBtn.setOnClickListener { onClickNewTarget() }
    }

    private fun onClickNewTarget() {
        binding.newTargetContainer.visibility = View.VISIBLE
        binding.allTargetContainer.visibility = View.GONE
        binding.newTargetBtn.visibility = View.GONE
    }


    private inner class SpinnerListener : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long,
        ) {
            locationSendingInterval = when (position) {
                0 -> -1
                1 -> LOCATION_PERMISSION_TYPE_CHANGES
                2 -> LOCATION_PERMISSION_TYPE_EVERY_5_M
                3 -> LOCATION_PERMISSION_TYPE_EVERY_30_M
                4 -> LOCATION_PERMISSION_TYPE_EVERY_1_H
                5 -> LOCATION_PERMISSION_TYPE_EVERY_3_H
                6 -> LOCATION_PERMISSION_TYPE_EVERY_1_D
                else -> -1
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            // Not yet implemented
        }
    }
}