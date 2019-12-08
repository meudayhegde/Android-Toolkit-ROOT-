package com.uday.android.toolkit.listeners

import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.text.Html
import android.view.View
import android.view.ViewGroup

import com.github.angads25.filepicker.view.FilePickerDialog

import java.io.File

import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.TextView

import com.uday.android.toolkit.fragments.AndroidImagesFragment
import com.uday.android.toolkit.R
import com.uday.android.toolkit.ui.DialogUtils

class AndImgListListener(private val fragment: AndroidImagesFragment, private val group: RadioGroup) : DialogInterface.OnClickListener {
    override fun onClick(p1: DialogInterface, which: Int) {
        p1.cancel()
        fragment.backupDir = File(fragment.DIR?.absolutePath + "/backups")
        when (group.checkedRadioButtonId) {
            R.id.boot_selection -> {
                fragment.BLOCK = fragment.BOOT
                fragment.BLOCK_NAME = "boot.img"
                fragment.choosen = "Boot Image"
                fragment.backupDir = File(fragment.backupDir!!.absolutePath + "/Boot")
            }
            R.id.recovery_selection -> {
                fragment.BLOCK = fragment.RECOVERY
                fragment.BLOCK_NAME = "recovery.img"
                fragment.choosen = "Recovery Image"
                fragment.backupDir = File(fragment.backupDir!!.absolutePath + "/Recovery")
            }
            R.id.logo_selection -> {
                fragment.BLOCK = fragment.LOGO
                fragment.BLOCK_NAME = "logo.bin"
                fragment.choosen = "Boot Logo"
                fragment.backupDir = File(fragment.backupDir!!.absolutePath + "/Logo")
            }
        }

        when (fragment.mOption) {
            AndroidImagesFragment.SELECTED_BACKUP -> DialogUtils.showConfirmDialog(
                fragment.context,
                "Backup " + fragment.choosen,
                null,
                Html.fromHtml("Do you want to <b>backup</b> current <b>" + fragment.choosen + "</b>..?"),
                "confirm",
                fragment.mListener
            )
            AndroidImagesFragment.SELECTED_RESTORE -> {
                if (!fragment.backupDir!!.exists()) fragment.backupDir!!.mkdirs()
                val list = fragment.backupDir!!.list()
                AlertDialog.Builder(fragment.context)
                    .setTitle("Restore " + fragment.BLOCK_NAME!!)
                    .setAdapter(object : ArrayAdapter<String>(fragment.context,
                        android.R.layout.simple_list_item_1, list!!) {
                        override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
                            var view = view
                            view = super.getView(pos, view, parent)
                            (view as TextView).setSingleLine(false)
                            return view
                        }
                    } , DialogInterface.OnClickListener { p1, p2 ->
                        p1.cancel()
                        fragment.choosen = list!![p2]
                        DialogUtils.showConfirmDialog(
                            fragment.context,
                            "Restore",
                            "are you sure you want to restore " + fragment.choosen,
                            null,
                            "confirm",
                            fragment.mListener
                        )
                    })
                    .show()
                fragment.mOption = AndroidImagesFragment.SELECTED_RESTORE_ITEM
            }

            AndroidImagesFragment.SELECTED_INSTALL -> {
                fragment.properties?.extensions = arrayOf(".bin", ".img")
                fragment.dialog =
                    FilePickerDialog(fragment.context, fragment.properties!!, R.style.AppTheme)
                fragment.dialog?.setTitle("Select an img file to unpack")
                fragment.dialog?.setDialogSelectionListener(fragment.fileListener!!)
                fragment.dialog?.show()
            }
            AndroidImagesFragment.SELECTED_RESTORE_ITEM -> DialogUtils.showConfirmDialog(
                fragment.context,
                "Restore " + fragment.BLOCK_NAME!!,
                null,
                Html.fromHtml("Are you sure you want to <b>restore</b> <br><b>" + fragment.choosen + "</b>..?"),
                "confirm",
                fragment.mListener
            )
        }

    }
}
