package ir.srp.rasad.core.errors.network_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.core.MessageViewer.showWarning
import ir.srp.rasad.core.errors.Error
import ir.srp.rasad.presentation.otp.OtpFragment
import ir.srp.rasad.presentation.otp.OtpFragmentDirections
import javax.inject.Inject

class E404 @Inject constructor() : Error {

    override var errorMessage: String = "Not found ..."

    override fun invoke(fragment: Fragment) {
        if (fragment is OtpFragment) {
            goToRegisterFragment(fragment)
            showWarning(fragment.requireContext(), errorMessage)
        }
    }


    private fun goToRegisterFragment(fragment: OtpFragment) {
        val mobile = fragment.mobile
        val action = OtpFragmentDirections.actionOtpFragmentToRegisterFragment(mobile)
        fragment.navController.navigate(action)
    }
}