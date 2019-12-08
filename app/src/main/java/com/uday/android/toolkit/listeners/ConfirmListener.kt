package com.uday.android.toolkit.listeners

import com.uday.android.toolkit.fragments.AndroidImagesFragment
import com.uday.android.toolkit.ui.DialogUtils

import androidx.appcompat.app.AlertDialog
import android.app.ProgressDialog
import android.widget.ProgressBar
import android.widget.TextView

class ConfirmListener(private val fragment: AndroidImagesFragment) : DialogUtils.OnClickListener {

    override fun onClick(utils: AlertDialog?) {
        utils?.cancel()
        when (fragment.mOption) {
            AndroidImagesFragment.SELECTED_REPACK -> {
                val obj = DialogUtils.showTermDialog(
                    fragment.context,
                    "Repack img",
                    "Repacking project please wait...",
                    "open folder",
                    "finish"
                )
                fragment.termDialog = obj[0] as AlertDialog
                fragment.termTextView = obj[1] as TextView
                fragment.termProgress = obj[2] as ProgressBar
                Thread(fragment.executer as Runnable).start()
            }
            AndroidImagesFragment.SELECTED_INSTALL -> {
                fragment.pDialog = ProgressDialog.show(fragment.context, "Installing", "Installing selected " + fragment.choosen + "\nplease wait...", false, false)
                Thread(fragment.executer as Runnable).start()
            }
            AndroidImagesFragment.SELECTED_BACKUP -> {
                fragment.pDialog = ProgressDialog.show(fragment.context, "Backup", "Backup process in progress please wait...", false, false)
                Thread(fragment.executer).start()
            }
            AndroidImagesFragment.SELECTED_RESTORE -> {
                fragment.pDialog = ProgressDialog.show(fragment.context, "Restoring", "Restoring " + fragment.BLOCK_NAME!!.toUpperCase() + ".\nplease wait...", false, false)
                Thread(fragment.executer).start()
            }
            AndroidImagesFragment.SELECTED_RESTORE_ITEM -> {
                fragment.pDialog = ProgressDialog.show(fragment.context, "Restoring", "Restoring " + fragment.BLOCK_NAME!!.toUpperCase() + ".\nplease wait...", false, false)
                Thread(fragment.executer).start()
            }
        }
    }
}
