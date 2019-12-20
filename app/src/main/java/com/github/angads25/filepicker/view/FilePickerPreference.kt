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

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.preference.Preference
import android.util.AttributeSet
import android.view.View

import com.uday.android.toolkit.R
import com.github.angads25.filepicker.controller.DialogSelectionListener
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties

import java.io.File

/**
 *
 *
 * Created by angads25 on 15-07-2016.
 *
 */

class FilePickerPreference : Preference, DialogSelectionListener,
    Preference.OnPreferenceClickListener {
    private var mDialog: FilePickerDialog? = null
    private var properties: DialogProperties? = null
    private var titleText: String? = null

    constructor(context: Context) : super(context) {
        properties = DialogProperties()
        onPreferenceClickListener = this
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        properties = DialogProperties()
        initProperties(attrs)
        onPreferenceClickListener = this
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        properties = DialogProperties()
        initProperties(attrs)
        onPreferenceClickListener = this
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return super.onGetDefaultValue(a, index)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any) {
        super.onSetInitialValue(restorePersistedValue, defaultValue)
    }

    override fun onBindView(view: View) {
        super.onBindView(view)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        if (mDialog == null || !mDialog!!.isShowing) {
            return superState
        }

        val myState = SavedState(superState)
        myState.dialogBundle = mDialog!!.onSaveInstanceState()
        return myState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state == null || state !is SavedState) {
            super.onRestoreInstanceState(state)
            return
        }
        val myState = state as SavedState?
        super.onRestoreInstanceState(myState!!.superState)
        showDialog(myState.dialogBundle)
    }

    private fun showDialog(state: Bundle?) {
        mDialog = FilePickerDialog(context)
        setProperties(properties)
        mDialog!!.setDialogSelectionListener(this)
        if (state != null) {
            mDialog!!.onRestoreInstanceState(state)
        }
        mDialog!!.setTitle(titleText)
        mDialog!!.show()
    }

    override fun onSelectedFilePaths(files: Array<String>) {
        val buff = StringBuilder()
        for (path in files) {
            buff.append(path).append(":")
        }
        val dFiles = buff.toString()
        if (isPersistent) {
            persistString(dFiles)
        }
        try {
            onPreferenceChangeListener.onPreferenceChange(this, dFiles)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        showDialog(null)
        return false
    }

    fun setProperties(properties: DialogProperties?) {
        mDialog!!.setProperties(properties!!)
    }

    private class SavedState : Preference.BaseSavedState {
        internal var dialogBundle: Bundle? = null

        constructor(source: Parcel) : super(source) {
            dialogBundle = source.readBundle(javaClass.classLoader)
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeBundle(dialogBundle)
        }

        constructor(superState: Parcelable) : super(superState) {}

        companion object {

            @SuppressLint("ParcelCreator")
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    private fun initProperties(attrs: AttributeSet) {
        val tarr =
            context.theme.obtainStyledAttributes(attrs, R.styleable.FilePickerPreference, 0, 0)
        val N = tarr.indexCount
        for (i in 0 until N) {
            val attr = tarr.getIndex(i)
            if (attr == R.styleable.FilePickerPreference_selection_mode) {
                properties!!.selection_mode = tarr.getInteger(
                    R.styleable.FilePickerPreference_selection_mode,
                    DialogConfigs.SINGLE_MODE
                )
            } else if (attr == R.styleable.FilePickerPreference_selection_type) {
                properties!!.selection_type = tarr.getInteger(
                    R.styleable.FilePickerPreference_selection_type,
                    DialogConfigs.FILE_SELECT
                )
            } else if (attr == R.styleable.FilePickerPreference_root_dir) {
                val root_dir = tarr.getString(R.styleable.FilePickerPreference_root_dir)
                if (root_dir != null && root_dir != "") {
                    properties!!.root = File(root_dir)
                }
            } else if (attr == R.styleable.FilePickerPreference_error_dir) {
                val error_dir = tarr.getString(R.styleable.FilePickerPreference_error_dir)
                if (error_dir != null && error_dir != "") {
                    properties!!.error_dir = File(error_dir)
                }
            } else if (attr == R.styleable.FilePickerPreference_offset_dir) {
                val offset_dir = tarr.getString(R.styleable.FilePickerPreference_offset_dir)
                if (offset_dir != null && offset_dir != "") {
                    properties!!.offset = File(offset_dir)
                }
            } else if (attr == R.styleable.FilePickerPreference_extensions) {
                val extensions = tarr.getString(R.styleable.FilePickerPreference_extensions)
                if (extensions != null && extensions != "") {
                    properties!!.extensions =
                        extensions.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                            .toTypedArray()
                }
            } else if (attr == R.styleable.FilePickerPreference_title_text) {
                titleText = tarr.getString(R.styleable.FilePickerPreference_title_text)
            }
        }
        tarr.recycle()
    }
}
