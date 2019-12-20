package com.uday.android.toolkit.fragments

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AbsListView

import com.github.angads25.filepicker.controller.DialogSelectionListener
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.view.FilePickerDialog
import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R
import com.uday.android.toolkit.ui.ApkListAdapter
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.toolkit.ui.SelectedDialog

import eu.chainfire.libsuperuser.Shell

import java.io.File
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

import android.view.View.OnClickListener
import com.uday.android.util.ApkListData
import com.uday.android.util.Utils

import android.text.*
import android.widget.AbsListView.OnScrollListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast

@SuppressLint("NewApi", "ValidFragment")
class BatchInstallerFragment(private val context:Context): androidx.fragment.app.Fragment() {

    private val instDialog: AlertDialog
    private var adapter:ApkListAdapter? = null
    private val apkFiles:ArrayList<ApkListData>
    private var nameComparator:Comparator<ApkListData>? = null
    private var sizeComparator:Comparator<ApkListData>? = null
    private var dateComparator:Comparator<ApkListData>? = null
    private var fileNameComparator:Comparator<ApkListData>? = null
    private var pathComparator:Comparator<ApkListData>? = null
    private val properties:DialogProperties
    private val icAppDefault:Drawable
    private var filePicker:FilePickerDialog? = null
    private var instFab:FloatingActionButton? = null
    private var addCustomApk:FloatingActionButton? = null
    private var delFab:FloatingActionButton? = null
    private var menuFab:FloatingActionMenu? = null
    private var addInternal:FloatingActionButton? = null
    private var addExternal:FloatingActionButton? = null
    private var addCustom:FloatingActionButton? = null
    private var n = 0
    private var i:Int = 0
    private var chkdCount = 0
    private var countToInstall:Int = 0
    private var countOfInstalled:Int = 0
    private var mPreviousVisibleItem:Int = 0
    private var scrollState:Int = 0
    private var sortLayout:LinearLayout? = null
    private var myApkListView:ListView? = null
    private val pm:PackageManager
    private val instProg:ProgressDialog
    private val instBar:ProgressBar
    private var rootView:RelativeLayout? = null
    private var search: SearchView? = null
    private var sorter:Spinner? = null
    private var query:String? = null
    private var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    private val instMsg:TextView
    private val apkCount:TextView
    private val apkPercantage:TextView
    private var chkdInfoTotal:TextView? = null
    private var chkdInfoSelected:TextView? = null

    private var refreshFinished:Boolean = false

    init{

        rootSession = MainActivity.rootSession

        properties = DialogProperties()
        properties.selection_mode = DialogConfigs.MULTI_MODE
        properties.selection_type = DialogConfigs.DIR_SELECT
        properties.root = Environment.getExternalStorageDirectory()
        properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
        properties.offset = File(DialogConfigs.DEFAULT_DIR)
        properties.hasStorageButton = true

        pm = context.getPackageManager()
        icAppDefault = context.getResources().getDrawable(R.drawable.ic_app_default)

        apkFiles = ArrayList()
        refresh()

        instProg = ProgressDialog(getContext())
        instProg.window!!.attributes.windowAnimations = R.style.DialogTheme
        instProg.setTitle("Loading")
        instProg.setMessage("Searching for apk files.\nplease wait...")
        instProg.setCancelable(false)

        val layout = (context as AppCompatActivity).layoutInflater.inflate(R.layout.apk_inst_dialog, null) as RelativeLayout
        instMsg = layout.findViewById(R.id.apk_progress_name) as TextView
        instBar = layout.findViewById(R.id.apk_progress) as ProgressBar
        apkPercantage = layout.findViewById(R.id.apk_percentage) as TextView
        apkCount = layout.findViewById(R.id.apk_count) as TextView
        instDialog = AlertDialog.Builder(context)
        .setTitle("Installing ")
        .setIcon(R.drawable.ic_app_default)
        .setView(layout)
        .create()
        instDialog.window!!.attributes.windowAnimations = R.style.DialogTheme
        instDialog.setCancelable(false)

        setComparators()

    }

    override fun getContext():Context {
        return context
    }

    override fun onResume() {
        if (!menuFab!!.isOpened && !menuFab!!.isMenuButtonHidden) {
            menuFab!!.hideMenuButton(false)
            Handler().postDelayed({ menuFab!!.showMenuButton(true) }, 400)
        }
        super.onResume()
    }


    override fun onCreateView(inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle?):View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.batch_installer, container, false) as RelativeLayout
            onViewFirstCreated()
        }
        rootView!!.startAnimation(MainActivity.mFadeIn)
        return rootView
    }

    override fun onCreateOptionsMenu(menu:Menu, inflater:MenuInflater) {
        inflater.inflate(R.menu.check_all, menu)
        search = menu.findItem(R.id.action_search).actionView as SearchView
        search!!.queryHint = "type to search..."
        search!!.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText:String):Boolean {
                query = newText
                adapter!!.filter(query!!)
                return true
            }

            override fun onQueryTextSubmit(txt:String):Boolean {
            return false
            }
        })

        val allToggle = menu.findItem(R.id.check_all_chbx).actionView as CheckBox

        val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        val colors = intArrayOf(Color.WHITE, Color.WHITE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            allToggle.buttonTintList = ColorStateList(states, colors)
        allToggle.setPadding(10, 0, 10, 0)

        allToggle.setOnCheckedChangeListener { p1, p2 ->
            for (apkFile in apkFiles) {
                if (apkFile.isSelectable)
                    apkFile.isSelected = p2
            }
            adapter!!.notifyDataSetChanged()
        }


        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item:MenuItem):Boolean {
        when (item.itemId) {
            R.id.toggle_all_chbx -> {
                    for (apkFile in apkFiles) {
                        if (apkFile.isSelectable)
                            apkFile.isSelected = !apkFile.isSelected
                    }
                adapter!!.notifyDataSetChanged()
            }
            R.id.check_not_installed -> {
                for (apkFile in apkFiles) {
                    if (apkFile.isSelectable) {
                        apkFile.isSelected = !apkFile.isInstalled
                    }
                }
                adapter!!.notifyDataSetChanged()
            }

            R.id.check_old -> {
                for (apkFile in apkFiles) {
                    apkFile.isSelected = apkFile.isOld
                }
                adapter!!.notifyDataSetChanged()
            }
            R.id.check_updatable -> {
                for (apkFile in apkFiles) {
                    apkFile.isSelected = apkFile.isInstalled && !apkFile.isInstalledVer && !apkFile.isOld
                }
                adapter!!.notifyDataSetChanged()
            }
            R.id.clrscr -> (context as MainActivity).refreshApkScreen()
        }
        return true
    }

    private fun refresh() {

        val swipeRefresh = Runnable {
            if (swipeRefreshLayout != null && !swipeRefreshLayout!!.isRefreshing) {
                swipeRefreshLayout!!.isRefreshing = true
                sortLayout!!.visibility = View.GONE
            }
        }

        if (apkFilesOrig == null || apkFilesOrig!!.isEmpty()) {
            apkFilesOrig = ArrayList()
            if (swipeRefreshLayout != null)
                swipeRefreshLayout!!.isRefreshing = false
        } else {
            refreshFinished = false
            setHasOptionsMenu(false)
            object:Thread() {
                override fun run() {
                    while ((getContext() as MainActivity).backgroundThreadisRunning || !refreshFinished) {
                        runOnUiThread(swipeRefresh)
                    }
                    runOnUiThread(Runnable {
                        if (swipeRefreshLayout != null && swipeRefreshLayout!!.isRefreshing) {
                            swipeRefreshLayout!!.isRefreshing = false
                            sortLayout!!.visibility = View.VISIBLE
                            setHasOptionsMenu(true)
                        }
                    })
                }
            }.start()
            for (data in apkFilesOrig!!) {
                if (!data.apkFile.exists()) {
                    apkFilesOrig!!.remove(data)
                    if (data.isSelected)
                    chkdCount--
                } else {
                    if (!data.isSelectable) data.add()
                    if (data.isSelected) chkdCount++
                }
            }
            refreshFinished = true
        }
        apkFiles.clear()
        apkFiles.addAll(apkFilesOrig!!)
        if (search != null && query != null && !TextUtils.isEmpty(query))
        adapter!!.filter(query!!)
    }

    private fun setComparators() {
        nameComparator = Comparator<ApkListData> { p1, p2 -> p1.NAME.compareTo(p2.NAME, ignoreCase = true) }
        sizeComparator = Comparator<ApkListData> { p1, p2 -> (p1.SIZE_LONG - p2.SIZE_LONG).toInt() }
        dateComparator = Comparator<ApkListData> { p1, p2 -> (p1.apkFile.lastModified() - p2.apkFile.lastModified()).toInt() }
        fileNameComparator = Comparator<ApkListData> { p1, p2 -> p1.apkFile.name.compareTo(p2.apkFile.name, ignoreCase = true) }
        pathComparator = Comparator<ApkListData> { p1, p2 -> p1.PATH.compareTo(p2.PATH, ignoreCase = true) }
    }

    private fun onViewFirstCreated() {
        swipeRefreshLayout = rootView!!.findViewById(R.id.apk_swipe_refresh) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        swipeRefreshLayout!!.setOnRefreshListener { refresh() }
        sortLayout = rootView!!.findViewById(R.id.layout_sort) as LinearLayout
        sorter = rootView!!.findViewById(R.id.sort_spinner) as Spinner
        sorter!!.adapter = ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, arrayOf("App Name", "File Name", "Size", "Date Modified"))
        sorter!!.onItemSelectedListener = object:AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p1:AdapterView<*>, p2:View, p3:Int, p4:Long) {
                sort(p3)
            }
            override fun onNothingSelected(p1:AdapterView<*>) {}
        }

        myApkListView = rootView!!.findViewById(R.id.apk_list_view) as ListView
        menuFab = rootView!!.findViewById(R.id.menu_batch_app)
        menuFab!!.isVerticalScrollBarEnabled = true
        chkdInfoTotal = rootView!!.findViewById(R.id.batch_info_total) as TextView
        chkdInfoSelected = rootView!!.findViewById(R.id.batch_info_selected) as TextView
        menuFab!!.setClosedOnTouchOutside(true)
        adapter = ApkListAdapter(this, R.layout.apk_list_item, apkFiles)
        myApkListView!!.adapter = adapter
        myApkListView!!.setOnScrollListener(object:OnScrollListener {
            override fun onScrollStateChanged(view:AbsListView, scrollState:Int) {
                this@BatchInstallerFragment.scrollState = scrollState
                if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                    Handler().postDelayed({
                        if (menuFab!!.isMenuButtonHidden && this@BatchInstallerFragment.scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                            menuFab!!.showMenuButton(true)
                        }
                    }, 400)

                }
            }

            override fun onScroll(view:AbsListView, firstVisibleItem:Int, visibleItemCount:Int, totalItemCount:Int) {
                if (firstVisibleItem > mPreviousVisibleItem)
                    menuFab!!.hideMenuButton(true)
                else if (firstVisibleItem < mPreviousVisibleItem)
                    menuFab!!.showMenuButton(true)
                mPreviousVisibleItem = firstVisibleItem
            }
        })

        addInternal = menuFab!!.findViewById(R.id.internal) as FloatingActionButton
        addExternal = menuFab!!.findViewById(R.id.external) as FloatingActionButton
        addCustom = menuFab!!.findViewById(R.id.custom) as FloatingActionButton
        addCustomApk = menuFab!!.findViewById(R.id.custom_apk) as FloatingActionButton

        addInternal!!.setOnClickListener(InstClickListener())
        addExternal!!.setOnClickListener(ExtClickListener())
        addCustom!!.setOnClickListener(CustomClickListener())
        addCustomApk!!.setOnClickListener(CustomApk())

        chkdInfoSelected!!.setOnClickListener(SelectedDialog(this, adapter!!))
        if (!apkFiles.isEmpty()) {
            chkdInfoTotal!!.text = Html.fromHtml("Total :  <b><font color=" + '"'.toString() + "blue" + '"'.toString() + ">" + apkFiles.size + "</font></b>")
            sortLayout!!.visibility = View.VISIBLE
            chkdInfoSelected!!.visibility = View.VISIBLE
        }
    }


    private fun searchForApks(mDir:File?) {
        try {
            val samp = mDir!!.listFiles()
            for (tmp in samp!!)
                if (!tmp.isDirectory && tmp.name.endsWith(".apk"))
                    addIntoList(ApkListData(context!!, tmp, pm, icAppDefault))
            for (tmp in samp!!)
                if (tmp.isDirectory)
                    searchForApks(tmp)
        } catch (ex:NullPointerException) {
            Log.e(MainActivity.TAG, ex.toString())
        }

    }


    private fun addIntoList(apkListData:ApkListData) {
        if (Collections.binarySearch(apkFilesOrig!!, apkListData, pathComparator) < 0) {
            apkListData.add()
            apkFilesOrig!!.add(apkListData)
            n++
        }
    }

    private fun sort(method:Int) {
        SORTING_SELECTED = method
        when (SORTING_SELECTED) {
            SORT_BY_NAME -> {
                Collections.sort(apkFilesOrig!!, nameComparator)
                Collections.sort(apkFiles, nameComparator)
            }
            SORT_BY_FILE_NAME -> {
                Collections.sort(apkFilesOrig!!, fileNameComparator)
                Collections.sort(apkFiles, fileNameComparator)
            }
            SORT_BY_SIZE -> {
                Collections.sort(apkFilesOrig!!, sizeComparator)
                Collections.sort(apkFiles, sizeComparator)
            }
            SORT_BY_DATE -> {
                Collections.sort(apkFilesOrig!!, dateComparator)
                Collections.sort(apkFiles, dateComparator)
            }
            else -> sort(SORT_BY_NAME)
        }
        adapter!!.notifyDataSetChanged()
    }

    private fun setApkStatus() {
        for (apkListData in apkFilesOrig!!) {
            i = 0
            while (i < n) {
                if (apkFilesOrig!![i].PACKAGE_NAME.compareTo(apkListData.PACKAGE_NAME, ignoreCase = true) == 0 && !apkListData.isInstalled && apkFilesOrig!![i].VERSION_CODE > apkListData.VERSION_CODE)
                apkListData.isOld = true
                i++
            }
        }
    }

    private fun install(position:Int) {
        try {
            if (apkFilesOrig!![position].isSelected) {
                if (!instDialog.isShowing) {
                    instBar.max = countToInstall
                    instDialog.show()
                }
                countOfInstalled++
                instDialog.setIcon(apkFiles[position].ICON)
                    instBar.progress = countOfInstalled
                    instMsg.text = getString(R.string.installing) + apkFiles.get(position).NAME + " " + apkFiles.get(position).VERSION_NAME
                apkCount.setText((countOfInstalled).toString() + " / " + countToInstall)
                apkPercantage.setText((countOfInstalled * 100 / countToInstall).toString() + " %")

                rootSession!!.addCommand("pm install -rd " + '"'.toString() + apkFiles[position].PATH + '"'.toString(), position) { comandcode, exitcode, output ->
                    val outStr = apkFiles[comandcode].NAME + "_" + apkFiles[comandcode].VERSION_NAME + " : " + Utils.getString(output)
                    Log.d(MainActivity.TAG, outStr)
                    runOnUiThread(Runnable {
                        if (exitcode == 0) {
                            CustomToast.showSuccessToast(context, outStr, Toast.LENGTH_SHORT)
                            apkFiles[comandcode].isInstalled = true
                            apkFiles[comandcode].titleColor = Color.rgb(0, 202, 0)
                            apkFiles[comandcode].isInstalledVer = true
                            for (data in apkFiles) {
                                if (data.PACKAGE_NAME.equals(apkFiles[comandcode].PACKAGE_NAME, ignoreCase = true) && data.VERSION_CODE < apkFiles[comandcode].VERSION_CODE) {

                                    data.isInstalledVer = false
                                    data.isOld = true
                                }
                            }
                        } else {
                            CustomToast.showFailureToast(context, outStr, Toast.LENGTH_SHORT)
                            apkFiles[comandcode].isInstalled = false
                            apkFiles[comandcode].titleColor = Color.rgb(255, 25, 0)
                        }
                        install(comandcode + 1)
                    })
                }
            }
            else
            install(position + 1)
        }
        catch (ex:Exception) {
            instDialog.cancel()
            setHasOptionsMenu(true)
            adapter!!.notifyDataSetChanged()
        }

    }


    fun runOnUiThread(run:Runnable) {
        (context as AppCompatActivity).runOnUiThread(run)
    }

    private fun beforeApkSearch() {
        setHasOptionsMenu(false)
        menuFab!!.close(true)
        menuFab!!.hideMenuButton(true)
        instProg.show()
    }


    private fun delApk() {
        val delList = ArrayList<ApkListData>()
        for (apkFile in apkFilesOrig!!) {
            if (apkFile.isSelected)
            delList.add(apkFile)
        }
        if (delList.size == 0)
            CustomToast.showNotifyToast(context, "no apk file selected for deletion", Toast.LENGTH_SHORT)
        else {
            AlertDialog.Builder(context)
            .setTitle("Delete apk files")
            .setMessage(("This will delete all the selected apk files (" + delList.size + ") from the storage.\n"
            + "and this action can not be undone"))
            .setNegativeButton("cancel", object:DialogInterface.OnClickListener {
                override fun onClick(p1:DialogInterface, p2:Int) {
                p1.cancel()
                }
            })
            .setPositiveButton("delete", object:DialogInterface.OnClickListener {
                override fun onClick(p1:DialogInterface, p2:Int) {
                    var dellist = ""
                    for (listData in delList)
                    dellist = dellist + " " + '"'.toString() + listData.PATH + '"'.toString()
                    rootSession!!.addCommand(MainActivity.TOOL + " rm " + dellist, 4323, object:Shell.OnCommandResultListener {
                        override fun onCommandResult(commandcode:Int, resultcode:Int, output:List<String>) {
                            runOnUiThread(object:Runnable {
                                override fun run() {
                                    for (list in delList) {
                                        if (!list.apkFile.exists()) {
                                            apkFiles.remove(list)
                                            apkFilesOrig!!.remove(list)
                                        }
                                    }
                                    adapter!!.notifyDataSetChanged()
                                }
                            })
                        }
                    })
                }
            })
            .show()

        }
    }

    private fun OnApkSearchCompleted() {
        swipeRefreshLayout!!.isRefreshing = true
        sortLayout!!.visibility = View.GONE
        chkdInfoTotal!!.text = Html.fromHtml("Total :  <b><font color=" + '"'.toString() + "blue" + '"'.toString() + ">" + apkFilesOrig!!.size + "</font></b>")
        object:Thread() {
            override fun run() {
                while ((context as MainActivity).backgroundThreadisRunning) {
                    try {
                        Thread.sleep(1000)
                    }
                    catch (e:InterruptedException) {}
                    finally {
                        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE)
                        runOnUiThread(object:Runnable {
                            override fun run() {
                                adapter!!.notifyDataSetChanged()
                            }
                        })
                    }
                }
                runOnUiThread(object:Runnable {
                    override fun run() {
                        apkFiles.clear()
                        setApkStatus()
                        sort(SORTING_SELECTED)
                        swipeRefreshLayout!!.isRefreshing = false
                        sortLayout!!.visibility = View.VISIBLE
                        apkFiles.addAll(apkFilesOrig!!)
                    }
                })
            }
        }.start()



        setHasOptionsMenu(true)
        menuFab!!.showMenuButton(false)
        apkFiles.addAll(apkFilesOrig!!)
        adapter!!.notifyDataSetChanged()
        instProg.cancel()

        if (instFab == null && !apkFiles.isEmpty()) {
            instFab = FloatingActionButton(context!!)
            instFab!!.buttonSize = FloatingActionButton.SIZE_MINI
            instFab!!.setColorNormalResId(R.color.greenFabNormal)
            instFab!!.setColorPressedResId(R.color.greenFabPressed)
            instFab!!.setColorRippleResId(R.color.greenFabRipple)
            instFab!!.setImageResource(R.drawable.ic_install)
            instFab!!.labelText = "Install selected apks"
            instFab!!.setOnClickListener(object:OnClickListener {
                override fun onClick(p1:View) {
                    countToInstall = 0
                    menuFab!!.close(true)
                    for (tmp in apkFilesOrig!!)
                    if (tmp.isSelected)
                    countToInstall++
                    if (countToInstall > 0) {
                        countOfInstalled = 0
                        menuFab!!.hideMenuButton(true)
                        setHasOptionsMenu(false)
                        install(0)
                    }
                    else CustomToast.showNotifyToast(context, "no apk file is selected for installation", Toast.LENGTH_SHORT)
                }
            })
            menuFab!!.addMenuButton(instFab!!)
            delFab = FloatingActionButton(context!!)
            delFab!!.buttonSize = FloatingActionButton.SIZE_MINI
            delFab!!.setImageResource(R.drawable.ic_del)
            delFab!!.labelText = "delete selected apk files"
            delFab!!.setOnClickListener(object:OnClickListener {
                override fun onClick(p1:View) {
                    delApk()
                    menuFab!!.close(true)
                }
            })
            menuFab!!.addMenuButton(delFab!!)
        }
    }

    fun onChecked() {
        chkdCount++
        chkdInfoSelected!!.text = Html.fromHtml("selected :  <b><font color=\"blue\">$chkdCount</font></b>")
    }

    fun onUnchecked() {
        chkdCount--
        chkdInfoSelected!!.text = Html.fromHtml("selected :  <b><font color=\"blue\">$chkdCount</font></b>")
    }

    fun onAdapterNotified() {
        if (!(context as MainActivity).backgroundThreadisRunning) {
            chkdInfoTotal!!.text = Html.fromHtml("Total :  <b><font color=" + '"'.toString() + "blue" + '"'.toString() + ">" + apkFiles.size + "</font></b>")
            chkdCount = 0
            for (data in apkFilesOrig!!)
            if (data.isSelected) chkdCount++
            chkdInfoSelected!!.text = Html.fromHtml("selected :  <b><font color=\"blue\">$chkdCount</font></b>")
        }
    }

    private inner class InstClickListener: OnClickListener {
        override fun onClick(p1:View) {
            beforeApkSearch()
            Thread(Runnable {
                searchForApks(Environment.getExternalStorageDirectory())
                runOnUiThread(Runnable {
                    menuFab!!.removeMenuButton(addInternal!!)
                    OnApkSearchCompleted()
                })
            }).start()
        }
    }

    private inner class ExtClickListener:OnClickListener {
        override fun onClick(p1:View) {
            if (Utils.externalSdCard == null)
            Toast.makeText(context, "External storage not inserted...!!", Toast.LENGTH_SHORT).show()
            else {
                beforeApkSearch()

                object:Thread() {
                    override fun run() {
                        searchForApks(Utils.externalSdCard)
                        runOnUiThread(Runnable {
                            menuFab!!.removeMenuButton(addExternal!!)
                            Handler().postAtTime({ OnApkSearchCompleted() }, 500)
                        })
                    }
                }.start()
            }
        }
    }

    private inner class CustomClickListener:OnClickListener {
        override fun onClick(p1:View) {
            menuFab!!.close(true)
            properties.selection_type = DialogConfigs.DIR_SELECT
            filePicker = FilePickerDialog(context, properties, R.style.AppTheme)
            filePicker!!.setDialogSelectionListener(object:DialogSelectionListener {
                override fun onSelectedFilePaths(files:Array<String>) {
                    beforeApkSearch()
                    menuFab!!.hideMenuButton(false)
                    object:Thread() {
                        override fun run() {
                            for (tmp in files)
                                searchForApks(File(tmp))
                            runOnUiThread(Runnable { OnApkSearchCompleted() })
                        }
                    }.start()
                }
            })
            filePicker!!.show()
            filePicker!!.window!!.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT)
        }
    }

    private inner class CustomApk: OnClickListener {
        override fun onClick(p1:View) {
            menuFab!!.close(true)
            properties.extensions = arrayOf(".apk")
            properties.selection_mode = DialogConfigs.MULTI_MODE
            properties.selection_type = DialogConfigs.FILE_SELECT
            val fpDiag = FilePickerDialog(context, properties, R.style.AppTheme)
            fpDiag.setDialogSelectionListener(object:DialogSelectionListener {
                override fun onSelectedFilePaths(files:Array<String>) {
                    beforeApkSearch()
                    for (apk in files) {
                        addIntoList(ApkListData(context, File(apk), pm, icAppDefault).add().setOnAddedListener(object:ApkListData.OnAddedListener {
                            override fun onAdded() {
                                adapter!!.notifyDataSetChanged()
                            }
                        }))
                    }
                    OnApkSearchCompleted()
                }
            })
            fpDiag.show()
        }
    }

    companion object {
        var rootSession:Shell.Interactive? = null
        var apkFilesOrig:ArrayList<ApkListData>? = null

        private val SORT_BY_NAME = 0
        private val SORT_BY_FILE_NAME = 1
        private val SORT_BY_SIZE = 2
        private val SORT_BY_DATE = 3

        private var SORTING_SELECTED:Int = 0
    }

}
