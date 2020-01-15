package com.uday.android.toolkit.listeners

import android.content.DialogInterface
import android.os.Environment
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.R
import com.uday.android.toolkit.fragments.AndroidImagesFragment
import com.uday.android.util.Utils
import eu.chainfire.libsuperuser.Shell

class TermListener(private val fragment: AndroidImagesFragment) : Shell.OnCommandLineListener {

    override fun onCommandResult(commandcode: Int, exitcode: Int) {
        (fragment.context as AppCompatActivity).runOnUiThread {
            fragment.termProgress?.visibility = View.GONE
            fragment.termDialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.visibility = View.VISIBLE
            fragment.termDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.isEnabled = true
            fragment.refreshList()
            if (exitcode == 0) {
                fragment.termDialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
                    fragment.termDialog?.cancel()
                    when (fragment.mOption) {
                        AndroidImagesFragment.SELECTED_UNPACK -> Utils.openFolder(
                            fragment.context,
                            Environment.getDataDirectory().absolutePath + "/local/ToolKit/" + fragment.pName
                        )
                        AndroidImagesFragment.SELECTED_REPACK -> Utils.openFolder(
                            fragment.context,
                            Environment.getDataDirectory().absolutePath + "/local/ToolKit/" + fragment.choosen
                        )
                    }
                }
            } else {
                fragment.termTextView?.append("\nSomething went wrong,\nOperation failed...!!")
                fragment.termDialog?.getButton(DialogInterface.BUTTON_NEGATIVE)?.visibility = View.GONE
                fragment.termDialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.text = fragment.getString(R.string.finish)
            }
        }
    }

    override fun onLine(line: String) {
        (fragment.context as AppCompatActivity).runOnUiThread { appendLineToOutput(line) }
    }

    private fun appendLineToOutput(line: String) {
        val sb = StringBuilder().append(line).append(10.toChar())
        fragment.termTextView?.append(sb.toString())
    }

}
