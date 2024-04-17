package ir.srp.rasad.core.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

object Dialog {

    private var dialog: AlertDialog? = null
    var dialogLabel = ""
        private set

    fun showSimpleDialog(
        label: String = "",
        context: Context,
        msg: String,
        negativeButton: String = "Cancel",
        positiveButton: String = "ok",
        negativeAction: (dialog: DialogInterface) -> Unit,
        positiveAction: (dialog: DialogInterface) -> Unit,
    ): AlertDialog? {
        dialogLabel = label
        dialog = AlertDialog.Builder(context).setMessage(msg)
            .setPositiveButton(positiveButton) { dialog, _ ->
                positiveAction(dialog)
            }
            .setNegativeButton(negativeButton) { dialog, _ ->
                negativeAction(dialog)
            }
            .setCancelable(false)
            .show()

        return dialog
    }

    fun hideSimpleDialog() {
        dialog?.dismiss()
        dialog = null
        dialogLabel = ""
    }
}