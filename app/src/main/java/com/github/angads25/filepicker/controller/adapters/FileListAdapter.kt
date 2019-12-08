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

package com.github.angads25.filepicker.controller.adapters

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView

import com.uday.android.toolkit.R
import com.github.angads25.filepicker.controller.NotifyItemChecked
import com.github.angads25.filepicker.model.DialogConfigs
import com.github.angads25.filepicker.model.DialogProperties
import com.github.angads25.filepicker.model.FileListItem
import com.github.angads25.filepicker.model.MarkedItemList
import com.github.angads25.filepicker.widget.MaterialCheckbox
import com.github.angads25.filepicker.widget.OnCheckedChangeListener

import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Date
import java.util.Locale

/* <p>
 * Created by Angad Singh on 09-07-2016.
 * </p>
 */

/**
 * Adapter Class that extends [BaseAdapter] that is
 * used to populate [ListView] with file info.
 */
class FileListAdapter(private val listItem: ArrayList<FileListItem>, private val context: Context, private val properties: DialogProperties) : BaseAdapter() {
    var notifyItemChecked: NotifyItemChecked? = null

    override fun getCount(): Int {
        return listItem.size
    }

    override fun getItem(i: Int): FileListItem {
        return listItem[i]
    }

    override fun getItemId(i: Int): Long {
        return i.toLong()
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
        var view = view
        val holder: ViewHolder
        if (view == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_file_list_item, viewGroup, false)
            holder = ViewHolder(view!!)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        val item = listItem[i]
        if (MarkedItemList.hasItem(item.location!!)) {
            val animation = AnimationUtils.loadAnimation(context, R.anim.marked_item_animation)
            view.animation = animation
        } else {
            val animation = AnimationUtils.loadAnimation(context, R.anim.unmarked_item_animation)
            view.animation = animation
        }
        if (item.isDirectory) {
            holder.type_icon.setImageResource(R.mipmap.ic_type_folder)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.type_icon.setColorFilter(
                    context.resources.getColor(
                        R.color.colorPrimary,
                        context.theme
                    )
                )
            } else {
                holder.type_icon.setColorFilter(context.resources.getColor(R.color.colorPrimary))
            }
            if (properties.selection_type == DialogConfigs.FILE_SELECT) {
                holder.fmark.visibility = View.INVISIBLE
            } else {
                holder.fmark.visibility = View.VISIBLE
            }
        } else {
            holder.type_icon.setImageResource(R.mipmap.ic_type_file)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holder.type_icon.setColorFilter(
                    context.resources.getColor(
                        R.color.colorAccent,
                        context.theme
                    )
                )
            } else {
                holder.type_icon.setColorFilter(context.resources.getColor(R.color.colorAccent))
            }
            if (properties.selection_type == DialogConfigs.DIR_SELECT) {
                holder.fmark.visibility = View.INVISIBLE
            } else {
                holder.fmark.visibility = View.VISIBLE
            }
        }
        holder.type_icon.contentDescription = item.filename
        holder.name.text = item.filename
        val sdate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val stime = SimpleDateFormat("hh:mm aa", Locale.getDefault())
        val date = Date(item.time)
        if (i == 0 && item.filename!!.startsWith(context.getString(R.string.label_parent_dir))) {
            holder.type.setText(R.string.label_parent_directory)
        } else {
            holder.type.text =
                context.getString(R.string.last_edit) + sdate.format(date) + ", " + stime.format(
                    date
                )
        }
        if (holder.fmark.visibility == View.VISIBLE) {
            if (i == 0 && item.filename!!.startsWith(context.getString(R.string.label_parent_dir))) {
                holder.fmark.visibility = View.INVISIBLE
            }
            if (MarkedItemList.hasItem(item.location!!)) {
                holder.fmark.isChecked = true
            } else {
                holder.fmark.isChecked = false
            }
        }

        holder.fmark.setOnCheckedChangedListener(object : OnCheckedChangeListener {
            override fun onCheckedChanged(checkbox: MaterialCheckbox, isChecked: Boolean) {
                item.isMarked = isChecked
                if (item.isMarked) {
                    if (properties.selection_mode == DialogConfigs.MULTI_MODE) {
                        MarkedItemList.addSelectedItem(item)
                    } else {
                        MarkedItemList.addSingleFile(item)
                    }
                } else {
                    MarkedItemList.removeSelectedItem(item.location!!)
                }
                notifyItemChecked!!.notifyCheckBoxIsClicked()
            }
        })
        return view
    }

    private inner class ViewHolder internal constructor(itemView: View) {
        internal var type_icon: ImageView
        internal var name: TextView
        internal var type: TextView
        internal var fmark: MaterialCheckbox

        init {
            name = itemView.findViewById(R.id.fname) as TextView
            type = itemView.findViewById(R.id.ftype) as TextView
            type_icon = itemView.findViewById(R.id.image_type) as ImageView
            fmark = itemView.findViewById(R.id.file_mark) as MaterialCheckbox
        }
    }

}
