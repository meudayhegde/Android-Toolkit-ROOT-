package com.uday.android.toolkit.fragments

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import androidx.fragment.app.Fragment
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Html
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import com.github.angads25.filepicker.controller.DialogSelectionListener
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R
import com.uday.android.toolkit.listeners.KernCommandLineListener
import com.uday.android.toolkit.runnable.KernelAction
import com.uday.android.toolkit.ui.DialogUtils
import com.uday.android.util.Utils
import eu.chainfire.libsuperuser.Shell
import java.io.File


@SuppressLint("NewApi", "ValidFragment")
class KernelFragment(private val context:Context) : androidx.fragment.app.Fragment() {
    var termDialog: AlertDialog?=null
    var termTextView: TextView?=null
    var termProgress: ProgressBar?=null
    var mOption: Int = 0
    var file: File?=null
    var selected: File?=null
    var BLOCK: String? = null
    var fileList: Array<String>?=null
    var isImg: String?=null
    var line: String?=null
    var commandLineListener: KernCommandLineListener
    var Inst: FloatingActionButton?=null
    var DIR: File?=null
    private var list: ListView? = null
    private var dialog: FilePickerDialog? = null
    private val properties: DialogProperties
    private var rootView: RelativeLayout? = null
    private var selector: Dialog? = null
    private var menu: FloatingActionMenu? = null
    private val rootSession: Shell.Interactive?
    private val backupsListListener: Shell.OnCommandLineListener
    private val confirmClickListener: DialogInterface.OnClickListener
    private val listItemOnClick: AdapterView.OnItemClickListener
    private val onFileSelected: DialogSelectionListener
    private val backTerm: KernelAction
    var kernVersion: String?=null

    override fun onResume() {
        try {
            Inst?.isEnabled = context.getSharedPreferences("general", 0).getBoolean("allow_kern_installation", false)
        } catch (ex: NullPointerException) { }
        if (context.getSharedPreferences("general", 0).getInt("storage", 0) == 1) {
            if (Utils.externalSdCard != null) {
                DIR = File(Utils.externalSdCard!!.absolutePath + "/ToolKit")
            } else {
                Toast.makeText(
                    context,
                    "External sdcard not found,\nuses internal storage for backups",
                    Toast.LENGTH_SHORT
                ).show()
                DIR = File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
            }
        } else {
            DIR = File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
        }
        (rootView!!.findViewById(R.id.kern_backup_dir) as TextView).text = "$DIR/backups/Kernel"
        updateBackupsList()

        if (!menu!!.isOpened) {
            menu!!.hideMenuButton(false)
            Handler().postDelayed({ menu!!.showMenuButton(true) }, 400)
        }

        super.onResume()
    }

    init {
        rootSession = MainActivity.rootSession
        properties = DialogProperties()
        properties.selection_mode = DialogConfigs.SINGLE_MODE
        properties.selection_type = DialogConfigs.FILE_SELECT
        properties.root = Environment.getExternalStorageDirectory()
        properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
        properties.offset = File(DialogConfigs.DEFAULT_DIR)
        properties.hasStorageButton = true
        initialise(context)

        commandLineListener = KernCommandLineListener(this)

        listItemOnClick = AdapterView.OnItemClickListener { parent, view, which, id ->
            selector!!.cancel()
            val fileChosen = list!!.getItemAtPosition(which) as String
            val selected = File("$DIR/backups/Kernel/$fileChosen")
            if (File(selected.absolutePath + "/kernel").exists() && !selected.absolutePath.contains(
                    " "
                )
            ) {
                kernRestore(selected)
            }
        }

        backTerm = KernelAction(this)

        confirmClickListener = DialogInterface.OnClickListener { sweet, p2 ->
            sweet.cancel()
            val obj: Array<Any>
            when (mOption) {
                SELECTED_BACKUP -> {
                    obj = DialogUtils.showTermDialog(
                        this@KernelFragment.context,
                        "kernel backup",
                        "backing up kernel please wait...",
                        "finish",
                        null
                    )
                    termDialog = obj[0] as AlertDialog
                    termTextView = obj[1] as TextView
                    termProgress = obj[2] as ProgressBar
                }
                SELECTED_RESTORE -> {
                    obj = DialogUtils.showTermDialog(
                        this@KernelFragment.context,
                        "Restoring",
                        "Restoring kernel please wait...",
                        "finish",
                        null
                    )
                    termDialog = obj[0] as AlertDialog
                    termTextView = obj[1] as TextView
                    termProgress = obj[2] as ProgressBar
                }
                SELECTED_INSTALL -> {
                    obj = DialogUtils.showTermDialog(
                        this@KernelFragment.context,
                        "Installing",
                        "Installing kernel please wait...",
                        "finish",
                        null
                    )
                    termDialog = obj[0] as AlertDialog
                    termTextView = obj[1] as TextView
                    termProgress = obj[2] as ProgressBar
                }
            }
            termDialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.visibility = View.VISIBLE
            termDialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = false
            Thread(backTerm).start()
        }


        onFileSelected = object : DialogSelectionListener {
            override fun onSelectedFilePaths(files: Array<String>) {
                file = File(files[0])
                var warn =
                    "Do you want to install as kernel at your own <b>knowledge and risk</b>.??"
                isImg = "false"
                if (file!!.name!!.endsWith(".img")) {
                    warn = "If it is having valid <b>android magic</b>,<br>" +
                            "kernel will be extracted from the img file you selected," +
                            "and then will be installed on your device..??"
                    isImg = "true"
                }
                val tw = TextView(this@KernelFragment.context)
                tw.setPadding(30, 30, 20, 0)
                tw.textSize = 16f
                tw.text = Html.fromHtml(
                    "Installing <b>wrong kernel</b> is extremely harmful and it will definitely <b>hard brick</b>" +
                            " your device.<br><br>You have selected<br><br>" + "<b>"
                            + file!!.absolutePath + "</b><br>" + "<br>" + warn + "<br>"
                )
                mOption = SELECTED_INSTALL
                AlertDialog.Builder(this@KernelFragment.context)
                    .setIcon(R.drawable.warning_red)
                    .setPositiveButton("Install Anyways", confirmClickListener)
                    .setNegativeButton("Don't install", null)
                    .setView(tw)
                    .setTitle("Kernel Installation:-")
                    .show()
            }
        }

        backupsListListener = object : Shell.OnCommandLineListener {
            override fun onCommandResult(commandcode: Int, exitcode: Int) {
                fileList = line?.split("\n".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
                line = ""
            }

            override fun onLine(line: String) {
                this@KernelFragment.line = this@KernelFragment.line + line + "\n"
            }
        }

    }

    override fun getContext():Context{
        return context
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (context.getSharedPreferences("general", 0).getInt("storage", 0) == 1) {
            if (Utils.externalSdCard != null) {
                DIR = File(Utils.externalSdCard!!.absolutePath + "/ToolKit")
            } else {
                Toast.makeText(
                    context,
                    "External sdcard not found,\nuses internal storage for backups",
                    Toast.LENGTH_SHORT
                ).show()
                DIR = File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
            }
        } else {
            DIR = File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
        }

        if (rootView == null) {
            line = ""
            rootView = inflater
                .inflate(R.layout.kernel, container, false) as RelativeLayout
            rootView!!.layoutParams =
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
            firstRun()
        }
        rootView!!.startAnimation(MainActivity.mFadeIn)
        Inst?.isEnabled =
            context.getSharedPreferences("general", 0).getBoolean("allow_kern_installation", false)
        (rootView!!.findViewById(R.id.kern_backup_dir) as TextView).text = "$DIR/backups/Kernel"
        return rootView
    }


    private fun firstRun() {
        menu = rootView!!.findViewById(R.id.menu_kernel)
        menu!!.setClosedOnTouchOutside(true)
        val prefs = context.getSharedPreferences("block_devs", 0)
        BLOCK = prefs.getString("boot", null)

        rootView!!.findViewById<LinearLayout>(R.id.kernel_backup_location)
            .setOnClickListener(OnClickListener {
                Utils.openFolder(
                    context,
                    DIR?.absolutePath + "/backups/Kernel"
                )
            })

        fileList = "".split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        kernVersion = System.getProperty("os.version")
        updateBackupsList()
        (rootView!!.findViewById(R.id.current_kernel) as TextView).text = kernVersion

        Inst = rootView!!.findViewById(R.id.kern_install)
        Inst?.setOnClickListener {
            if (dialog == null) {
                dialog = FilePickerDialog(context, properties, R.style.AppTheme)
                dialog!!.setTitle("Select kernel")
                dialog!!.setDialogSelectionListener(onFileSelected)
            }
            dialog!!.show()
        }

        val bkup = rootView!!.findViewById<FloatingActionButton>(R.id.kern_backup)
        bkup.setOnClickListener(OnClickListener { kernBackup() })

        val rstr = rootView!!.findViewById<FloatingActionButton>(R.id.kern_restore)
        rstr.setOnClickListener(OnClickListener {
            list!!.setAdapter(object : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, fileList) {
                override fun getView(pos: Int, view: View?, parent: ViewGroup): View {
                    var view = view
                    view = super.getView(pos, view, parent)
                    (view as TextView).setSingleLine(false)
                    return view
                }
            })
            selector!!.show()
            list!!.divider = ColorDrawable(Color.parseColor("#00AAFF"))
            list!!.dividerHeight = 3
            selector!!.window!!.setLayout(
                WindowManager.LayoutParams.FILL_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )

            list!!.onItemClickListener = listItemOnClick
        })

    }

    private fun kernBackup() {
        var confirmTxt = "Do you want to backup current kernel..??"
        if (File("$DIR/backups/Kernel/$kernVersion").exists())
            confirmTxt =
                "Backup of current kernel already <b>exists</b>.<br>Backing up again will <b>overwrite</b> the existing backup.\nDo you want to backup..?"
        val tw = TextView(this@KernelFragment.context)
        tw.text = Html.fromHtml(confirmTxt)
        tw.setPadding(30, 30, 20, 0)
        tw.textSize = 16f
        mOption = SELECTED_BACKUP
        AlertDialog.Builder(this@KernelFragment.context)
            .setIcon(R.drawable.backup_coloured)
            .setPositiveButton("backup", confirmClickListener)
            .setNegativeButton("cancel", null)
            .setView(tw)
            .setTitle("Kernel Backup:-")
            .show()

    }

    fun kernRestore(selected: File) {
        mOption = SELECTED_RESTORE
        this.selected = selected
        val tw = TextView(this@KernelFragment.context)
        tw.text = Html.fromHtml("Do you want restore<br><b>" + selected.name + "</b>..??")
        tw.setPadding(30, 30, 20, 0)
        tw.textSize = 16f
        mOption = SELECTED_RESTORE
        AlertDialog.Builder(this@KernelFragment.context)
            .setIcon(R.drawable.restore_coloured)
            .setPositiveButton("Restore", confirmClickListener)
            .setNegativeButton("cancel", null)
            .setView(tw)
            .setTitle("Kernel Restore:-")
            .show()

    }

    private fun updateBackupsList() {
        rootSession!!.addCommand(
            context.filesDir.toString() + "/common/get_backups.sh " + MainActivity.TOOL + " " + DIR + "/backups/Kernel",
            1432,
            backupsListListener
        )

    }


    private fun initialise(context: Context) {
        selector = object : Dialog(context) {
            override fun onBackPressed() {
                updateBackupsList()
                selector!!.dismiss()
            }
        }
        selector!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        selector!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        selector!!.setContentView(R.layout.dialog_list_items)
        (selector!!.findViewById(R.id.dg_list_title) as TextView).text = "Select desired backup to restore"
        val icon = selector!!.findViewById<ImageView>(R.id.dg_icon)
        icon.setVisibility(View.VISIBLE)
        icon.setImageResource(R.drawable.restore_coloured)
        list = selector!!.findViewById(R.id.dg_list_view)
    }

    fun runOnUiThread(action: Runnable) {
        (context as AppCompatActivity).runOnUiThread(action)
    }

    companion object {
        val SELECTED_BACKUP = 0
        val SELECTED_RESTORE = 1
        val SELECTED_INSTALL = 2
    }


}
