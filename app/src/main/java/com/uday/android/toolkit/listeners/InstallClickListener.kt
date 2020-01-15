package com.uday.android.toolkit.listeners

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R
import com.uday.android.toolkit.ui.ApkListAdapter
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.toolkit.ui.DialogUtils
import com.uday.android.util.ApkListData
import com.uday.android.util.Utils
import eu.chainfire.libsuperuser.Shell

class InstallClickListener(private val adapter: ApkListAdapter) : DialogUtils.OnClickListener {

    private var apkListData: ApkListData? = null
    private var position: Int = 0
    private val context: Context = adapter.context
    private val launcher: DialogUtils.OnClickListener
    private val commandResultListener: Shell.OnCommandResultListener
    private var sweet: AlertDialog? = null
    private var pDialog: ProgressDialog? = null

    init {

        launcher = object : DialogUtils.OnClickListener {
            override fun onClick(p1: AlertDialog?) {
                p1!!.cancel()
                try {
                    val launchIntent =
                        context.packageManager.getLaunchIntentForPackage(apkListData!!.PACKAGE_NAME)
                    if (launchIntent != null) {
                        context.startActivity(launchIntent)//null pointer check in case package name was not found
                    }
                } catch (ex: Exception) {
                    CustomToast.showFailureToast(
                        context,
                        "Failed to launch " + apkListData!!.NAME,
                        Toast.LENGTH_SHORT
                    )
                    Log.e(MainActivity.TAG, ex.toString())
                }

            }
        }

        commandResultListener =
            Shell.OnCommandResultListener { _, exitcode, output ->
                (context as AppCompatActivity).runOnUiThread {
                    pDialog!!.cancel()
                    val dialog: AlertDialog
                    if (exitcode == 0) {
                        apkListData!!.isInstalled = true
                        apkListData!!.titleColor = Color.rgb(0, 202, 0)
                        apkListData!!.isInstalledVer = true
                        dialog = DialogUtils.showConfirmDialog(
                            context,
                            "Installation Success",
                            apkListData!!.NAME + "_" + apkListData!!.VERSION_NAME + " Successfully installed...",
                            null,
                            "Launch",
                            launcher
                        )
                        dialog.setIcon(apkListData!!.ICON)
                        CustomToast.showSuccessToast(
                            context,
                            apkListData!!.NAME + "_" + apkListData!!.VERSION_NAME + " Successfully installed...",
                            Toast.LENGTH_SHORT
                        )
                    } else {
                        Log.e(MainActivity.TAG, Utils.getString(output))
                        apkListData!!.titleColor = Color.rgb(255, 25, 0)
                        apkListData!!.isInstalled = false
                        dialog = DialogUtils.showConfirmDialog(
                            context,
                            "Installation failed",
                            "Failed to install " + apkListData!!.NAME + "_" + apkListData!!.VERSION_NAME,
                            null,
                            null,
                            null
                        )
                        dialog.setIcon(apkListData!!.ICON)
                        CustomToast.showFailureToast(
                            context,
                            "Failed to install " + apkListData!!.NAME + "_" + apkListData!!.VERSION_NAME + "\n" + Utils.getString(
                                output
                            ),
                            Toast.LENGTH_SHORT
                        )
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }

    override fun onClick(p1: AlertDialog?) {
        this.sweet = p1
        p1?.cancel()
        pDialog = ProgressDialog(context)
        pDialog!!.setIcon(apkListData!!.ICON)
        pDialog!!.setTitle("Installing...")
        pDialog!!.setMessage("Installing " + apkListData!!.NAME + " please wait...")
        pDialog!!.setCancelable(false)
        pDialog!!.window!!.attributes.windowAnimations = R.style.DialogTheme
        pDialog!!.show()
        rootSession!!.addCommand(
            "pm install -rd " + '"'.toString() + apkListData!!.PATH + '"'.toString(),
            position,
            commandResultListener
        )

    }

    fun setPosition(position: Int): InstallClickListener {
        this.position = position
        this.apkListData = adapter.getItem(position)
        return this
    }

    companion object {


        private val rootSession = MainActivity.rootSession
    }

}
