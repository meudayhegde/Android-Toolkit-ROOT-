package com.uday.android.toolkit.fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Html
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.OnFileSelectedListener
import com.uday.android.toolkit.R
import com.uday.android.toolkit.listeners.KernCommandLineListener
import com.uday.android.toolkit.runnable.KernelAction
import com.uday.android.toolkit.ui.DialogUtils
import com.uday.android.util.PathUtil
import com.uday.android.util.Utils
import eu.chainfire.libsuperuser.Shell
import java.io.File
import java.util.*


@SuppressLint("NewApi", "ValidFragment")
class KernelFragment(private val context:Context) : androidx.fragment.app.Fragment() {
    var termDialog: AlertDialog?=null
    var termTextView: TextView?=null
    var termProgress: ProgressBar?=null
    var mOption: Int = 0
    var file: File?=null
    var selected: File?=null
    var block: String? = null
    var fileList: Array<String>?=null
    var isImg: String?=null
    var line: String?=null
    var commandLineListener: KernCommandLineListener
    private var inst: FloatingActionButton?=null
    var dir: File?=null
    private var list: ListView? = null
    private var rootView: RelativeLayout? = null
    private var selector: Dialog? = null
    private var menu: FloatingActionMenu? = null
    private val rootSession: Shell.Interactive? = MainActivity.rootSession
    private val backupsListListener: Shell.OnCommandLineListener
    private val confirmClickListener: DialogInterface.OnClickListener
    private val listItemOnClick: AdapterView.OnItemClickListener
    private val backTerm: KernelAction
    var kernVersion: String?=null
    private var fileSelectedListener:OnFileSelectedListener?=null

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        try {
            inst?.isEnabled = context.getSharedPreferences("general", 0).getBoolean("allow_kern_installation", false)
        } catch (ex: NullPointerException) { }
        dir = if (context.getSharedPreferences("general", 0).getInt("storage", 0) == 1) {
            if (Utils.externalSdCard != null) {
                File(Utils.externalSdCard!!.absolutePath + "/ToolKit")
            } else {
                Toast.makeText(
                    context,
                    "External sdcard not found,\nuses internal storage for backups",
                    Toast.LENGTH_SHORT
                ).show()
                File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
            }
        } else {
            File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
        }
        (rootView!!.findViewById(R.id.kern_backup_dir) as TextView).text = "$dir/backups/Kernel"
        updateBackupsList()

        if (!menu!!.isOpened) {
            menu!!.hideMenuButton(false)
            Handler().postDelayed({ menu!!.showMenuButton(true) }, 400)
        }

        super.onResume()
    }

    init {
        initialise(context)

        commandLineListener = KernCommandLineListener(this)

        listItemOnClick = AdapterView.OnItemClickListener { _, _, which, _ ->
            selector!!.cancel()
            val fileChosen = list!!.getItemAtPosition(which) as String
            val selected = File("$dir/backups/Kernel/$fileChosen")
            if (File(selected.absolutePath + "/kernel").exists() && !selected.absolutePath.contains(" ")) {
                kernRestore(selected)
            }
        }

        backTerm = KernelAction(this)

        confirmClickListener = DialogInterface.OnClickListener { sweet, _ ->
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


        fileSelectedListener = object : OnFileSelectedListener {
            override fun onFileSelected(file: File) {
                var warn = getString(R.string.install_kernel_warn)
                isImg = "false"
                if (file.name!!.endsWith(".img")) {
                    warn = getString(R.string.kern_install_from_ing_warn)
                    isImg = "true"
                }
                val tw = TextView(this@KernelFragment.context)
                tw.setPadding(30, 30, 20, 0)
                tw.textSize = 16f
                @Suppress("DEPRECATION")
                tw.text = Html.fromHtml(
                    "${getString(R.string.installing)} <b>${getString(R.string.wrong_kernel)}</b> ${getString(
                        R.string.is_extremely_harmful_and_it_will_definitely
                    )} <b>${getString(R.string.hard_brick)}</b> ${getString(R.string.your_device)}.<br><br>${getString(
                        R.string.you_have_selected
                    )}<br><br><b>${file.absolutePath}</b><br><br>$warn<br>"
                )
                mOption = SELECTED_INSTALL
                AlertDialog.Builder(this@KernelFragment.context)
                    .setIcon(R.drawable.warning_red)
                    .setPositiveButton(getString(R.string.install_always), confirmClickListener)
                    .setNegativeButton(getString(R.string.dont_install), null)
                    .setView(tw)
                    .setTitle(getString(R.string.kernel_installation))
                    .show()
            }

            override fun onMultipleFilesSelected(files: ArrayList<File>) {}
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

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dir = if (context.getSharedPreferences("general", 0).getInt("storage", 0) == 1) {
            if (Utils.externalSdCard != null) {
                File(Utils.externalSdCard!!.absolutePath + "/ToolKit")
            } else {
                Toast.makeText(
                    context,
                    "External sdcard not found,\nuses internal storage for backups",
                    Toast.LENGTH_SHORT
                ).show()
                File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
            }
        } else {
            File(Environment.getExternalStorageDirectory().absolutePath + "/ToolKit")
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
        inst?.isEnabled =
            context.getSharedPreferences("general", 0).getBoolean("allow_kern_installation", false)
        (rootView!!.findViewById(R.id.kern_backup_dir) as TextView).text = "$dir/backups/Kernel"
        return rootView
    }


    private fun firstRun() {
        menu = rootView!!.findViewById(R.id.menu_kernel)
        menu!!.setClosedOnTouchOutside(true)
        val prefs = context.getSharedPreferences("block_devs", 0)
        block = prefs.getString("boot", null)

        rootView!!.findViewById<LinearLayout>(R.id.kernel_backup_location)
            .setOnClickListener {
                Utils.openFolder(context, dir?.absolutePath + "/backups/Kernel")
            }

        fileList = "".split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        kernVersion = System.getProperty("os.version")
        updateBackupsList()
        (rootView!!.findViewById(R.id.current_kernel) as TextView).text = kernVersion

        inst = rootView!!.findViewById(R.id.kern_install)
        inst?.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/octet-stream"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            try {
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_kernel_or_boot_img_file)), KERNEL_SELECT_CODE)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(context, getString(R.string.install_file_manager), Toast.LENGTH_SHORT).show()
            }
        }

        val bkup = rootView!!.findViewById<FloatingActionButton>(R.id.kern_backup)
        bkup.setOnClickListener { kernBackup() }

        val rstr = rootView!!.findViewById<FloatingActionButton>(R.id.kern_restore)
        rstr.setOnClickListener {
            list!!.adapter = object : ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, fileList?: arrayOf<String>()) {
                    override fun getView(pos: Int, view_: View?, parent: ViewGroup): View {
                        var view = view_
                        view = super.getView(pos, view, parent)
                        (view as TextView).setSingleLine(false)
                        return view
                    }
                }
            selector!!.show()
            list!!.divider = ColorDrawable(Color.parseColor("#00AAFF"))
            list!!.dividerHeight = 3
            selector!!.window!!.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )

            list!!.onItemClickListener = listItemOnClick
        }

    }

    private fun kernBackup() {
        var confirmTxt = "Do you want to backup current kernel..??"
        if (File("$dir/backups/Kernel/$kernVersion").exists())
            confirmTxt =
                "Backup of current kernel already <b>exists</b>.<br>Backing up again will <b>overwrite</b> the existing backup.\nDo you want to backup..?"
        val tw = TextView(this@KernelFragment.context)
        @Suppress("DEPRECATION")
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

    private fun kernRestore(selected: File) {
        mOption = SELECTED_RESTORE
        this.selected = selected
        val tw = TextView(this@KernelFragment.context)
        @Suppress("DEPRECATION")
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
            context.filesDir.toString() + "/common/get_backups.sh " + MainActivity.TOOL + " " + dir + "/backups/Kernel",
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
        (selector!!.findViewById(R.id.dg_list_title) as TextView).text = getString(R.string.select_desired_backup_to_restore)
        val icon = selector!!.findViewById<ImageView>(R.id.dg_icon)
        icon.visibility = View.VISIBLE
        icon.setImageResource(R.drawable.restore_coloured)
        list = selector!!.findViewById(R.id.dg_list_view)
    }

    fun runOnUiThread(action: Runnable) {
        (context as AppCompatActivity).runOnUiThread(action)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            KERNEL_SELECT_CODE ->
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    // Get the Uri of the selected file
                    val uri = data?.data
                    Log.d("CONFIG_SELECTOR", "File Uri: " + uri.toString())
                    try {
                        fileSelectedListener?.onFileSelected(File(PathUtil.getPath(context,uri)))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val SELECTED_BACKUP = 0
        const val SELECTED_RESTORE = 1
        const val SELECTED_INSTALL = 2
        private const val KERNEL_SELECT_CODE = 7267
    }


}
