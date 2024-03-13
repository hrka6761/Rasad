package ir.srp.rasad.core.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

object MessageViewer {

    fun showError(fragment: Fragment, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        val view = fragment.view
        view?.let { Snackbar.make(it, message, duration).show() }
    }

    fun showError(context: Context?, message: String, duration: Int = Toast.LENGTH_LONG) {
        context?.let { Toast.makeText(it, message, duration).show() }
    }

    fun showWarning(fragment: Fragment, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        val view = fragment.view
        view?.let { Snackbar.make(it, message, duration).show() }
    }

    fun showWarning(context: Context?, message: String, duration: Int = Toast.LENGTH_SHORT) {
        context?.let { Toast.makeText(it, message, duration).show() }
    }

    fun showMessage(fragment: Fragment, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        val view = fragment.view
        view?.let { Snackbar.make(it, message, duration).show() }
    }

    fun showMessage(context: Context?, message: String, duration: Int = Toast.LENGTH_SHORT) {
        context?.let { Toast.makeText(it, message, duration).show() }
    }
}