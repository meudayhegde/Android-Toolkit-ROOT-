package com.uday.android.toolkit.listeners

import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.uday.android.toolkit.R
import com.uday.android.toolkit.fragments.AndroidImagesFragment
import com.uday.android.toolkit.ui.DialogUtils
import java.io.File

class AndImgListListener(private val fragment: AndroidImagesFragment, private val group: RadioGroup) : DialogInterface.OnClickListener {
    override fun onClick(p1: DialogInterface, which: Int) {
        p1.cancel()
        fragment.backupDir = File(fragment.dir?.absolutePath + "/backups")
        when (group.checkedRadioButtonId) {
            R.id.boot_selection -> {
                fragment.block = fragment.boot
                fragment.blockName = "boot.img"
                fragment.choosen = "Boot Image"
                fragment.backupDir = File(fragment.backupDir!!.absolutePath + "/Boot")
            }
            R.id.recovery_selection -> {
                fragment.block = fragment.recovery
                fragment.blockName = "recovery.img"
                fragment.choosen = "Recovery Image"
                fragment.backupDir = File(fragment.backupDir!!.absolutePath + "/Recovery")
            }
            R.id.logo_selection -> {
                fragment.block = fragment.logo
                fragment.blockName = "logo.bin"
                fragment.choosen = "Boot Logo"
                fragment.backupDir = File(fragment.backupDir!!.absolutePath + "/Logo")
            }
        }

        @Suppress("DEPRECATION")
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
                    .setTitle("Restore " + fragment.blockName!!)
                    .setAdapter(object : ArrayAdapter<String>(fragment.context,
                        android.R.layout.simple_list_item_1, list!!) {
                        override fun getView(pos: Int, view_: View?, parent: ViewGroup): View {
                            var view = view_
                            view = super.getView(pos, view, parent)
                            (view as TextView).setSingleLine(false)
                            return view
                        }
                    }) { p3, p2 ->
                        p3.cancel()
                        fragment.choosen = list[p2]
                        DialogUtils.showConfirmDialog(fragment.context, "Restore", "are you sure you want to restore " + fragment.choosen, null, "confirm", fragment.mListener)
                    }
                    .show()
                fragment.mOption = AndroidImagesFragment.SELECTED_RESTORE_ITEM
            }

            AndroidImagesFragment.SELECTED_INSTALL -> {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "application/octet-stream"
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                try {
                    fragment.startActivityForResult(
                        Intent.createChooser(intent, "Select an img or bin file to install"),
                        AndroidImagesFragment.IMAGE_SELECT_CODE
                    )
                } catch (ex: ActivityNotFoundException) {
                    Toast.makeText(fragment.context, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
                }
            }
            AndroidImagesFragment.SELECTED_RESTORE_ITEM -> DialogUtils.showConfirmDialog(
                fragment.context,
                "Restore " + fragment.blockName!!,
                null,
                Html.fromHtml("Are you sure you want to <b>restore</b> <br><b>" + fragment.choosen + "</b>..?"),
                "confirm",
                fragment.mListener
            )
        }

    }
}
