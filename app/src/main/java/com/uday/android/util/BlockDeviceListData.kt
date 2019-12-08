package com.uday.android.util

import java.io.File
import com.uday.android.toolkit.R

class BlockDeviceListData {
    private var BLOCK_DEV: File? = null
    private var END_ADDR: Long = 0
    var endStr: String? = null
        private set
    private var SIZE: Long = 0
    var sizeStr: String? = null
        private set
    private var START_ADDR: Long = 0
    var startStr: String? = null
        private set
    private var NAME: String? = null
    private var TYPE: String? = null
    private var SIZE_UNIT: Int = 0

    init {
        SIZE_UNIT = R.id.unit_dynamic
    }

    fun setSizeUnit(unit: Int): BlockDeviceListData {
        this.SIZE_UNIT = unit
        setUnit()
        return this
    }

    private fun setUnit() {
        when (SIZE_UNIT) {
            R.id.unit_dynamic -> {
                sizeStr = Utils.getConventionalSize(SIZE)
                startStr = Utils.getConventionalSize(START_ADDR)
                endStr = Utils.getConventionalSize(END_ADDR)
            }
            R.id.unit_byte -> {
                sizeStr = "$SIZE B"
                startStr = "$START_ADDR B"
                endStr = "$END_ADDR B"
            }
            R.id.unit_kbyte -> {
                sizeStr = Utils.getSizeInKb(SIZE)
                startStr = Utils.getSizeInKb(START_ADDR)
                endStr = Utils.getSizeInKb(END_ADDR)
            }
            R.id.unit_mbyte -> {
                sizeStr = Utils.getSizeInMb(SIZE)
                startStr = Utils.getSizeInMb(START_ADDR)
                endStr = Utils.getSizeInMb(END_ADDR)
            }
            R.id.unit_gbyte -> {
                sizeStr = Utils.getSizeInGb(SIZE)
                startStr = Utils.getSizeInGb(START_ADDR)
                endStr = Utils.getSizeInGb(END_ADDR)
            }
        }
    }

    fun setName(name: String): BlockDeviceListData {
        this.NAME = name
        return this
    }

    fun setType(type: String): BlockDeviceListData {
        this.TYPE = type
        return this
    }

    fun setStart(start: Long): BlockDeviceListData {
        this.START_ADDR = start
        startStr = Utils.getConventionalSize(START_ADDR)
        return this
    }

    fun setEnd(end: Long): BlockDeviceListData {
        this.END_ADDR = end
        endStr = Utils.getConventionalSize(END_ADDR)
        return this
    }

    fun setSize(size: Long): BlockDeviceListData {
        this.SIZE = size
        sizeStr = Utils.getConventionalSize(SIZE)
        return this
    }

    fun setBlock(file: File): BlockDeviceListData {
        this.BLOCK_DEV = file
        return this
    }

    fun getName(): String? {
        return this.NAME
    }

    fun getType(): String? {
        return this.TYPE
    }

    fun getStart(): Long {
        return this.START_ADDR
    }

    fun getEnd(): Long {
        return this.END_ADDR
    }

    fun getSize(): Long {
        return this.SIZE
    }

    fun getBlock(): File? {
        return this.BLOCK_DEV
    }
}
