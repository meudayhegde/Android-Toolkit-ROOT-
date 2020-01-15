package com.uday.android.toolkit.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R
import com.uday.android.toolkit.fragments.BatchInstallerFragment
import com.uday.android.toolkit.listeners.DeleteClickListener
import com.uday.android.toolkit.listeners.InstallClickListener
import com.uday.android.util.ApkListData
import java.util.*


open class ApkListAdapter(private val fragment: BatchInstallerFragment, private val layoutRes: Int,
                          private val apkList: ArrayList<ApkListData>) : ArrayAdapter<ApkListData>(fragment.context, layoutRes, apkList) {
    private val deleter: DeleteClickListener = DeleteClickListener(this)
    private val installer: InstallClickListener = InstallClickListener(this)

    override fun getItem(position: Int): ApkListData? {
        return apkList[position]
    }

    @SuppressLint("DefaultLocale", "ViewHolder", "InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val row = LayoutInflater.from(context).inflate(layoutRes, parent, false)
        val apkListData = apkList[position]
        val iV = row.findViewById(R.id.apk_icon) as ImageView
        iV.setImageDrawable(apkList[position].ICON)
        var titleText = apkListData.NAME + " " + apkListData.VERSION_NAME
        val txtSearch = apkListData.txtSearch
        if (txtSearch != null && titleText.toLowerCase().contains(txtSearch.toLowerCase())) {
            val length = txtSearch.length
            val start = titleText.toLowerCase().indexOf(txtSearch.toLowerCase())
            var regx = ""
            for (i in 0 until length) {
                regx += titleText[start + i]
            }
            titleText = titleText.replace(regx, "<font color=\"#00AEFF\">$regx</font>")
        }

        val title = row.findViewById(R.id.apk_name) as TextView
        title.setTextColor(apkListData.titleColor)
        title.text = Html.fromHtml(titleText)

        if (!apkListData.isSelectable)
            title.setTextColor(Color.RED)

        var apkName = apkListData.apkFile.name
        if (txtSearch != null && apkName.toLowerCase().contains(txtSearch.toLowerCase())) {
            val length = txtSearch.length
            val start = apkName.toLowerCase().indexOf(txtSearch.toLowerCase())
            var regx = ""
            for (i in 0 until length) {
                regx += apkName[start + i]
            }
            apkName = apkName.replace(regx, "<font color=\"#00AEFF\">$regx</font>")
        }
        (row.findViewById(R.id.apk_path) as TextView).text =
            Html.fromHtml(apkListData.apkFile.parent + "/" + apkName)
        val chbx = row.findViewById(R.id.apk_chbx) as CheckBox

        chbx.isChecked = apkListData.isSelected

        if (apkListData.isSelectable)
            row.setOnClickListener { chbx.toggle() }
        else
            chbx.isEnabled = false

        chbx.setOnCheckedChangeListener { _, p2 ->
            apkList[position].isSelected = p2
            if (p2)
                fragment.onChecked()
            else
                fragment.onUnchecked()
            this@ApkListAdapter.onCheckedChanged()
        }

        val chkVer = row.findViewById(R.id.check_ver) as TextView
        if (apkListData.isOld || apkListData.isInstalled) {
            chkVer.visibility = View.VISIBLE
        }

        row.setOnLongClickListener {
            val twWidth = (context.resources.displayMetrics.widthPixels * 0.48).toInt()
            @SuppressLint("ViewHolder") val layout = (context as AppCompatActivity).layoutInflater.inflate(R.layout.apk_details_layout, null) as TableLayout
            val verName = layout.findViewById(R.id.apk_version_view) as TextView
            verName.width = twWidth
            verName.text = apkListData.VERSION_NAME
            (layout.findViewById(R.id.apk_version_code_view) as TextView).text = apkListData.VERSION_CODE.toString()
            (layout.findViewById(R.id.apk_size_view) as TextView).text = apkListData.SIZE
            val pkgName = layout.findViewById(R.id.apk_package_view) as TextView
            pkgName.width = twWidth
            pkgName.text = apkListData.PACKAGE_NAME
            val fileName = layout.findViewById(R.id.apk_file_view) as TextView
            fileName.width = (context.resources.displayMetrics.widthPixels * 0.45).toInt()
            fileName.text = apkListData.apkFile.name
            val dialog = AlertDialog.Builder(context)
                .setIcon(apkList[position].ICON)
                .setTitle(apkList[position].NAME)
                .setView(layout)
                .setPositiveButton("Install", null)
                .setNegativeButton("Market") { _, _ ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("market://details?id=" + apkListData.PACKAGE_NAME)
                        context.startActivity(intent)

                    } catch (ex: Exception) {
                        Log.e(MainActivity.TAG, ex.toString())
                        CustomToast.showFailureToast(
                            context,
                            "No market is found\n",
                            Toast.LENGTH_SHORT
                        )
                    }
                }
                .setNeutralButton("Delete", null)
                .create()
            dialog.window!!.attributes.windowAnimations = R.style.DialogTheme
            dialog.show()
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                installer.setPosition(position)
                installer.onClick(dialog)
            }
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener {
                deleter.setPosition(position, row)
                deleter.onClick(dialog)
            }
            true
        }

        val State = row.findViewById(R.id.stat_inst) as TextView
        if (apkListData.isInstalled) {
            State.text = "Installed"
            State.setTextColor(Color.rgb(0, 255, 55))

            iV.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle(apkListData.NAME)
                    .setIcon(apkListData.ICON)
                    .setMessage("Do you want to launch " + apkListData.NAME + "..?")
                    .setNegativeButton("cancel", null)
                    .setPositiveButton("Launch") { _, _ ->
                        try {
                            val launchIntent =
                                context.packageManager.getLaunchIntentForPackage(apkListData.PACKAGE_NAME)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)//null pointer check in case package name was not found
                            } else
                                CustomToast.showFailureToast(
                                    context,
                                    "Launch intent not found,\ncould not launch this app.",
                                    Toast.LENGTH_SHORT
                                )
                        } catch (ex: Exception) {
                            CustomToast.showFailureToast(
                                context,
                                "Failed to launch " + apkListData.NAME,
                                Toast.LENGTH_SHORT
                            )
                            Log.e(MainActivity.TAG, ex.toString())
                        }
                    }
                    .show()
            }
            if (apkListData.isInstalledVer) {
                chkVer.text = "Current Installed Version"
                chkVer.setTextColor(Color.rgb(0, 255, 55))
            } else if (!apkListData.isOld) {
                chkVer.text = "New Version"
                chkVer.setTextColor(context.resources.getColor(R.color.colorPrimaryDark))
            }
        } else {
            State.text = "Not Installed"
            State.setTextColor(Color.rgb(255, 15, 0))
        }
        return row


    }

    open fun onCheckedChanged() {}

    override fun notifyDataSetChanged() {
        fragment.onAdapterNotified()
        super.notifyDataSetChanged()
    }

    fun filter(charText_: String) {
        var charText = charText_
        charText = charText.toLowerCase(Locale.getDefault())
        apkList.clear()
        if (charText.isEmpty()) {
            apkList.addAll(BatchInstallerFragment.apkFilesOrig!!)
            for (data in apkList) {
                data.txtSearch = null
            }
        } else {
            for (data in BatchInstallerFragment.apkFilesOrig!!) {
                if ((data.NAME + data.VERSION_NAME + data.apkFile.name).toLowerCase(Locale.getDefault()).contains(
                        charText
                    )
                ) {
                    apkList.add(data)
                    data.txtSearch = charText
                }
            }
        }
        notifyDataSetChanged()
    }

}
