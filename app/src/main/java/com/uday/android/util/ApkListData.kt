package com.uday.android.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.Log

import com.uday.android.toolkit.MainActivity

import java.io.File


class ApkListData(
    private val context: Context,
    var apkFile: File,
    private val pm: PackageManager,
    var ICON: Drawable
) {
    var PATH: String
    var NAME: String
    var VERSION_NAME = "Invalid apk file"
    var VERSION_CODE = 0
    var isSelectable = false
    var isSelected = false
    var SIZE: String
    var PACKAGE_NAME = "parse error..!"
    var titleColor = MainActivity.colorOnBackground
    var isInstalled = false
    var isOld = false
    var isInstalledVer = false
    var txtSearch: String? = null
    var SIZE_LONG: Long = 0
    private var pi: PackageInfo? = null
    private var listener: OnAddedListener? = null

    init {
        PATH = apkFile.absolutePath
        NAME = apkFile.name
        SIZE_LONG = apkFile.length()
        SIZE = Utils.getConventionalSize(SIZE_LONG)
    }

    fun add(): ApkListData {
        (context as MainActivity).runInBackground(Runnable {
            try {
                pi = pm.getPackageArchiveInfo(PATH, 0)
                pi!!.applicationInfo.sourceDir = PATH
                pi!!.applicationInfo.publicSourceDir = PATH
                NAME = pi!!.applicationInfo.loadLabel(pm).toString()
                VERSION_NAME = pi!!.versionName
                VERSION_CODE = pi!!.versionCode
                ICON = pi!!.applicationInfo.loadIcon(pm)
                PACKAGE_NAME = pi!!.packageName
                if (PACKAGE_NAME != "com.uday.android.toolkit")
                    isSelectable = true
                val info = pm.getApplicationInfo(
                    PACKAGE_NAME, PackageManager.GET_UNINSTALLED_PACKAGES
                )
                isInstalled = true
                Log.d(MainActivity.TAG, "checkAppInstalledByName: $PACKAGE_NAME : found")
                val pInfo = pm.getPackageInfo(PACKAGE_NAME, 0)
                if (pInfo.versionCode == VERSION_CODE) {
                    isInstalledVer = true
                } else if (pInfo.versionCode > VERSION_CODE) isOld = true

            } catch (ex: Exception) {
                //	Log.d(MainActivity.TAG,ex.toString());
                //	Log.d(MainActivity.TAG,  "checkAppInstalledByName:"+PACKAGE_NAME+" not found");
                isInstalled = false
            }

            if (listener != null) {
                context.runOnUiThread { listener!!.onAdded() }
            }
        })
        return this
    }

    fun setOnAddedListener(listener: OnAddedListener): ApkListData {
        this.listener = listener
        return this
    }


    interface OnAddedListener {
        fun onAdded()
    }
}
