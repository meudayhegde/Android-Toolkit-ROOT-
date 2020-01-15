
package com.uday.android.toolkit.ui

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.text.Html
import android.util.Log
import android.view.Window
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R
import com.uday.android.util.Utils
import eu.chainfire.libsuperuser.Shell
import java.io.File

open class EnvSetup @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
constructor(private val context:Context) {
    private val fullScreenDialog:Dialog = Dialog(context, android.R.style.Theme_Material_Light_NoActionBar)
    private val pDialog:ProgressDialog
    private var prefs:SharedPreferences? = null
    private var edit:SharedPreferences.Editor? = null
    private val isFirstRun:Boolean
    private var abi:String? = null
    private var hasBusyBox:Boolean = false
    private var currentVersionCode:Int = 0

    fun requestPermissions(permissions:Array<String>, requestCode:Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            (context as AppCompatActivity).requestPermissions(permissions, requestCode)
    }

    fun finishActivity() {
        (context as AppCompatActivity).finish()
    }

    private fun runOnMainThread(action:Runnable) {
        (context as AppCompatActivity).runOnUiThread(action)
    }

    init{
        fullScreenDialog.setCancelable(false)
        fullScreenDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        fullScreenDialog.show()

        pDialog = ProgressDialog(context)
        pDialog.setTitle("Please wait...")
        pDialog.setCancelable(false)
        pDialog.setMessage("Obtaining root access...")
        pDialog.window!!.attributes.windowAnimations = R.style.DialogTheme

        prefs = context.getSharedPreferences("general", 0)
        edit = prefs!!.edit()
        isFirstRun = prefs!!.getBoolean("isFirstRun", true)
        abi = prefs!!.getString("abi", "")
        val previousVersionCode = prefs!!.getInt("versionCode", 1)
        currentVersionCode = try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (ex:PackageManager.NameNotFoundException) {
            previousVersionCode + 1
        }

        when {
            isFirstRun -> showAgreement()
            currentVersionCode > previousVersionCode -> {
                pDialog.show()
                FirstRunSettup()
            }
            else -> {
                pDialog.show()
                object:Thread() {
                    override fun run() {
                        obtainRootShell()
                    }
                }.start()
            }
        }

    }

    private fun clearData() {
        try {
            for (file in File(context.filesDir.absolutePath + "/common/").listFiles()!!)
                if (file.exists() && !file.isDirectory)
                    file.delete()
        }
        catch (ex:Exception) {
            Log.d(MainActivity.TAG, "Fresh installation")
        }

    }

    private fun showAgreement() {
        val agreeTxt = TextView(context)
        val agreementLayout = ScrollView(context)
        agreementLayout.addView(agreeTxt)
        agreeTxt.text = Html.fromHtml(Utils.getStringFromInputStream(context.resources.openRawResource(R.raw.agreement)))
        agreeTxt.setPadding(30, 0, 15, 0)
        val agreementDialog = AlertDialog.Builder(context)
            .setPositiveButton("agree") { _, _ ->
                if (!pDialog.isShowing) pDialog.show()
                object:Thread() {
                    override fun run() {
                        FirstRunSettup()
                    }
                }.start()
            }
            .setNegativeButton("exit") { _, _ -> finishActivity() }
            .setTitle("Terms & Conditions")
            .setView(agreementLayout)
            .show()
         //	agreementDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,(int)(MainActivity.SCREEN_HEIGHT*0.75));
        	agreementDialog.setCancelable(false)
    }

    private fun FirstRunSettup() {
        runOnMainThread(Runnable { pDialog.setMessage("Getting device properties...") })
        val SUPPORTED_ABIS:Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) SUPPORTED_ABIS = Build.SUPPORTED_ABIS
        else SUPPORTED_ABIS = arrayOf(Build.CPU_ABI, Build.CPU_ABI2)
        for (test in SUPPORTED_ABIS) {
            when (test) {
                "armeabi" -> abi = "arm"
                "armeabi-v7a" -> abi = "arm"
                "armeabi-v7a-hard" -> abi = "arm"
                "arm64-v8a" -> abi = "arm64"
                "x86_64" -> abi = "x64"
                "x86" -> abi = "x86"
            }
            if (abi != null && !abi!!.equals("", ignoreCase = true)) break
        }
        if (abi!!.equals("", ignoreCase = true)) {
            CustomToast.showFailureToast(context, "Unsupported Architecture...", Toast.LENGTH_LONG)
            finishActivity()
        }

        edit!!.putString("abi", abi)

        clearData()
        Utils.copyAsset(context.assets, "utils.tar.xz", context.filesDir.absolutePath)
        Utils.unpackXZ(File(context.filesDir.absolutePath + "/utils.tar.xz"), false)
        edit!!.putInt("versionCode", currentVersionCode)
        edit!!.apply()
        if (!isFirstRun)
            CustomToast.showSuccessToast(context, "Application updated", Toast.LENGTH_SHORT)
        obtainRootShell()
    }

    private fun error(title:String, message:String, btn_:String?, listener:DialogInterface.OnClickListener?) {
        var btn = btn_
        pDialog.cancel()
        val exit = DialogInterface.OnClickListener { _, _ ->
            MainActivity.rootSession = null
            finishActivity()
        }
        if (btn == null) btn = "exit"
        val errorBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
        if (listener == null) errorBuilder.setPositiveButton(btn, exit)
        else errorBuilder.setPositiveButton(btn, listener).setNegativeButton("exit", exit)
        errorBuilder.setIcon(R.drawable.ic_error)
        val error = errorBuilder.create()
        error.setCancelable(false)
        error.window!!.attributes.windowAnimations = R.style.DialogTheme
        error.show()
    }

    private fun saveBlockdevs() {
        val listener = Shell.OnCommandResultListener { _, exitCode, output ->
                if (exitCode < 0) Log.e(MainActivity.TAG, "Root access failed" + Utils.getString(output))
                else {
                    Log.i(MainActivity.TAG, Utils.getString(output))
                    prefs = context.getSharedPreferences("block_devs", 0)
                    edit = prefs!!.edit()
                    try {
                        for (Str in output) {
                            edit!!.putString(Str.split((" ").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0], Str.split((" ").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
                            edit!!.apply()
                        }
                    } catch (ex:Exception) {
                        runOnMainThread(Runnable { Toast.makeText(context, "Failed to detect block devices\nPlease consider manually setting block devices.", Toast.LENGTH_SHORT).show() })
                    }

                    NormalStartup()
                }
            }
        try {
            pDialog.setMessage("Enumerating partitions...")
            MainActivity.rootSession!!.addCommand("cd " + context.filesDir.absolutePath + "\n" +
                    MainActivity.TOOL + " tar -xvf utils.tar\n" +
                    MainActivity.TOOL + " ls | while read file ; do\n" +
                    "if [ \$file != " + abi + " ] && [ \$file != common ]; then\n" +
                    MainActivity.TOOL + " rm -rf \$file\n" +
                    "fi\n" +
                    "done\n" +
                    MainActivity.TOOL + " mv " + abi + " bin\n" +
                    MainActivity.TOOL + " mv common/parted bin/parted\n" +
                    MainActivity.TOOL + " chmod 755 bin/* common/*")
            MainActivity.rootSession!!.addCommand(context.filesDir.absolutePath + "/common/find_blockdev.sh " + MainActivity.TOOL, 0, listener)
        }
        catch (ex:Exception) {
            Log.e(MainActivity.TAG, ex.toString())
        }
    }

    private fun obtainRootShell() {
        if (MainActivity.rootSession == null) {
            runOnMainThread(Runnable { pDialog.setMessage("Obtaining root access...") })
            MainActivity.rootSession = Shell.Builder().useSU().setWantSTDERR(true).setWatchdogTimeout(0).setMinimalLogging(false).open { _, exitCode, output ->
                if (exitCode != 0) {
                    Log.e(MainActivity.TAG, "error obtaining root shell $exitCode")
                    MainActivity.rootSession = null
                    runOnMainThread(Runnable {
                        try {
                            if (!Utils.findInPath("su")) {
                                error("Oops...", "Root not found...!!\n", null, null)
                                CustomToast.showFailureToast(context, "Root not found...!!", Toast.LENGTH_SHORT)
                            } else {
                                error("Oops...", "Root access Denied...!!\nRoot access is essential for this app to work", null, null)
                                CustomToast.showFailureToast(context, Utils.getString(output), Toast.LENGTH_SHORT)
                            }
                        } catch (ex:Exception) {
                            Log.e(MainActivity.TAG, ex.toString())
                            error("Oops...", "Root not found...!!\n", null, null)
                            CustomToast.showFailureToast(context, "Root not found...!!", Toast.LENGTH_SHORT)
                        }
                    })
                } else {
                    Log.i(MainActivity.TAG, "Root shell successfully obtained $exitCode")
                    CustomToast.showSuccessToast(context, "Root shell successfully obtained ", Toast.LENGTH_SHORT)
                    checkForBusyBox()
                }
            }
        }
        else
        checkForBusyBox()
    }

    fun checkForBusyBox() {
        hasBusyBox = false
        when {
            Utils.findInPath("busybox") -> {
                hasBusyBox = true
                MainActivity.TOOL = "busybox"
            }
            Utils.findInPath("toybox") -> {
                hasBusyBox = true
                MainActivity.TOOL = "toybox"
                CustomToast.showNotifyToast(context, "busybox not found, we use toybox,\nplease consider installing busybox in case of any incoveniences.", Toast.LENGTH_SHORT)
            }
            Utils.findInPath("toolbox") -> {
                hasBusyBox = true
                MainActivity.TOOL = "toolbox"
                CustomToast.showNotifyToast(context, "busybox not found, we use toolbox,\nplease consider installing busybox in case of any incoveniences.", Toast.LENGTH_SHORT)
            }
        }
        runOnMainThread(Runnable {
            if (!hasBusyBox) {
                error("Oops...", "busybox not found", "Install",
                    DialogInterface.OnClickListener { _, _ ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("market://details?id=ru.meefik.busybox")
                            context.startActivity(intent)
                            finishActivity()
                        } catch (ex:Exception) {
                            Log.e(MainActivity.TAG, ex.toString())
                            CustomToast.showFailureToast(context, "No market is found\nplease manually install busybox to continue", Toast.LENGTH_SHORT)
                        }
                    })
            } else {
                if (!(context as MainActivity).opened) {
                    context.opened = true
                    if (!isFirstRun) NormalStartup()
                    else saveBlockdevs()
                }
            }
        })
    }


    fun NormalStartup() {
        runOnMainThread(Runnable {
            if (Build.VERSION.SDK_INT > 22 && (context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
                pDialog.setMessage("Obtaining storage permissions")
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS), 69243)
            } else {
                context.getSharedPreferences("general", 0).edit().putBoolean("isFirstRun", false).apply()
                onStartup()
            }
        })
    }

    open fun onStartup() {
        pDialog.dismiss()
        fullScreenDialog.dismiss()
    }

    fun onRequestPermissionsResult(requestCode:Int, permissions:Array<String>, grantResults:IntArray) {
        pDialog.dismiss()
        when (requestCode) {
            69243 -> if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                context.getSharedPreferences("general", 0).edit().putBoolean("isFirstRun", false).apply()
                onStartup()
            } else {
                CustomToast.showFailureToast(context,
                "This app will not work unless you grant permissions..!!",
                Toast.LENGTH_LONG)
                error("Oops...", "Storage permissions Denied..!!", null, null)
                return
            }
        }
    }
}
