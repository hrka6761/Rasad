package ir.srp.rasad.core.errors.local_errors

import androidx.fragment.app.Fragment
import ir.srp.rasad.core.errors.Error

class NotFoundTargetError : Error {

    override var errorMessage: String = "Not found any target"

    override fun invoke(fragment: Fragment) {

    }
}