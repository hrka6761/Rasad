package ir.srp.rasad.core.errors.network_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.core.utils.MessageViewer.showError
import ir.srp.rasad.core.errors.Error
import javax.inject.Inject

class E400 @Inject constructor() : Error {

    override var errorMessage: String = "Input is incorrect"

    override fun invoke(fragment: Fragment) =
        showError(fragment, errorMessage)
}