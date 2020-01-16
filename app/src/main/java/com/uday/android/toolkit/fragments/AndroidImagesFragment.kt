package com.uday.android.toolkit.fragments

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.OnFileSelectedListener
import com.uday.android.toolkit.R
import com.uday.android.toolkit.listeners.AndImgListListener
import com.uday.android.toolkit.listeners.ConfirmListener
import com.uday.android.toolkit.listeners.TermListener
import com.uday.android.toolkit.runnable.ActionExecutor
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.toolkit.ui.DialogUtils
import com.uday.android.util.PathUtil
import com.uday.android.util.Utils
import eu.chainfire.libsuperuser.Shell
import java.io.File
import java.util.*

@SuppressLint("NewApi")
class AndroidImagesFragment: Fragment {

//     var mBlock:Int = 0
     var mOption:Int = 0
     var backupDir:File? = null
     var fileChoosen:File?=null
     var tmpstr:String? = null
     var pName:String?=null
     var choosen:String?=null
     var boot:String? = null
     var recovery:String? = null
     var logo:String? = null
     var block:String? = null
     var blockName:String? = null
     var commandLineListener:TermListener?=null
//     var termFinished:Button? = null
     var termTextView:TextView?=null
     var termDialog: AlertDialog?=null
     @Suppress("DEPRECATION")
     var pDialog:android.app.ProgressDialog? = null
     var executor:ActionExecutor?=null
//     var blockAdapter:ArrayAdapter<*>? = null
     private var imagesDialog: AlertDialog?=null
     private var projectDialog: AlertDialog?=null
//     var confirmDialog: AlertDialog? = null
     var mListener:ConfirmListener?=null
     var dir:File?=null
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
    private var fileSelectedListener:OnFileSelectedListener? = null

//    var blockNames = arrayOf("Boot image", "Recovery image", "Boot Logo")

    constructor()

    override fun getContext():Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && context == null) {
            context = super.getContext()
        }
        return context!!
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        if (rootView != null) refreshList()
        dir = if (getContext().getSharedPreferences("general", 0).getInt("storage", 0) == 1) {
            if (Utils.externalSdCard != null) {
                File(Utils.externalSdCard!!.absolutePath + "/ToolKit")
            } else {
                CustomToast.showFailureToast(getContext(), "External sdcard not found,\nuses internal storage for backups", Toast.LENGTH_SHORT)
                File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
            }
        } else {
            File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
        }
        (rootView!!.findViewById(R.id.backup_dir_android) as TextView).text =
            "${(dir).toString()}/backups"
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
		prefs = context.getSharedPreferences("block_devs", 0)
        boot = prefs?.getString("boot", null)
        recovery = prefs?.getString("recovery", null)
        logo = prefs?.getString("logo", null)

        commandLineListener = TermListener(this)
        executor = ActionExecutor(this)
        mListener = ConfirmListener(this)
        fileSelectedListener = object:OnFileSelectedListener {
            override fun onFileSelected(file:File) {
                fileChoosen = file
                if(!(file.absolutePath.endsWith(".img") or file.absolutePath.endsWith(".bin"))){
                    Toast.makeText(context,"Please select an img ir bin file",Toast.LENGTH_LONG).show()
                    return
                }
                pName = fileChoosen?.name?.replace(' ', '_')?.replace('.', '_')
                @Suppress("DEPRECATION")
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
                                    Thread(executor).start()
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

            override fun onMultipleFilesSelected(files: ArrayList<File>) {}
        }

		list = arrayOf("please wait a second")
        initialise()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            IMAGE_SELECT_CODE ->
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    // Get the Uri of the selected file
                    val uri = data?.data
                    try {
                        fileSelectedListener?.onFileSelected(File(PathUtil.getPath(context!!,uri)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle?):View? {
        dir = if (getContext().getSharedPreferences("general", 0).getInt("storage", 0) == 1) {
            if (Utils.externalSdCard != null) {
                File(Utils.externalSdCard!!.absolutePath + "/ToolKit")
            } else {
                Toast.makeText(getContext(), "External sdcard not found,\nuses internal storage for backups", Toast.LENGTH_SHORT).show()
                File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
            }
        } else {
            File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
        }
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.android_image, container, false) as RelativeLayout
            firstOpen()
        }
        rootView!!.startAnimation(MainActivity.mFadeIn)
        (rootView!!.findViewById(R.id.backup_dir_android) as TextView).text = (dir).toString() + "/" + getString(R.string.backp_dir_name)
        return rootView
    }



    private fun firstOpen() {
        rootView!!.findViewById<LinearLayout>(R.id.path_unpack_dir).setOnClickListener { Utils.openFolder(getContext(), Environment.getDataDirectory().absolutePath + "/local/ToolKit/") }
        rootView!!.findViewById<LinearLayout>(R.id.magic_path_backup_dir).setOnClickListener { Utils.openFolder(getContext(), dir?.absolutePath + "/backups/") }
        list = arrayOf("")
        menu = rootView!!.findViewById(R.id.menu_img)
        menu!!.setClosedOnTouchOutside(true)
        refreshList()

        val unpack = rootView!!.findViewById<FloatingActionButton>(R.id.unpack)
        unpack.setOnClickListener {
            mOption = SELECTED_UNPACK
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/octet-stream"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                startActivityForResult(Intent.createChooser(intent, "Select an img file to unpack"), IMAGE_SELECT_CODE)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(context, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
            }
        }
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
            ) { _, exitCode, output ->
                if (exitCode != 0) {
                    Log.e(MainActivity.TAG, "error Obtaining backups list$exitCode")
                } else {
                    runOnUiThread(Runnable {
                        try {
                            list = Utils.getString(output).split(("\n").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            projectDialog?.setTitle("select project to unpack")
                            listProjectView!!.adapter = object:ArrayAdapter<String>(getContext(),
                                android.R.layout.simple_list_item_1, list!!) {
                                override fun getView(pos:Int, view_:View?, parent:ViewGroup):View {
                                    var view = view_
                                    view = super.getView(pos, view, parent)
                                    (view as TextView).setSingleLine(false)
                                    return view
                                }
                            }

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
        listProjectView!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, p2, _, _ ->
                projectDialog?.cancel()
                choosen = (p2 as TextView).text.toString()
                mOption = SELECTED_REPACK
                @Suppress("DEPRECATION")
                DialogUtils.showConfirmDialog(getContext(), "Repack project", null, Html.fromHtml("Selected project : <b>$choosen</b>"), "confirm", mListener)
            }
    }

    fun runOnUiThread(action:Runnable) {
        (getContext() as AppCompatActivity).runOnUiThread(action)
    }

    companion object {
         const val SELECTED_UNPACK = 1
         const val SELECTED_REPACK = 0
         const val SELECTED_INSTALL = 2
         const val SELECTED_BACKUP = 4
         const val SELECTED_RESTORE = 3
//         val SELECTED_BOOT = 5
//         val SELECTED_RECOVERY = 6
//         val SELECTED_LOGO = 7
         const val SELECTED_RESTORE_ITEM = 8
         const val IMAGE_SELECT_CODE =  7265

        private var rootSession:Shell.Interactive?=null
    }

}
