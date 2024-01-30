package ir.srp.rasad.core.errors.local_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.core.errors.Error
import ir.srp.rasad.core.utils.MessageViewer
import javax.inject.Inject

class LoadLocalDataError @Inject constructor() : Error {

    override var errorMessage: String = "Error occurred when Loading data..."

    override fun invoke(fragment: Fragment) =
        MessageViewer.showError(fragment, errorMessage)
}