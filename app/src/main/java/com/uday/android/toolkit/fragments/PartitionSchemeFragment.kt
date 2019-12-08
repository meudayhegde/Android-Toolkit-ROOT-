package com.uday.android.toolkit.fragments

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.util.Log
import android.view.*
import android.widget.*
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R
import com.uday.android.toolkit.ui.PartitionListAdapter
import com.uday.android.util.BlockDeviceListData
import com.uday.android.util.Utils
import java.io.*
import java.util.*

@SuppressLint("NewApi", "ValidFragment")
class PartitionSchemeFragment @SuppressLint("ValidFragment")
    constructor(private val context:Context): androidx.fragment.app.Fragment() {
    private var DISK:File? = null
    private var partitionListView:ListView? = null
    private var DISK_SIZE:Long = 0
    private var adapter:PartitionListAdapter? = null
    private var rootView:RelativeLayout? = null
    private var SECTOR_SIZE:String? = null
    private var STORAGE_MODEL:String? = null
    private var PARTITION_TABLE:String? = null
    private var partitionSwipeRefresh: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    private val partedParser:PartedParserRunnable
    private var headerView:View? = null
    private var mainBlockDevices:MutableList<String>? = null
    private var spinnerAdapter:ArrayAdapter<*>? = null
    private val blockDev0 = "/dev/block/mmcblk0"
    private val blockDev1 = "/dev/block/mmcblk1"

    init{
        partedParser = PartedParserRunnable()
    }

    override fun getContext():Context {
        return context
    }

    override fun onCreateView(inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle?):View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.partition_scheme, container, false) as RelativeLayout
            onViewFirstCreated()
        }
        rootView!!.startAnimation(MainActivity.mFadeIn)
        return rootView
    }

    private fun onViewFirstCreated() {
        partitionSwipeRefresh = rootView!!.findViewById(R.id.partition_swipe_refresh) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        partitionSwipeRefresh!!.setOnRefreshListener {
            partitionSwipeRefresh!!.isRefreshing = true
            refresh(1)
        }
        partitionListView = rootView!!.findViewById(R.id.partition_list_view) as ListView
        if (blockDevicesList == null) {
            partitionSwipeRefresh!!.isRefreshing = true
            blockDevicesList = ArrayList()
            newAdapter()
            refresh(0)
        } else newAdapter()

        mainBlockDevices = ArrayList()
        if (File(blockDev0).exists()) mainBlockDevices!!.add(blockDev0)
        if (File(blockDev1).exists()) mainBlockDevices!!.add(blockDev1)
            partitionListView!!.adapter = adapter
        headerView = (getContext() as AppCompatActivity).layoutInflater.inflate(R.layout.partition_view_header, null)
        spinnerAdapter = ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, mainBlockDevices!!)
        (headerView!!.findViewById(R.id.part_disk_spinner) as Spinner).adapter = spinnerAdapter
        headerView!!.visibility = View.GONE
        partitionListView!!.addHeaderView(headerView)
    }

    private fun newAdapter() {
        adapter = PartitionListAdapter(getContext(), R.layout.partition_list_item, blockDevicesList!!)
    }

    private fun refresh(type:Int) {
        blockDevicesList!!.clear()
        adapter!!.notifyDataSetChanged()
        if (!(getContext() as MainActivity).backgroundThreadisRunning)
            runInBackground(partedParser.setType(type))
        else
            Thread(partedParser).start()
    }

    private fun onLoadingCompleted(partedData:String) {
        blockDevicesList!!.clear()
        blockDevicesList!!.addAll(parseParted(partedData))
        runOnUiThread(Runnable {
            headerView!!.visibility = View.VISIBLE
            adapter!!.notifyDataSetChanged()
            partitionSwipeRefresh!!.isRefreshing = false
            setHasOptionsMenu(true)
            mainBlockDevices!!.removeAt(0)
            mainBlockDevices!!.add(0, blockDev0 + " : " + Utils.getConventionalSize(DISK_SIZE))
            spinnerAdapter!!.notifyDataSetChanged()
            (headerView!!.findViewById(R.id.part_table) as TextView).text = PARTITION_TABLE
            (headerView!!.findViewById(R.id.disk_model) as TextView).text = STORAGE_MODEL
            (headerView!!.findViewById(R.id.sector_size) as TextView).text = SECTOR_SIZE
        })
    }

    private fun runInBackground(action:Runnable) {
        (getContext() as MainActivity).runInBackground(action)
    }

    private fun runOnUiThread(action:Runnable) {
        (getContext() as MainActivity).runOnUiThread(action)
    }

    private fun writeToFile(data:String, fileName:String, context:Context) {
        val outputStream:FileOutputStream
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            outputStream.write(data.toByteArray())
            outputStream.close()
        }
        catch (ex:Exception) {
            Log.e(MainActivity.TAG, "File write failed: $ex")
        }
    }

    override fun onCreateOptionsMenu(menu:Menu, inflater:MenuInflater) {
        inflater.inflate(R.menu.menu_part_unit, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item:MenuItem):Boolean {
        for (data in blockDevicesList!!)
            data.setSizeUnit(item.itemId)
        adapter!!.notifyDataSetChanged()
        return super.onOptionsItemSelected(item)
    }

    private fun parseParted(str:String):ArrayList<BlockDeviceListData> {
        val devicesList = ArrayList<BlockDeviceListData>()
        for (tmp in str.split(("\n").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
            if (STORAGE_MODEL == null && tmp.contains("Model"))
            STORAGE_MODEL = tmp.replace("Model: ", "").replace("Model", "")
            else if (tmp.contains("Disk ")) {
                try {
                    DISK = File(tmp.split((":").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].split((" ").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1])
                    DISK_SIZE = java.lang.Long.parseLong(tmp.split((":").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].replace("B", "").replace(" ", ""))
                } catch (ex:NullPointerException) {
                    Log.e(MainActivity.TAG, ex.toString())
                }
            }
            else if (tmp.contains("Sector size")) {
                try {
                    SECTOR_SIZE = tmp.split((":").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].replace(" ", "")
                } catch (ex:NullPointerException) {
                    Log.e(MainActivity.TAG, ex.toString())
                }
            } else if (tmp.contains("Partition Table")) {
                try {
                    PARTITION_TABLE = tmp.split((":").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].replace(" ", "")
                } catch (ex:NullPointerException) {
                    Log.e(MainActivity.TAG, ex.toString())
                }
            }
            else if (tmp.contains("Number "))
                for (test in str.split((tmp).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1].split(("\n").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                    if (test.length > 10) {
                        var i = 0
                        val data = BlockDeviceListData()
                        for (another in test.split((" ").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
                        if (another != "") {
                            when (i) {
                                0 -> data.setBlock(File((DISK).toString() + "p" + Integer.parseInt(another)))
                                1 -> data.setStart(java.lang.Long.parseLong(another.split(("B").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toString()))
                                2 -> data.setEnd(java.lang.Long.parseLong(another.split(("B").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toString()))
                                3 -> data.setSize(java.lang.Long.parseLong(another.split(("B").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toString()))
                                else -> {
                                    try {
                                        if (data.getName() != null) data.setType(data.getName()!!)
                                        else data.setType("")
                                    } catch (ex:Exception) {
                                        data.setType("")
                                    }
                                    data.setName(another)
                                }
                            }
                            i++
                        }
                        if (data.getBlock() != null) devicesList.add(data)
                    }
        }
        return devicesList
    }

    private inner class PartedParserRunnable:Runnable {
        private var type = 0
        fun setType(type:Int):Runnable {
            this.type = type
            return this
        }

        override fun run() {
            val dataFile = File((getContext().getFilesDir()).toString() + "/partition_scheme.info")
            var scheme = ""
            if (dataFile.exists() && type == 0) {
                try {
                    val reader = BufferedReader(InputStreamReader(FileInputStream(dataFile)))
                    var tmp = reader.readLine()
                    while (tmp!= null) {
                        scheme += tmp + "\n"
                        tmp = reader.readLine()
                    }
                    onLoadingCompleted(scheme)
                } catch (ex:IOException) {
                    Log.e("Exception", "Failed to read " + dataFile.getAbsolutePath(), ex)
                }
            } else {
                if (dataFile.exists()) dataFile.delete()
                MainActivity.rootSession!!.addCommand((getContext().getFilesDir()).toString() + "/common/partition_scheme.sh " + MainActivity.TOOL + " 'b'", 2323) { commandcode, exitcode, output ->
                    writeToFile(Utils.getString(output), "partition_scheme.info", context)
                    onLoadingCompleted(Utils.getString(output))
                }
            }
        }
    }

    companion object {

        private var blockDevicesList:ArrayList<BlockDeviceListData>? = null
    }
}

