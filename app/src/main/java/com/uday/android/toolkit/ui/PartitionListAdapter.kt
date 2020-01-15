package com.uday.android.toolkit.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TextView
import com.uday.android.toolkit.R
import com.uday.android.util.BlockDeviceListData
import java.util.*

class PartitionListAdapter(
    private val contex: Context,
    private val layout_res: Int,
    private val partitionList: ArrayList<BlockDeviceListData>) : ArrayAdapter<BlockDeviceListData>(contex, layout_res, partitionList) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val row = inflater.inflate(layout_res, parent, false)
        val blockDevice = partitionList[position]

        (row.findViewById(R.id.part_name) as TextView).text = blockDevice.getName()
        (row.findViewById(R.id.block_dev) as TextView).text = blockDevice.getBlock()!!.absolutePath
        (row.findViewById(R.id.part_size) as TextView).text = blockDevice.sizeStr
        (row.findViewById(R.id.start_addr) as TextView).text = blockDevice.startStr
        (row.findViewById(R.id.end_addr) as TextView).text = blockDevice.endStr
        row.findViewById<ImageView>(R.id.size_expand).setOnClickListener {
            val addrs = row.findViewById<HorizontalScrollView>(R.id.part_size_addr)
            if (addrs.visibility == View.GONE) {
                val animation =
                    AnimationUtils.loadAnimation(context, R.anim.expand_to_down)
                addrs.visibility = View.VISIBLE
                addrs.startAnimation(animation)
            } else {
                val animation = AnimationUtils.loadAnimation(context, R.anim.shrink_to_top)
                animation!!.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p1: Animation) {}
                    override fun onAnimationEnd(p1: Animation) {
                        addrs.visibility = View.GONE
                    }

                    override fun onAnimationRepeat(p1: Animation) {}
                })
                addrs.startAnimation(animation)
            }
        }
        return row
    }
}
