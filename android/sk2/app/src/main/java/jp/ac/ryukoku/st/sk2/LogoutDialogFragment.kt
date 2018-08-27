package jp.ac.ryukoku.st.sk2

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import jp.ac.ryukoku.st.sk2.MainActivity.Companion.sk2
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGOUT_DIALOG_CANCEL
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGOUT_DIALOG_MSG
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGOUT_DIALOG_OK
import jp.ac.ryukoku.st.sk2.Sk2Globals.Companion.LOGOUT_DIALOG_TITLE

/** ////////////////////////////////////////////////////////////////////////////// **/
/** Logout Alert Dialog **/
class LogoutDialog : DialogFragment() {
    /** YES **/
    private var onOkClickListener : DialogInterface.OnClickListener?
            = DialogInterface.OnClickListener { _, _ ->
        sk2.logout()
    }
    /** CANCEL **/
    private var onCancelClickListener : DialogInterface.OnClickListener?
            = DialogInterface.OnClickListener { _, _ -> }

    /** ダイアログを作成 **/
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the Builder class for convenient dialog construction
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(LOGOUT_DIALOG_TITLE)
                .setMessage(LOGOUT_DIALOG_MSG)
                .setPositiveButton(LOGOUT_DIALOG_OK, onOkClickListener)
                .setNegativeButton(LOGOUT_DIALOG_CANCEL, onCancelClickListener)
        // Create the AlertDialog object and return it
        return builder.create()
    }
    /** onPause でダイアログを閉じる **/
    override fun onPause() {
        super.onPause()
        dismiss()
    }
}