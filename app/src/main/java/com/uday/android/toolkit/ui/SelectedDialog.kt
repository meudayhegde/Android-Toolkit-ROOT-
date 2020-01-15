package com.uday.android.toolkit.ui

import android.view.View
import android.view.WindowManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R
import com.uday.android.toolkit.fragments.BatchInstallerFragment
import com.uday.android.util.ApkListData
import java.util.*

class SelectedDialog(fragment: BatchInstallerFragment, adapter: ArrayAdapter<*>) :
    View.OnClickListener {
    private val selectedData: ArrayList<ApkListData> = ArrayList()
    private val selectedAdapter: ApkListAdapter
    private val dialog: AlertDialog

    init {
        selectedAdapter = object : ApkListAdapter(fragment, R.layout.apk_list_item, selectedData) {
            override fun onCheckedChanged() {
                adapter.notifyDataSetChanged()
            }
        }
        dialog = AlertDialog.Builder(fragment.context)
            .setTitle("Selected Apk files")
            .setAdapter(selectedAdapter, null)
            .create()
        dialog.window!!.attributes.windowAnimations = R.style.DialogTheme
    }

    override fun onClick(p1: View) {
        selectedData.clear()
        for (data in BatchInstallerFragment.apkFilesOrig!!) {
            if (data.isSelected) selectedData.add(data)
        }
        selectedAdapter.notifyDataSetChanged()
        dialog.show()
        dialog.window!!.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            (MainActivity.SCREEN_HEIGHT * 0.7).toInt()
        )
    }
}
