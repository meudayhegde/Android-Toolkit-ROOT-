package com.uday.android.toolkit.listeners

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast

import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.ui.ApkListAdapter
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.toolkit.ui.DialogUtils

import eu.chainfire.libsuperuser.Shell

import com.uday.android.toolkit.fragments.*
import com.uday.android.util.ApkListData

class DeleteClickListener(private val adapter: ApkListAdapter) : DialogUtils.OnClickListener {

    private var apkListData: ApkListData? = null
    private var position: Int = 0
    private val context: Context
    private val anim: Animation
    private val commandResultListener: Shell.OnCommandResultListener
    private var row: View? = null

    init {
        this.context = adapter.context
        anim = AnimationUtils.loadAnimation(
            context, android.R.anim.slide_out_right
        )
        anim.duration = 500

        commandResultListener =
            Shell.OnCommandResultListener { commandCode, exitCode, output ->
                (context as AppCompatActivity).runOnUiThread {
                    if (!apkListData!!.apkFile.exists()) {
                        row!!.startAnimation(anim)
                        Handler().postDelayed({
                            adapter.remove(apkListData)
                            BatchInstallerFragment.apkFilesOrig!!.remove(apkListData!!)
                        }, 400)
                        CustomToast.showSuccessToast(
                            context,
                            apkListData!!.PATH + " is deleted",
                            Toast.LENGTH_SHORT
                        )
                    } else
                        CustomToast.showFailureToast(
                            context,
                            "failed to delete" + apkListData!!.PATH,
                            Toast.LENGTH_SHORT
                        )
                }
            }
    }

    override fun onClick(sweet: AlertDialog?) {
        sweet?.cancel()
        val dialog = DialogUtils.showConfirmDialog(
            context,
            "Are you sure..?",
            "do you want to permanently delete\n" + apkListData!!.PATH + "..?",
            null,
            "confirm",
            object : DialogUtils.OnClickListener {
                override fun onClick(p1: AlertDialog?) {
                    rootSession!!.addCommand(
                        MainActivity.TOOL + " rm -f " + '"'.toString() + apkListData!!.PATH + '"'.toString(),
                        position,
                        commandResultListener
                    )
                    p1!!.cancel()
                }
            })
        dialog.setIcon(apkListData!!.ICON)
    }

    fun setPosition(position: Int, row: View): DeleteClickListener {
        this.position = position
        this.apkListData = adapter.getItem(position)
        this.row = row
        return this
    }

    companion object {

        private val rootSession = MainActivity.rootSession
    }
}
