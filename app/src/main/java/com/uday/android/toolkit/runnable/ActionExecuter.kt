package com.uday.android.toolkit.runnable
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.Toast
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.fragments.AndroidImagesFragment
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.toolkit.ui.DialogUtils
import com.uday.android.util.Utils
import eu.chainfire.libsuperuser.Shell
import java.io.File
import java.util.*

class ActionExecuter(private val fragment:AndroidImagesFragment):Runnable {
    private val context:Context
    init{
        this.context = fragment.context
    }

    override fun run() {
        when (fragment.mOption) {
            AndroidImagesFragment.SELECTED_UNPACK -> try {
                    fragment.tmpstr = ""
                    MainActivity.rootSession!!.addCommand((context.getFilesDir()).toString() + "/common/boot_unpack.sh " + MainActivity.TOOL + " " + '"'.toString() + fragment.fileChoosen!!.absolutePath + '"'.toString() + " " + fragment.pName, AndroidImagesFragment.SELECTED_UNPACK, fragment.commandLineListener)
                } catch (ex:Exception) {
                    Log.e(MainActivity.TAG, ex.toString() + "\n" + ex.message)
                    CustomToast.showFailureToast(context, ex.toString(), Toast.LENGTH_SHORT)
                }

            AndroidImagesFragment.SELECTED_REPACK -> MainActivity.rootSession!!.addCommand((context.getFilesDir()).toString() + "/common/boot_repack.sh " + MainActivity.TOOL + " /data/local/ToolKit/" + fragment.choosen, 4, fragment.commandLineListener)
            AndroidImagesFragment.SELECTED_INSTALL -> MainActivity.rootSession!!.addCommand(MainActivity.TOOL + " dd if=" + '"'.toString() + fragment.fileChoosen!!.absolutePath + '"'.toString() + " of=" + fragment.BLOCK, AndroidImagesFragment.SELECTED_INSTALL) { commandCode, exitCode, output ->
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
                fragment.backupDir = File(fragment.backupDir!!.absolutePath + "/" + Calendar.getInstance().time.toLocaleString().replace(" ", "_").replace(",", "").replace(":", ""))
                fragment.backupDir!!.mkdirs()
                MainActivity.rootSession!!.addCommand((MainActivity.TOOL + " mkdir -p '" + fragment.backupDir + "'\n" + MainActivity.TOOL + " dd if=" + fragment.BLOCK + " of=" + fragment.backupDir!!.absolutePath + "/" + fragment.BLOCK_NAME), AndroidImagesFragment.SELECTED_BACKUP, object:Shell.OnCommandResultListener {
                    override fun onCommandResult(commandCode:Int, exitCode:Int, output:List<String>) {
                        fragment.runOnUiThread(object:Runnable {
                            override fun run() {
                                if (fragment.pDialog != null) fragment.pDialog!!.cancel()
                                if (exitCode == 0) {
                                    DialogUtils.showConfirmDialog(context, "Success", "Backup completed.\nyou can find backup in\n" + fragment.backupDir!!.absolutePath, null, "close", null).getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
                                    CustomToast.showSuccessToast(context, "Successfully backed up", Toast.LENGTH_SHORT)
                                }
                                else {
                                    DialogUtils.showConfirmDialog(context, "Failed", "Failed to backup\nCheck log for more info...", null, "close", null).getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
                                    CustomToast.showFailureToast(context, Utils.getString(output), Toast.LENGTH_SHORT)
                                }
                            }
                        })
                    }
                })
            }

            AndroidImagesFragment.SELECTED_RESTORE_ITEM -> MainActivity.rootSession!!.addCommand(MainActivity.TOOL + " dd if=" + fragment.backupDir + "/" + fragment.choosen + "/" + fragment.BLOCK_NAME + " of=" + fragment.BLOCK, AndroidImagesFragment.SELECTED_BACKUP, object:Shell.OnCommandResultListener {
                override fun onCommandResult(commandCode:Int, exitCode:Int, output:List<String>) {
                    fragment.runOnUiThread(object:Runnable {
                        override fun run() {
                            if (fragment.pDialog != null) fragment.pDialog!!.cancel()
                            if (exitCode == 0) {
                                DialogUtils.showConfirmDialog(context, "Success", "Restore completed.\nReboot the device for changes to take effect...", null, "close", null).getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
                                CustomToast.showSuccessToast(context, Utils.getString(output), Toast.LENGTH_SHORT)
                            }
                            else {
                                DialogUtils.showConfirmDialog(context, "Failed", "Failed to Restore\nCheck log for more info...", null, "close", null).getButton(DialogInterface.BUTTON_NEGATIVE).visibility = View.GONE
                                CustomToast.showFailureToast(context, Utils.getString(output), Toast.LENGTH_SHORT)
                            }
                        }
                    })
                }
            })
        }
    }
}
