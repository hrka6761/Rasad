package ir.srp.rasad.core.errors.network_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.core.utils.MessageViewer.showWarning
import ir.srp.rasad.core.errors.Error
import ir.srp.rasad.presentation.observers.ObserversFragment
import ir.srp.rasad.presentation.otp.OtpFragment
import ir.srp.rasad.presentation.otp.OtpFragmentDirections
import javax.inject.Inject

class E404 @Inject constructor() : Error {

    override var errorMessage: String = "Not found ..."

    override fun invoke(fragment: Fragment) {
        when (fragment) {
            is OtpFragment -> {
                goToRegisterFragment(fragment)
                showWarning(fragment.requireContext(), errorMessage)
            }

            is ObserversFragment -> {
                showEmptyList(fragment)
            }
        }
    }


    private fun goToRegisterFragment(fragment: OtpFragment) {
        val mobile = fragment.mobile
        val action = OtpFragmentDirections.actionOtpFragmentToRegisterFragment(mobile)
        fragment.navController.navigate(action)
    }

    private fun showEmptyList(fragment: ObserversFragment) {
        fragment.error404Action()
    }
}