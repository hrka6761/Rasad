package ir.srp.rasad.core.errors.local_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.core.errors.Error
import ir.srp.rasad.core.utils.MessageViewer
import ir.srp.rasad.presentation.observers.ObserversFragment
import javax.inject.Inject

class LoadLocalDataError @Inject constructor() : Error {

    override var errorMessage: String = "Error occurred when Loading data..."

    override fun invoke(fragment: Fragment) {
        if (fragment is ObserversFragment)
            fragment.loadUserDataFiledAction()
        else
            MessageViewer.showError(fragment, errorMessage)
    }
}