package ir.srp.rasad.core.errors.local_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.core.MessageViewer.showError
import ir.srp.rasad.core.errors.Error
import javax.inject.Inject

class RetrofitError @Inject constructor() : Error {

    override var errorMessage: String = "Network error ..."

    override fun invoke(fragment: Fragment) {
        showError(fragment, errorMessage)
    }
}