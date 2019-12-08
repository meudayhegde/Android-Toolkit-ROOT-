package com.uday.android.toolkit.runnable


import android.annotation.SuppressLint
import com.uday.android.toolkit.*
import com.uday.android.toolkit.fragments.*
import java.io.*

class KernelAction(private val fragment: KernelFragment) : Runnable {


    @SuppressLint("NewApi")
    override fun run() {
        when (fragment.mOption) {
            KernelFragment.SELECTED_BACKUP -> {
                val backup =
                    File(fragment.DIR!!.absolutePath + "/backups/Kernel/" + fragment.kernVersion)
                backup.mkdirs()
                MainActivity.rootSession!!.addCommand(
                    fragment.context.filesDir.toString() + "/common/kern_backup.sh " + MainActivity.TOOL + " " + fragment.BLOCK + " '" + backup.absolutePath + "'",
                    KernelFragment.SELECTED_BACKUP,
                    fragment.commandLineListener
                )
            }
            KernelFragment.SELECTED_RESTORE -> MainActivity.rootSession!!.addCommand(
                fragment.context.filesDir.toString() + "/common/kern_install.sh " + MainActivity.TOOL + " " + fragment.BLOCK + " '" + fragment.selected!!.absolutePath + "/kernel' false",
                KernelFragment.SELECTED_RESTORE,
                fragment.commandLineListener
            )
            KernelFragment.SELECTED_INSTALL -> MainActivity.rootSession!!.addCommand(
                fragment.context.filesDir.toString() + "/common/kern_install.sh " + MainActivity.TOOL + " " + fragment.BLOCK + " " + '"'.toString() + fragment.file!!.absolutePath + '"'.toString() + " " + fragment.isImg,
                KernelFragment.SELECTED_INSTALL,
                fragment.commandLineListener
            )
        }
    }

}
