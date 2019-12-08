package com.uday.android.toolkit.fragments

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.app.ProgressDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*

import com.github.angads25.filepicker.controller.DialogSelectionListener
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R
import com.uday.android.toolkit.listeners.AndImgListListener
import com.uday.android.toolkit.listeners.ConfirmListener
import com.uday.android.toolkit.listeners.TermListener
import com.uday.android.toolkit.runnable.ActionExecuter
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.toolkit.ui.DialogUtils
import com.uday.android.util.Utils

import java.io.File

import eu.chainfire.libsuperuser.Shell

@SuppressLint("NewApi")
class AndroidImagesFragment: androidx.fragment.app.Fragment {

     var mBlock:Int = 0
     var mOption:Int = 0
     var backupDir:File? = null
     var fileChoosen:File?=null
     var tmpstr:String? = null
     var pName:String?=null
     var choosen:String?=null
     var BOOT:String? = null
     var RECOVERY:String? = null
     var LOGO:String? = null
     var BLOCK:String? = null
     var BLOCK_NAME:String? = null
     var commandLineListener:TermListener?=null
     var term_finished:Button? = null
     var termTextView:TextView?=null
     var termDialog: AlertDialog?=null
     var pDialog:ProgressDialog? = null
     var executer:ActionExecuter?=null
     var BlockAdapter:ArrayAdapter<*>? = null
     var dialog:FilePickerDialog?=null
     var properties:DialogProperties?=null
     var imagesDialog: AlertDialog?=null
     var projectDialog: AlertDialog?=null
     var confirmDialog: AlertDialog? = null
     var mListener:ConfirmListener?=null
     var fileListener:DialogSelectionListener?=null
     var DIR:File?=null
     var termProgress:ProgressBar?=null

    private var rootView:RelativeLayout? = null
    private var repack:FloatingActionButton? = null
    private var install:FloatingActionButton? = null
    private var backup:FloatingActionButton? = null
    private var restore:FloatingActionButton? = null

    private var menu:FloatingActionMenu? = null
    private var edtTxt:EditText? = null
    private var list:Array<String>? = null
    private var listProjectView:ListView? = null
    private var context:Context? = null
    private var prefs:SharedPreferences?=null
    var blockNames = arrayOf("Boot image", "Recovery image", "Boot Logo")

    constructor() {}

    override fun getContext():Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && context == null) {
            context = super.getContext()
        }
        return context!!
    }

    override fun onResume() {
        if (rootView != null) refreshList()
        if (getContext().getSharedPreferences("general", 0).getInt("storage", 0) == 1) {
            if (Utils.externalSdCard != null) {
                DIR = File(Utils.externalSdCard!!.absolutePath + "/ToolKit")
            } else {
                CustomToast.showFailureToast(getContext(), "External sdcard not found,\nuses internal storage for backups", Toast.LENGTH_SHORT)
                DIR = File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
            }
        } else {
            DIR = File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
        }
        (rootView!!.findViewById(R.id.backup_dir_android) as TextView).setText((DIR).toString() + "/backups")
        if (!menu!!.isOpened) {
            menu!!.hideMenuButton(false)
            Handler().postDelayed({ menu!!.showMenuButton(true) }, 400)
        }
        super.onResume()
    }

    @SuppressLint("ValidFragment")
    constructor(context:Context) {
        rootSession = MainActivity.rootSession
        this.context = context
        properties = DialogProperties()
        properties?.selection_mode = DialogConfigs.SINGLE_MODE
        properties?.selection_type = DialogConfigs.FILE_SELECT
        properties?.root = Environment.getExternalStorageDirectory()
        properties?.error_dir = File(DialogConfigs.DEFAULT_DIR)
        properties?.offset = File(DialogConfigs.DEFAULT_DIR)
        properties?.extensions = arrayOf(".img", ".bin", ".win")
        properties?.hasStorageButton = true

		prefs = context.getSharedPreferences("block_devs", 0)
        BOOT = prefs?.getString("boot", null)
        RECOVERY = prefs?.getString("recovery", null)
        LOGO = prefs?.getString("logo", null)

        commandLineListener = TermListener(this)
        executer = ActionExecuter(this)
        mListener = ConfirmListener(this)
        fileListener = object:DialogSelectionListener {
            override fun onSelectedFilePaths(files:Array<String>) {
                fileChoosen = File(files[0])
                pName = fileChoosen?.name?.replace(' ', '_')?.replace('.', '_')
                when (mOption) {
                    SELECTED_UNPACK -> edtTxt = DialogUtils.showEditTextDialog(getContext(), "Enter project name...", "(Any name without space)\nNote: This will overwrite the existing project with same name if exists..!", pName, "project name", "confirm", object:DialogUtils.OnClickListener {
                        override fun onClick(p1: AlertDialog?) {
                            if (edtTxt!!.text.toString() == "") {
                                CustomToast.showNotifyToast(getContext(), "Please enter project name...", Toast.LENGTH_SHORT)
                            }
                            else if (edtTxt!!.text.toString().contains(" ") || edtTxt!!.text.toString().contains("\n")) {
                                CustomToast.showNotifyToast(getContext(), "project name should not contain any spaces,\nplease enter a new name", Toast.LENGTH_SHORT)
                            } else {
                                try {
                                    pName = edtTxt!!.text.toString()
                                    (getContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                                    .hideSoftInputFromWindow(edtTxt!!.windowToken, 0)
                                    p1!!.cancel()
                                    val obj = DialogUtils.showTermDialog(getContext(), "Unpack img", "unpacking please wait...", "open folder", "finish")
                                    termDialog = obj[0] as AlertDialog
                                    termTextView = obj[1] as TextView
                                    termProgress = obj[2] as ProgressBar
                                    Thread(executer).start()
                                } catch (ex:Exception) {
                                    Log.e(MainActivity.TAG, ex.toString() + ex.message)
                                    CustomToast.showFailureToast(getContext(), ex.toString(), Toast.LENGTH_SHORT)
                                }
                            }
                        }
                    })
                    SELECTED_INSTALL -> DialogUtils.showConfirmDialog(getContext(), "Install $choosen", null, Html.fromHtml("Are you sure you want to install <br><b>" + fileChoosen?.absolutePath + "</b><br> as <b>" + choosen + "</b>..??"), "confirm", mListener)
                }
            }
        }

		list = arrayOf("please wait a second")
        initialise()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle?):View? {
        if (getContext().getSharedPreferences("general", 0).getInt("storage", 0) == 1) {
            if (Utils.externalSdCard != null) {
                DIR = File(Utils.externalSdCard!!.absolutePath + "/ToolKit")
            } else {
                Toast.makeText(getContext(), "External sdcard not found,\nuses internal storage for backups", Toast.LENGTH_SHORT).show()
                DIR = File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
            }
        } else {
            DIR = File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
        }
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.android_image, container, false) as RelativeLayout
            firstOpen()
        }
        rootView!!.startAnimation(MainActivity.mFadeIn)
        (rootView!!.findViewById(R.id.backup_dir_android) as TextView).setText((DIR).toString() + "/" + getString(R.string.backp_dir_name))
        return rootView
    }



    private fun firstOpen() {
        rootView!!.findViewById<LinearLayout>(R.id.path_unpack_dir).setOnClickListener(OnClickListener { Utils.openFolder(getContext(), Environment.getDataDirectory().absolutePath + "/local/ToolKit/") })
        rootView!!.findViewById<LinearLayout>(R.id.magic_path_backup_dir).setOnClickListener(OnClickListener { Utils.openFolder(getContext(), DIR?.absolutePath + "/backups/") })
        list = arrayOf("")
        menu = rootView!!.findViewById(R.id.menu_img)
        menu!!.setClosedOnTouchOutside(true)
        refreshList()

        val unpack = rootView!!.findViewById<FloatingActionButton>(R.id.unpack)
        unpack.setOnClickListener(OnClickListener {
            mOption = SELECTED_UNPACK
            properties?.extensions = arrayOf(".img")
            dialog = FilePickerDialog(getContext(), properties!!, R.style.AppTheme)
            dialog?.setTitle("Select an img file to unpack")
            dialog?.setDialogSelectionListener(fileListener!!)
            dialog?.show()
        })
        repack = rootView!!.findViewById(R.id.repack)
        repack!!.setOnClickListener { projectDialog?.show() }
            install = rootView!!.findViewById(R.id.install)
        install!!.setOnClickListener {
            mOption = SELECTED_INSTALL
            imagesDialog?.show()
        }
            backup = rootView!!.findViewById(R.id.backup)
        backup!!.setOnClickListener {
            mOption = SELECTED_BACKUP
            imagesDialog?.show()
        }
            restore = rootView!!.findViewById(R.id.restore)
        restore!!.setOnClickListener {
            mOption = SELECTED_RESTORE
            imagesDialog?.show()
        }
    }

    fun refreshList() {
        try {
            rootSession!!.addCommand(((MainActivity.TOOL + " mkdir -p " + Environment.getDataDirectory().absolutePath + "/local/ToolKit\n"
            + MainActivity.TOOL + " ls /data/local/ToolKit/ | while read file ; do\n"
            + "if [ ! -f /data/local/ToolKit/\$file ] && [ -f /data/local/ToolKit/\$file/boot_orig.img ] && [ -f /data/local/ToolKit/\$file/kernel ] ; then\n"
            + MainActivity.TOOL + " echo \$file\n"
            + "fi\n"
            + "done")).split(("\n").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray(), 1234
            ) { commandCode, exitCode, output ->
                if (exitCode != 0) {
                    Log.e(MainActivity.TAG, "error Obtaining backups list$exitCode")
                } else {
                    runOnUiThread(Runnable {
                        try {
                            list = Utils.getString(output).split(("\n").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            projectDialog?.setTitle("select project to unpack")
                            listProjectView!!.setAdapter(object:ArrayAdapter<String>(getContext(),
                                android.R.layout.simple_list_item_1, list!!) {
                                override fun getView(pos:Int, view:View?, parent:ViewGroup):View {
                                    var view = view
                                    view = super.getView(pos, view, parent)
                                    (view as TextView).setSingleLine(false)
                                    return view
                                }
                            })

                        } catch (ex:Exception) {
                            list = null
                            projectDialog?.setTitle("no project found..!!")
                        }
                    })
                }
            }
        }
        catch (ex:Exception) {
            Log.e(MainActivity.TAG, ex.toString())
        }

    }

    private fun initialise() {
        @SuppressLint("InflateParams") val group = (getContext() as AppCompatActivity).layoutInflater.inflate(R.layout.partition_selection_dialog, null) as RadioGroup
        group.check(R.id.recovery_selection)
        imagesDialog = AlertDialog.Builder(getContext()).setView(group).setTitle("select partition").setNegativeButton("cancel", null).setPositiveButton("confirm", AndImgListListener(this, group)).create()
        listProjectView = ListView(getContext())
        listProjectView!!.setPadding(30, 20, 20, 0)
        projectDialog = AlertDialog.Builder(getContext()).setView(listProjectView).setNegativeButton("cancel", null).create()
        listProjectView!!.onItemClickListener = object:AdapterView.OnItemClickListener {
            override fun onItemClick(p1:AdapterView<*>, p2:View, p3:Int, p4:Long) {
                projectDialog?.cancel()
                choosen = (p2 as TextView).text.toString()
                mOption = SELECTED_REPACK
                DialogUtils.showConfirmDialog(getContext(), "Repack project", null, Html.fromHtml("Selected project : <b>$choosen</b>"), "confirm", mListener)
            }
        }
    }

    fun runOnUiThread(action:Runnable) {
        (getContext() as AppCompatActivity).runOnUiThread(action)
    }

    companion object {
         val SELECTED_UNPACK = 1
         val SELECTED_REPACK = 0
         val SELECTED_INSTALL = 2
         val SELECTED_BACKUP = 4
         val SELECTED_RESTORE = 3
         val SELECTED_BOOT = 5
         val SELECTED_RECOVERY = 6
         val SELECTED_LOGO = 7
         val SELECTED_RESTORE_ITEM = 8


        private var rootSession:Shell.Interactive?=null
    }

}
