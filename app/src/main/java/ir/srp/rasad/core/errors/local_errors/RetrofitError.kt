package ir.srp.rasad.core.errors.local_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.errors.Error
import ir.srp.rasad.presentation.observers.ObserversFragment
import javax.inject.Inject

class RetrofitError @Inject constructor() : Error {

    override var errorMessage: String = "Network error ..."

    override fun invoke(fragment: Fragment) {
        when (fragment) {
            is ObserversFragment -> fragment.networkErrorAction()
            else -> showError(fragment, errorMessage)
        }
    }
}