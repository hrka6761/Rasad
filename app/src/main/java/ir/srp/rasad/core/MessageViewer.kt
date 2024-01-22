package ir.srp.rasad.core

import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

object MessageViewer {

    fun showError(fragment: Fragment, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(fragment.requireView(), message, duration).show()
    }
}