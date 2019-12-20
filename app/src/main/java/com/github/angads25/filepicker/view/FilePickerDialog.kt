/*
 * Copyright (C) 2016 Angad Singh
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.angads25.filepicker.view

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.github.angads25.filepicker.controller.DialogSelectionListener
import com.github.angads25.filepicker.controller.NotifyItemChecked
import com.github.angads25.filepicker.controller.adapters.FileListAdapter
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.model.FileListItem
import com.github.angads25.filepicker.model.MarkedItemList
import com.github.angads25.filepicker.utils.ExtensionFilter
import com.github.angads25.filepicker.utils.Utility
import com.github.angads25.filepicker.widget.MaterialCheckbox
import com.uday.android.toolkit.R
import java.io.File
import java.util.*

/**
 *
 *
 * Created by Angad Singh on 09-07-2016.
 *
 */

class FilePickerDialog : Dialog, AdapterView.OnItemClickListener {
    private var listView: ListView? = null
    private var dname: TextView? = null
    private var dir_path: TextView? = null
    private var title: TextView? = null
    private var properties: DialogProperties? = null
    private var callbacks: DialogSelectionListener? = null
    private var internalList: ArrayList<FileListItem>? = null
    private var filter: ExtensionFilter? = null
    private var mFileListAdapter: FileListAdapter? = null
    private var select: Button? = null
    private var titleStr: String? = null
    private var positiveBtnNameStr: String? = null
    private var negativeBtnNameStr: String? = null
    // Other methods- pre lolipop
    val externalSdCard: File?
        get() {
            var externalStorage: File? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val storage = File("/storage")

                if (storage.exists()) {
                    try {
                        val files = storage.listFiles()
                        for (file in files!!) {
                            if (file.exists() && file.canRead()) {
                                if (Environment.isExternalStorageRemovable(file)) {
                                    externalStorage = file
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("TAG", e.toString())
                    }

                }
            } else {
            }
            return externalStorage
        }

    constructor(context: Context) : super(context) {
        properties = DialogProperties()
        filter = ExtensionFilter(properties!!)
        internalList = ArrayList()
    }

    constructor(context: Context, properties: DialogProperties) : super(context) {
        this.properties = properties
        filter = ExtensionFilter(properties)
        internalList = ArrayList()
    }

    constructor(context: Context, properties: DialogProperties, themeResId: Int) : super(
        context, themeResId) {
        this.properties = properties
        filter = ExtensionFilter(properties)
        internalList = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_main)
        listView = findViewById(R.id.fileList) as ListView
        select = findViewById(R.id.select) as Button
        val size = MarkedItemList.fileCount
        if (size == 0) {
            select!!.isEnabled = false
            val color: Int
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                color = context!!.resources.getColor(R.color.colorAccent, context!!.theme)
            } else {
                color = context!!.resources.getColor(R.color.colorAccent)
            }
            select!!.setTextColor(
                Color.argb(
                    128,
                    Color.red(color),
                    Color.green(color),
                    Color.blue(color)
                )
            )
        }
        dname = findViewById(R.id.dname) as TextView
        title = findViewById(R.id.title) as TextView
        dir_path = findViewById(R.id.dir_path) as TextView
        val cancel = findViewById(R.id.cancel) as Button
        if (negativeBtnNameStr != null) {
            cancel.text = negativeBtnNameStr
        }
        select!!.setOnClickListener {
            /*  Select Button is clicked. Get the array of all selected items
         *  from MarkedItemList singleton.
         */
            val paths = MarkedItemList.selectedPaths
            //NullPointerException fixed in v1.0.2
            if (callbacks != null) {
                callbacks!!.onSelectedFilePaths(paths)
            }
            dismiss()
        }
        cancel.setOnClickListener { cancel() }


        val storage = findViewById(R.id.storage) as Button

        if (externalSdCard == null || !properties!!.hasStorageButton)
            storage.visibility = View.GONE
        storage.setOnClickListener(object : View.OnClickListener {
            @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
            override fun onClick(p1: View) {

                val popup = PopupMenu(context, storage)
                popup.menuInflater.inflate(R.menu.storage_button, popup.menu)
                popup.setOnMenuItemClickListener { items ->
                    if (items.itemId == R.id.extsd) {

                        if (externalSdCard!!.canRead()) {
                            val currLoc = externalSdCard
                            properties!!.root = currLoc!!
                            dname!!.text = currLoc!!.name
                            setTitle()
                            dir_path!!.text = currLoc.absolutePath
                            internalList!!.clear()
                            if (currLoc.name != properties!!.root.name) {
                                val parent = FileListItem()
                                parent.filename = context!!.getString(R.string.label_parent_dir)
                                parent.isDirectory = true
                                parent.location = currLoc.parentFile.absolutePath
                                parent.time = currLoc.lastModified()
                                internalList!!.add(parent)
                            }
                            internalList =
                                Utility.prepareFileListEntries(internalList!!, currLoc, filter!!)
                            mFileListAdapter!!.notifyDataSetChanged()
                        } else {
                            Toast.makeText(context, R.string.error_dir_access, Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        if (Environment.getExternalStorageDirectory().canRead()) {
                            val currLoc = Environment.getExternalStorageDirectory()
                            properties!!.root = currLoc
                            dname!!.text = currLoc.name
                            setTitle()
                            dir_path!!.text = currLoc.absolutePath
                            internalList!!.clear()
                            if (currLoc.name != properties!!.root.name) {
                                val parent = FileListItem()
                                parent.filename = context!!.getString(R.string.label_parent_dir)
                                parent.isDirectory = true
                                parent.location = currLoc.parentFile.absolutePath
                                parent.time = currLoc.lastModified()
                                internalList!!.add(parent)
                            }
                            internalList =
                                Utility.prepareFileListEntries(internalList!!, currLoc, filter!!)
                            mFileListAdapter!!.notifyDataSetChanged()
                        } else {
                            Toast.makeText(context, R.string.error_dir_access, Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                    true
                }
                popup.show()
            }
        })


        mFileListAdapter = FileListAdapter(internalList!!, context, properties!!)
        mFileListAdapter!!.notifyItemChecked= object : NotifyItemChecked{
            override fun notifyCheckBoxIsClicked() {
                /*  Handler function, called when a checkbox is checked ie. a file is
                 *  selected.
                 */
                positiveBtnNameStr = if (positiveBtnNameStr == null)
                    context!!.resources.getString(R.string.choose_button_label)
                else
                    positiveBtnNameStr
                val size = MarkedItemList.fileCount
                if (size == 0) {
                    select!!.isEnabled = false
                    val color: Int
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        color = context!!.resources.getColor(R.color.colorAccent, context!!.theme)
                    } else {
                        color = context!!.resources.getColor(R.color.colorAccent)
                    }
                    select!!.setTextColor(
                        Color.argb(
                            128,
                            Color.red(color),
                            Color.green(color),
                            Color.blue(color)
                        )
                    )
                    select!!.text = positiveBtnNameStr
                } else {
                    select!!.isEnabled = true
                    val color: Int
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        color = context!!.resources.getColor(R.color.colorAccent, context!!.theme)
                    } else {
                        color = context!!.resources.getColor(R.color.colorAccent)
                    }
                    select!!.setTextColor(color)
                    val button_label = "$positiveBtnNameStr ($size) "
                    select!!.text = button_label
                }
                if (properties!!.selection_mode == DialogConfigs.SINGLE_MODE) {
                    /*  If a single file has to be selected, clear the previously checked
                 *  checkbox from the list.
                 */
                    mFileListAdapter!!.notifyDataSetChanged()
                }
            }
        }
        listView!!.adapter = mFileListAdapter

        //Title method added in version 1.0.5
        setTitle()
    }

    private fun setTitle() {
        if (title == null || dname == null) {
            return
        }
        if (titleStr != null) {
            if (title!!.visibility == View.INVISIBLE) {
                title!!.visibility = View.VISIBLE
            }
            title!!.text = titleStr
            if (dname!!.visibility == View.VISIBLE) {
                dname!!.visibility = View.INVISIBLE
            }
        } else {
            if (title!!.visibility == View.VISIBLE) {
                title!!.visibility = View.INVISIBLE
            }
            if (dname!!.visibility == View.INVISIBLE) {
                dname!!.visibility = View.VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        positiveBtnNameStr = if (positiveBtnNameStr == null)
            context!!.resources.getString(R.string.choose_button_label)
        else
            positiveBtnNameStr
        select!!.text = positiveBtnNameStr
        if (Utility.checkStorageAccessPermissions(context)) {
            val currLoc: File
            internalList!!.clear()
            if (properties!!.offset.isDirectory && validateOffsetPath()) {
                currLoc = File(properties!!.offset.absolutePath)
                val parent = FileListItem()
                parent.filename = context!!.getString(R.string.label_parent_dir)
                parent.isDirectory = true
                parent.location = currLoc.parentFile.absolutePath
                parent.time = currLoc.lastModified()
                internalList!!.add(parent)
            } else if (properties!!.root.exists() && properties!!.root.isDirectory) {
                currLoc = File(properties!!.root.absolutePath)
            } else {
                currLoc = File(properties!!.error_dir.absolutePath)
            }
            dname!!.text = currLoc.name
            dir_path!!.text = currLoc.absolutePath
            setTitle()
            internalList = Utility.prepareFileListEntries(internalList!!, currLoc, filter!!)
            mFileListAdapter!!.notifyDataSetChanged()
            listView!!.onItemClickListener = this
        }
    }

    private fun validateOffsetPath(): Boolean {
        val offset_path = properties!!.offset.absolutePath
        val root_path = properties!!.root.absolutePath
        return offset_path != root_path && offset_path.contains(root_path)
    }

    override fun onItemClick(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
        if (internalList!!.size > i) {
            val fitem = internalList!![i]
            if (fitem.isDirectory) {
                if (File(fitem.location).canRead()) {
                    val currLoc = File(fitem.location)
                    dname!!.text = currLoc.name
                    setTitle()
                    dir_path!!.text = currLoc.absolutePath
                    internalList!!.clear()
                    if (currLoc.name != properties!!.root.name) {
                        val parent = FileListItem()
                        parent.filename = context!!.getString(R.string.label_parent_dir)
                        parent.isDirectory = true
                        parent.location = currLoc.parentFile.absolutePath
                        parent.time = currLoc.lastModified()
                        internalList!!.add(parent)
                    }
                    internalList = Utility.prepareFileListEntries(internalList!!, currLoc, filter!!)
                    mFileListAdapter!!.notifyDataSetChanged()
                } else {
                    Toast.makeText(context, R.string.error_dir_access, Toast.LENGTH_SHORT).show()
                }
            } else {
                val fmark = view.findViewById(R.id.file_mark) as MaterialCheckbox
                fmark.performClick()
            }
        }
    }

    fun getProperties(): DialogProperties? {
        return properties
    }

    fun setProperties(properties: DialogProperties) {
        this.properties = properties
        filter = ExtensionFilter(properties)
    }

    fun setDialogSelectionListener(callbacks: DialogSelectionListener) {
        this.callbacks = callbacks
    }

    override fun setTitle(titleStr: CharSequence?) {
        if (titleStr != null) {
            this.titleStr = titleStr.toString()
        } else {
            this.titleStr = null
        }
        setTitle()
    }

    fun setPositiveBtnName(positiveBtnNameStr: CharSequence?) {
        if (positiveBtnNameStr != null) {
            this.positiveBtnNameStr = positiveBtnNameStr.toString()
        } else {
            this.positiveBtnNameStr = null
        }
    }

    fun setNegativeBtnName(negativeBtnNameStr: CharSequence?) {
        if (negativeBtnNameStr != null) {
            this.negativeBtnNameStr = negativeBtnNameStr.toString()
        } else {
            this.negativeBtnNameStr = null
        }
    }

    fun markFiles(paths: List<String>?) {
        if (paths != null && paths.size > 0) {
            if (properties!!.selection_mode == DialogConfigs.SINGLE_MODE) {
                val temp = File(paths[0])
                when (properties!!.selection_type) {
                    DialogConfigs.DIR_SELECT -> if (temp.exists() && temp.isDirectory) {
                        val item = FileListItem()
                        item.filename = temp.name
                        item.isDirectory = temp.isDirectory
                        item.isMarked = true
                        item.time = temp.lastModified()
                        item.location = temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }

                    DialogConfigs.FILE_SELECT -> if (temp.exists() && temp.isFile) {
                        val item = FileListItem()
                        item.filename = temp.name
                        item.isDirectory = temp.isDirectory
                        item.isMarked = true
                        item.time = temp.lastModified()
                        item.location = temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }

                    DialogConfigs.FILE_AND_DIR_SELECT -> if (temp.exists()) {
                        val item = FileListItem()
                        item.filename = temp.name
                        item.isDirectory = temp.isDirectory
                        item.isMarked = true
                        item.time = temp.lastModified()
                        item.location = temp.absolutePath
                        MarkedItemList.addSelectedItem(item)
                    }
                }
            } else {
                for (path in paths) {
                    when (properties!!.selection_type) {
                        DialogConfigs.DIR_SELECT -> {
                            var temp = File(path)
                            if (temp.exists() && temp.isDirectory) {
                                val item = FileListItem()
                                item.filename = temp.name
                                item.isDirectory = temp.isDirectory
                                item.isMarked = true
                                item.time = temp.lastModified()
                                item.location = temp.absolutePath
                                MarkedItemList.addSelectedItem(item)
                            }
                        }

                        DialogConfigs.FILE_SELECT -> {
                            var temp = File(path)
                            if (temp.exists() && temp.isFile()) {
                                val item = FileListItem()
                                item.filename = temp.getName()
                                item.isDirectory = temp.isDirectory()
                                item.isMarked = true
                                item.time = temp.lastModified()
                                item.location = temp.getAbsolutePath()
                                MarkedItemList.addSelectedItem(item)
                            }
                        }

                        DialogConfigs.FILE_AND_DIR_SELECT -> {
                            var temp = File(path)
                            if (temp.exists() && (temp.isFile() || temp.isDirectory())) {
                                val item = FileListItem()
                                item.filename = temp.getName()
                                item.isDirectory = temp.isDirectory()
                                item.isMarked = true
                                item.time = temp.lastModified()
                                item.location = temp.getAbsolutePath()
                                MarkedItemList.addSelectedItem(item)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun show() {
        if (!Utility.checkStorageAccessPermissions(context)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                (context as AppCompatActivity).requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    EXTERNAL_READ_PERMISSION_GRANT
                )
            }
        } else {
            super.show()
            positiveBtnNameStr = if (positiveBtnNameStr == null)
                context.getString(R.string.choose_button_label)
            else
                positiveBtnNameStr
            select?.text = positiveBtnNameStr
            val size = MarkedItemList.fileCount
            if (size == 0) {
                select!!.text = positiveBtnNameStr
            } else {
                val button_label = "$positiveBtnNameStr ($size) "
                select!!.text = button_label
            }
        }
    }

    override fun onBackPressed() {
        //currentDirName is dependent on dname
        val currentDirName = dname!!.text.toString()
        if (internalList!!.size > 0) {
            val fitem = internalList!![0]
            val currLoc = File(fitem.location)
            if (currentDirName == properties!!.root.name || !currLoc.canRead()) {
                super.onBackPressed()
            } else {
                dname!!.text = currLoc.name
                dir_path!!.text = currLoc.absolutePath
                internalList!!.clear()
                if (currLoc.name != properties!!.root.name) {
                    val parent = FileListItem()
                    parent.filename = context!!.getString(R.string.label_parent_dir)
                    parent.isDirectory = true
                    parent.location = currLoc.parentFile.absolutePath
                    parent.time = currLoc.lastModified()
                    internalList!!.add(parent)
                }
                internalList = Utility.prepareFileListEntries(internalList!!, currLoc, filter!!)
                mFileListAdapter!!.notifyDataSetChanged()
            }
            setTitle()
        } else {
            super.onBackPressed()
        }
    }

    override fun dismiss() {
        MarkedItemList.clearSelectionList()
        internalList!!.clear()
        super.dismiss()
    }

    companion object {

        val EXTERNAL_READ_PERMISSION_GRANT = 112
    }
}
