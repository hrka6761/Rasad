package ir.srp.rasad.core.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

object MessageViewer {

    fun showError(fragment: Fragment, message: String, duration: Int = Snackbar.LENGTH_LONG) {
        Snackbar.make(fragment.requireView(), message, duration).show()
    }

    fun showError(context: Context, message: String, duration: Int = Toast.LENGTH_LONG) {
        Toast.makeText(context, message, duration).show()
    }

    fun showWarning(fragment: Fragment, message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        Snackbar.make(fragment.requireView(), message, duration).show()
    }

    fun showWarning(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(context, message, duration).show()
    }
}