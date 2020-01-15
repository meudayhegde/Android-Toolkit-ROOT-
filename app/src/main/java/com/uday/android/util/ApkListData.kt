package com.uday.android.util

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
    var path: String = apkFile.absolutePath
    var name: String
    var versionName = "Invalid apk file"
    var versionCode = 0
    var isSelectable = false
    var isSelected = false
    var size: String
    var packageName = "parse error..!"
    var titleColor = MainActivity.colorOnBackground
    var isInstalled = false
    var isOld = false
    var isInstalledVer = false
    var txtSearch: String? = null
    var sizeLong: Long = 0
    private var pi: PackageInfo? = null
    private var listener: OnAddedListener? = null

    init {
        name = apkFile.name
        sizeLong = apkFile.length()
        size = Utils.getConventionalSize(sizeLong)
    }

    fun add(): ApkListData {
        (context as MainActivity).runInBackground(Runnable {
            try {
                pi = pm.getPackageArchiveInfo(path, 0)
                pi!!.applicationInfo.sourceDir = path
                pi!!.applicationInfo.publicSourceDir = path
                name = pi!!.applicationInfo.loadLabel(pm).toString()
                versionName = pi!!.versionName
                @Suppress("DEPRECATION")
                versionCode = pi!!.versionCode
                ICON = pi!!.applicationInfo.loadIcon(pm)
                packageName = pi!!.packageName
                if (packageName != "com.uday.android.toolkit")
                    isSelectable = true
//                val info = pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES)
                isInstalled = true
                Log.d(MainActivity.TAG, "checkAppInstalledByName: $packageName : found")
                val pInfo = pm.getPackageInfo(packageName, 0)
                @Suppress("DEPRECATION")
                if (pInfo.versionCode == versionCode) {
                    isInstalledVer = true
                } else if (pInfo.versionCode > versionCode) isOld = true

            } catch (ex: Exception) {
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
