package ir.srp.rasad.core.errors.network_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.errors.Error
import javax.inject.Inject

class E409 @Inject constructor() : Error {

    override var errorMessage: String = "One of the entries is duplicate."

    override fun invoke(fragment: Fragment) {
        showError(fragment, errorMessage)
    }
}