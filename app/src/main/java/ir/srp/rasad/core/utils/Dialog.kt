package ir.srp.rasad.core.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import ir.srp.rasad.R

object Dialog {

    fun showSimpleDialog(
        context: Context,
        msg: String,
        negativeAction: (dialog: DialogInterface) -> Unit,
        positiveAction: (dialog: DialogInterface) -> Unit,
    ) {
        AlertDialog.Builder(context).setMessage(msg)
            .setPositiveButton(context.getString(R.string.btn_txt_positive_dialog)) { dialog, _ ->
                positiveAction(dialog)
            }
            .setNegativeButton(context.getString(R.string.btn_txt_negative_dialog)) { dialog, _ ->
                negativeAction(dialog)
            }
            .setCancelable(false)
            .show()
    }
}