package com.uday.android.toolkit.runnable
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.fragments.AndroidImagesFragment
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.toolkit.ui.DialogUtils
import com.uday.android.util.Utils
import java.io.File
import java.util.*

class ActionExecutor(private val fragment:AndroidImagesFragment):Runnable {
    private val context:Context = fragment.context

    override fun run() {
        when (fragment.mOption) {
            AndroidImagesFragment.SELECTED_UNPACK -> try {
                    fragment.tmpstr = ""
                    MainActivity.rootSession!!.addCommand((context.filesDir).toString() + "/common/boot_unpack.sh " + MainActivity.TOOL + " " + '"'.toString() + fragment.fileChoosen!!.absolutePath + '"'.toString() + " " + fragment.pName, AndroidImagesFragment.SELECTED_UNPACK, fragment.commandLineListener)
                } catch (ex:Exception) {
                    Log.e(MainActivity.TAG, ex.toString() + "\n" + ex.message)
                    CustomToast.showFailureToast(context, ex.toString(), Toast.LENGTH_SHORT)
                }

            AndroidImagesFragment.SELECTED_REPACK -> MainActivity.rootSession!!.addCommand((context.filesDir).toString() + "/common/boot_repack.sh " + MainActivity.TOOL + " /data/local/ToolKit/" + fragment.choosen, 4, fragment.commandLineListener)
            AndroidImagesFragment.SELECTED_INSTALL -> MainActivity.rootSession!!.addCommand(MainActivity.TOOL + " dd if=" + '"'.toString() + fragment.fileChoosen!!.absolutePath + '"'.toString() + " of=" + fragment.block, AndroidImagesFragment.SELECTED_INSTALL) { _, exitCode, output ->
                (context as AppCompatActivity).runOnUiThread {
                    if (fragment.pDialog != null) fragment.pDialog!!.cancel()
                    if (exitCode == 0) {
                        DialogUtils.showConfirmDialog(context, "Success", "Installation completed,\nreboot the device for the changes to take effect...", null, "close", null).getButton(DialogInterface.BUTTON_NEGATIVE)
                            .visibility = View.GONE
                        CustomToast.showSuccessToast(context, "installation successful", Toast.LENGTH_SHORT)
                    } else {
                        DialogUtils.showConfirmDialog(context, "Failed", "Installation Failed...", null, "dismiss", null).getButton(DialogInterface.BUTTON_NEGATIVE)
                            .visibility = View.GONE
                        CustomToast.showFailureToast(context, Utils.getString(output), Toast.LENGTH_SHORT)
                    }
                }
            }
            AndroidImagesFragment.SELECTED_BACKUP -> {
                @Suppress("DEPRECATION")
                fragment.backupDir = File(fragment.backupDir!!.absolutePath + "/" + Calendar.getInstance().time.toLocaleString().replace(" ", "_").replace(",", "").replace(":", ""))
                fragment.backupDir!!.mkdirs()
                MainActivity.rootSession!!.addCommand((MainActivity.TOOL + " mkdir -p '" + fragment.backupDir + "'\n" + MainActivity.TOOL + " dd if=" + fragment.block + " of=" + fragment.backupDir!!.absolutePath + "/" + fragment.blockName), AndroidImagesFragment.SELECTED_BACKUP
                ) { _, exitCode, output ->
                    fragment.runOnUiThread(Runnable {
                        if (fragment.pDialog != null) fragment.pDialog!!.cancel()
                        if (exitCode == 0) {
                            DialogUtils.showConfirmDialog(context, "Success", "Backup completed.\nyou can find backup in\n" + fragment.backupDir!!.absolutePath, null, "close", null).getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
                            CustomToast.showSuccessToast(context, "Successfully backed up", Toast.LENGTH_SHORT)
                        } else {
                            DialogUtils.showConfirmDialog(context, "Failed", "Failed to backup\nCheck log for more info...", null, "close", null).getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
                            CustomToast.showFailureToast(context, Utils.getString(output), Toast.LENGTH_SHORT)
                        }
                    })
                }
            }

            AndroidImagesFragment.SELECTED_RESTORE_ITEM -> MainActivity.rootSession!!.addCommand(MainActivity.TOOL + " dd if=" + fragment.backupDir + "/" + fragment.choosen + "/" + fragment.blockName + " of=" + fragment.block, AndroidImagesFragment.SELECTED_BACKUP
            ) { _, exitCode, output ->
                fragment.runOnUiThread(Runnable {
                    if (fragment.pDialog != null) fragment.pDialog!!.cancel()
                    if (exitCode == 0) {
                        DialogUtils.showConfirmDialog(context, "Success", "Restore completed.\nReboot the device for changes to take effect...", null, "close", null).getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
                        CustomToast.showSuccessToast(context, Utils.getString(output), Toast.LENGTH_SHORT)
                    } else {
                        DialogUtils.showConfirmDialog(context, "Failed", "Failed to Restore\nCheck log for more info...", null, "close", null).getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
                        CustomToast.showFailureToast(context, Utils.getString(output), Toast.LENGTH_SHORT)
                    }
                })
            }
        }
    }
}
