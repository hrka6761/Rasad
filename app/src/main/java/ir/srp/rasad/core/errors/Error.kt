package ir.srp.rasad.core.errors

import androidx.fragment.app.Fragment

interface Error {

    var errorMessage: String

    operator fun invoke(fragment: Fragment)
}