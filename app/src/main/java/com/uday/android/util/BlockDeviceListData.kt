package com.uday.android.util

import com.uday.android.toolkit.R
import java.io.File

class BlockDeviceListData {
    private var blockDev: File? = null
    private var endAddr: Long = 0
    var endStr: String? = null
        private set
    private var size: Long = 0
    var sizeStr: String? = null
        private set
    private var startAddr: Long = 0
    var startStr: String? = null
        private set
    private var name: String? = null
    private var type: String? = null
    private var sizeUnit: Int = 0

    init {
        sizeUnit = R.id.unit_dynamic
    }

    fun setSizeUnit(unit: Int): BlockDeviceListData {
        this.sizeUnit = unit
        setUnit()
        return this
    }

    private fun setUnit() {
        when (sizeUnit) {
            R.id.unit_dynamic -> {
                sizeStr = Utils.getConventionalSize(size)
                startStr = Utils.getConventionalSize(startAddr)
                endStr = Utils.getConventionalSize(endAddr)
            }
            R.id.unit_byte -> {
                sizeStr = "$size B"
                startStr = "$startAddr B"
                endStr = "$endAddr B"
            }
            R.id.unit_kbyte -> {
                sizeStr = Utils.getSizeInKb(size)
                startStr = Utils.getSizeInKb(startAddr)
                endStr = Utils.getSizeInKb(endAddr)
            }
            R.id.unit_mbyte -> {
                sizeStr = Utils.getSizeInMb(size)
                startStr = Utils.getSizeInMb(startAddr)
                endStr = Utils.getSizeInMb(endAddr)
            }
            R.id.unit_gbyte -> {
                sizeStr = Utils.getSizeInGb(size)
                startStr = Utils.getSizeInGb(startAddr)
                endStr = Utils.getSizeInGb(endAddr)
            }
        }
    }

    fun setName(name: String): BlockDeviceListData {
        this.name = name
        return this
    }

    fun setType(type: String): BlockDeviceListData {
        this.type = type
        return this
    }

    fun setStart(start: Long): BlockDeviceListData {
        this.startAddr = start
        startStr = Utils.getConventionalSize(startAddr)
        return this
    }

    fun setEnd(end: Long): BlockDeviceListData {
        this.endAddr = end
        endStr = Utils.getConventionalSize(endAddr)
        return this
    }

    fun setSize(size: Long): BlockDeviceListData {
        this.size = size
        sizeStr = Utils.getConventionalSize(this.size)
        return this
    }

    fun setBlock(file: File): BlockDeviceListData {
        this.blockDev = file
        return this
    }

    fun getName(): String? {
        return this.name
    }

    fun getBlock(): File? {
        return this.blockDev
    }
}
