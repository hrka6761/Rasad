package ir.srp.rasad.core.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface

object Dialog {

    private var simpleDialog: AlertDialog? = null
    var dialogLabel = ""
        private set

    fun showSimpleDialog(
        label: String = "",
        activity: Activity,
        msg: String,
        negativeButton: String = "Cancel",
        positiveButton: String = "ok",
        negativeAction: (dialog: DialogInterface) -> Unit,
        positiveAction: (dialog: DialogInterface) -> Unit,
    ): AlertDialog? {
        return if (!activity.isFinishing) {
            dialogLabel = label
            simpleDialog = AlertDialog.Builder(activity).setMessage(msg)
                .setPositiveButton(positiveButton) { dialog, _ ->
                    positiveAction(dialog)
                }
                .setNegativeButton(negativeButton) { dialog, _ ->
                    negativeAction(dialog)
                }
                .setCancelable(false)
                .show()
            null
        } else
            simpleDialog
    }

    fun hideSimpleDialog(activity: Activity) {
        if (!activity.isFinishing) {
            simpleDialog?.dismiss()
            simpleDialog = null
            dialogLabel = ""
        }
    }
}