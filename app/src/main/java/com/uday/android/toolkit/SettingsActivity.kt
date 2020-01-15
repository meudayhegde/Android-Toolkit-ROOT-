package com.uday.android.toolkit

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.toolkit.ui.DialogUtils
import com.uday.android.util.PathUtil
import com.uday.android.util.Utils
import java.io.File
import java.util.*


class SettingsActivity : AppCompatActivity() {

    private var mOption: Int = 0
    private var bootBlock: TextView? = null
    private var recoveryBlock: TextView? = null
    private var logoBlock: TextView? = null
    private var advancedLayout: LinearLayout? = null
    private lateinit var prefs2: SharedPreferences
    private lateinit var editor1: SharedPreferences.Editor
    private lateinit var editor2: SharedPreferences.Editor
    private var edtTxt: EditText? = null
    private var dialog: AlertDialog? = null
    private var fileSelectedListener : OnFileSelectedListener? = null

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        setContentView(R.layout.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        var prefs1: SharedPreferences = getSharedPreferences("block_devs", 0)
        editor1 = prefs1.edit()

        prefs2 = getSharedPreferences("general", 0)
        editor2 = prefs2.edit()


        fileSelectedListener = object:OnFileSelectedListener{
            override fun onFileSelected(file:File){
                editor2.putString("app_backup_dir", file.absolutePath)
                editor2.apply()
                setBackupDir()
            }
            override fun onMultipleFilesSelected(files:ArrayList<File>){}
        }

        //reboot dialog

        val rebootConfirm = findViewById<Switch>(R.id.confirm_reboot)
        val softReboot = findViewById<Switch>(R.id.allow_soft_reboot)
        rebootConfirm.isChecked = prefs2.getBoolean("show_reboot_confirm_dialog", true)
        softReboot.isChecked = prefs2.getBoolean("allow_soft_reboot", false)

        rebootConfirm.setOnCheckedChangeListener { _, p2 ->
            editor2.putBoolean("show_reboot_confirm_dialog", p2)
            editor2.apply()
        }

        softReboot.setOnCheckedChangeListener { _, p2 ->
            editor2.putBoolean("allow_soft_reboot", p2)
            editor2.apply()
        }

        //kernel

        val kernSwitch = findViewById<Switch>(R.id.allow_kern_inst)
        kernSwitch.isChecked = prefs2.getBoolean("allow_kern_installation", false)
        kernSwitch.setOnCheckedChangeListener { _, p2 ->
            editor2.putBoolean("allow_kern_installation", p2)
            editor2.apply()
        }

        //advanced

        val advancedSwitch = findViewById<Switch>(R.id.advanced_allow)
        advancedLayout = findViewById(R.id.advanced_layout)

        advancedSwitch.isChecked = prefs2.getBoolean("allow_advanced", false)
        if (advancedSwitch.isChecked)
            advancedLayout!!.visibility = View.VISIBLE
        else
            advancedLayout!!.visibility = View.GONE

        advancedSwitch.setOnCheckedChangeListener { _, p2 ->
            editor2.putBoolean("allow_advanced", p2)
            editor2.apply()
            if (p2) {
                advancedLayout!!.visibility = View.VISIBLE
                advancedLayout!!.startAnimation(
                    AnimationUtils.loadAnimation(
                        this@SettingsActivity,
                        R.anim.expand_to_down
                    )
                )
            } else {
                val anim =
                    AnimationUtils.loadAnimation(this@SettingsActivity, R.anim.shrink_to_top)
                anim!!.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationEnd(p1: Animation) {
                        advancedLayout!!.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(p1: Animation) {}
                    override fun onAnimationStart(p1: Animation) {}
                })
                advancedLayout!!.startAnimation(anim)
            }
        }

        setBackupDir()
        findViewById<LinearLayout>(R.id.set_app_bkp_layout).setOnClickListener {
            val intent = Intent(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent.ACTION_OPEN_DOCUMENT_TREE
            } else {
                Intent.ACTION_OPEN_DOCUMENT
            })

            try {
                startActivityForResult(
                    Intent.createChooser(intent, "Choose apps backup directory"), DIR_SELECT_CODE)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(
                    this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        prefs1 = getSharedPreferences("block_devs", 0)

        bootBlock = findViewById(R.id.boot_block_setting)
        val bootEdit = findViewById<LinearLayout>(R.id.boot_part_edit)

        recoveryBlock = findViewById(R.id.recovery_block_setting)
        val recEdit = findViewById<LinearLayout>(R.id.rec_part_edit)

        logoBlock = findViewById(R.id.logo_block_setting)
        val logoEdit = findViewById<LinearLayout>(R.id.logo_part_edit)


        bootBlock!!.text = prefs1.getString("boot", "please set this manually")
        recoveryBlock!!.text = prefs1.getString("recovery", "please set this manually")
        logoBlock!!.text = prefs1.getString("logo", "please set this manually")


        bootEdit.setOnClickListener {
            mOption = SELECTED_BOOT
            showDialog("boot block device")
            edtTxt!!.setText(bootBlock!!.text)
        }

        recEdit.setOnClickListener {
            mOption = SELECTED_RECOVERY
            showDialog("recovery block device")
            edtTxt!!.setText(recoveryBlock!!.text)
        }

        logoEdit.setOnClickListener {
            mOption = SELECTED_LOGO
            showDialog("boot logo block device")
            edtTxt!!.setText(logoBlock!!.text)
        }


        super.onCreate(savedInstanceState)
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data:Intent?) {
        when (requestCode) {
            DIR_SELECT_CODE ->
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    val uri = data?.data
                    Log.d("CONFIG_SELECTOR", "File Uri: " + uri.toString())
                    try {
                        val selected = File(PathUtil.getPath(this,uri!!))
                        fileSelectedListener?.onFileSelected(if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) selected else selected.parentFile)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun showDialog(title: String) {
        if (dialog == null) {
            edtTxt = DialogUtils.showEditTextDialog(this, title, "It may even lead to hard brick if wrong. do this only if you are so sure..!!",
                null, null, "save",
                object : DialogUtils.OnClickListener {
                    override fun onClick(p1: AlertDialog?) {
                        dialog = p1
                        if (File(edtTxt!!.text.toString()).exists()) {
                            when (mOption) {
                                SELECTED_BOOT -> if (!bootBlock!!.text.toString().equals(
                                        edtTxt!!.text.toString(),
                                        ignoreCase = true
                                    )
                                ) {
                                    editor1.putString("boot", edtTxt!!.text.toString())
                                    bootBlock!!.text = edtTxt!!.text
                                    CustomToast.showSuccessToast(
                                        this@SettingsActivity,
                                        "successfully saved dlock device detail",
                                        Toast.LENGTH_SHORT
                                    )
                                    dialog!!.cancel()
                                }
                                SELECTED_RECOVERY -> if (!recoveryBlock!!.text.toString().equals(
                                        edtTxt!!.text.toString(),
                                        ignoreCase = true
                                    )
                                ) {
                                    editor1.putString("recovery", edtTxt!!.text.toString())
                                    recoveryBlock!!.text = edtTxt!!.text
                                    CustomToast.showSuccessToast(
                                        this@SettingsActivity,
                                        "successfully saved dlock device detail",
                                        Toast.LENGTH_SHORT
                                    )
                                    dialog!!.cancel()
                                }
                                SELECTED_LOGO -> if (!logoBlock!!.text.toString().equals(
                                        edtTxt!!.text.toString(),
                                        ignoreCase = true
                                    )
                                ) {
                                    editor1.putString("logo", edtTxt!!.text.toString())
                                    logoBlock!!.text = edtTxt!!.text
                                    CustomToast.showSuccessToast(
                                        this@SettingsActivity,
                                        "successfully saved dlock device detail",
                                        Toast.LENGTH_SHORT
                                    )
                                    dialog!!.cancel()
                                }
                            }
                        } else {
                            CustomToast.showFailureToast(
                                this@SettingsActivity,
                                "this block device does not exists",
                                Toast.LENGTH_SHORT
                            )
                        }
                    }
                })
        } else {
            dialog!!.setTitle(title)
            dialog!!.show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {

        //backups
        var storages = arrayOf("Internal Storage")
        if (Utils.externalSdCard != null) {
            storages = arrayOf("Internal Storage", "External Storage")
        }
        val storageChoice = findViewById<Spinner>(R.id.settings_storage_choice)
        val spinnerAdapter = ArrayAdapter(
            this@SettingsActivity,
            android.R.layout.simple_spinner_dropdown_item,
            storages
        )
        storageChoice.adapter = spinnerAdapter
        if (prefs2.getInt("storage", 0) == 1) {
            if (Utils.externalSdCard != null)
                storageChoice.setSelection(1)
            else {
                storageChoice.setSelection(0)
                CustomToast.showFailureToast(
                    this,
                    "External storage not found\nseting internal storage as backup location",
                    Toast.LENGTH_SHORT
                )
            }
        } else {
            storageChoice.setSelection(0)
        }

        storageChoice.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p1: AdapterView<*>, p2: View, p3: Int, p4: Long) {
                editor2.putInt("storage", p3)
                editor2.apply()
            }

            override fun onNothingSelected(p1: AdapterView<*>) {}
        }

        super.onResume()
    }

    private fun setBackupDir() {
        (findViewById<TextView>(R.id.app_backup_dir)).text =
            prefs2.getString(
                "app_backup_dir",
                Environment.getExternalStorageDirectory().absolutePath + "/ToolKit/backups/apps"
            )
    }

    companion object {

        private const val SELECTED_BOOT = 0
        private const val SELECTED_RECOVERY = 1
        private const val SELECTED_LOGO = 2
        private const val DIR_SELECT_CODE = 7264
    }
}

interface OnFileSelectedListener{
    fun onFileSelected(file:File)
    fun onMultipleFilesSelected(files:ArrayList<File>)
}
