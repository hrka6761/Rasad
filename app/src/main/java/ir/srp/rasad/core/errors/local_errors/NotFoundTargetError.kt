package ir.srp.rasad.core.errors.local_errors

import android.os.Bundle
import androidx.fragment.app.Fragment
import ir.srp.rasad.core.Constants
import ir.srp.rasad.core.errors.Error
import ir.srp.rasad.domain.models.ObserverTargetModel
import ir.srp.rasad.presentation.home.HomeFragment
import javax.inject.Inject

class NotFoundTargetError @Inject constructor() : Error {

    override var errorMessage: String = "Not found any target"

    override fun invoke(fragment: Fragment) {
        val bottomSheet = (fragment as HomeFragment).trackUserBottomSheet
        val args = Bundle()
        val targets = arrayOf<ObserverTargetModel>()
        args.putParcelableArray(Constants.SAVED_TARGETS_KEY, targets)
        bottomSheet.arguments = args
        bottomSheet.show(fragment.requireActivity().supportFragmentManager, bottomSheet.tag)
    }
}