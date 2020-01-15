package com.uday.android.toolkit.listeners

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.fragments.KernelFragment
import com.uday.android.toolkit.ui.CustomToast
import eu.chainfire.libsuperuser.Shell

class KernCommandLineListener(private val fragment: KernelFragment) : Shell.OnCommandLineListener {

    @SuppressLint("NewApi")
    override fun onCommandResult(commandcode: Int, exitcode: Int) {
        fragment.runOnUiThread(Runnable {
            fragment.termProgress!!.visibility = View.GONE
            fragment.termDialog!!.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = true
            fragment.termDialog!!.getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
            if (exitcode == 0) {
                when (commandcode) {
                    KernelFragment.SELECTED_BACKUP -> CustomToast.showSuccessToast(
                        fragment.context,
                        "kernel backup completed"
                                + "backups dir: " + fragment.dir + "/backups/Kernel",
                        Toast.LENGTH_SHORT
                    )
                    KernelFragment.SELECTED_RESTORE -> CustomToast.showSuccessToast(
                        fragment.context,
                        "selected kernel has been restored successfully\n" + "reboot the device to activate the kernel",
                        Toast.LENGTH_SHORT
                    )
                    KernelFragment.SELECTED_INSTALL -> CustomToast.showSuccessToast(
                        fragment.context,
                        "Installation completed.." +
                                "reboot the device to change to take effect\n\n" +
                                "Note: device will not boot if you flashed a wrong kernel",
                        Toast.LENGTH_LONG
                    )
                }
            } else {


                CustomToast.showSuccessToast(
                    fragment.context,
                    "Something went wrong.\nplease check log for more info",
                    Toast.LENGTH_LONG
                )
            }
        })
    }

    @SuppressLint("NewApi")
    override fun onLine(line: String) {
        (fragment.context as AppCompatActivity).runOnUiThread { appendLineToOutput(line) }
    }


    private fun appendLineToOutput(line: String) {
        val sb = StringBuilder().append(line).append(10.toChar())
        fragment.termTextView!!.append(sb.toString())
    }

}
