package com.uday.android.toolkit.ui

import android.widget.ArrayAdapter
import com.uday.android.util.BlockDeviceListData
import android.content.Context
import java.util.ArrayList
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import com.uday.android.toolkit.R
import android.widget.ImageView
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.HorizontalScrollView

class PartitionListAdapter(
    private val contex: Context,
    private val layout_res: Int,
    private val partitionList: ArrayList<BlockDeviceListData>) : ArrayAdapter<BlockDeviceListData>(contex, layout_res, partitionList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val inflater =
            getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val row = inflater.inflate(layout_res, parent, false)
        val blockDevice = partitionList[position]

        (row.findViewById(R.id.part_name) as TextView).text = blockDevice.getName()
        (row.findViewById(R.id.block_dev) as TextView).text = blockDevice.getBlock()!!.absolutePath
        (row.findViewById(R.id.part_size) as TextView).text = blockDevice.sizeStr
        (row.findViewById(R.id.start_addr) as TextView).text = blockDevice.startStr
        (row.findViewById(R.id.end_addr) as TextView).text = blockDevice.endStr
        row.findViewById<ImageView>(R.id.size_expand).setOnClickListener(View.OnClickListener {
            val addrs = row.findViewById<HorizontalScrollView>(R.id.part_size_addr)
            if (addrs.getVisibility() == View.GONE) {
                val animation =
                    AnimationUtils.loadAnimation(getContext(), R.anim.expand_to_down)
                addrs.setVisibility(View.VISIBLE)
                addrs.startAnimation(animation)
            } else {
                val animation = AnimationUtils.loadAnimation(getContext(), R.anim.shrink_to_top)
                animation!!.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(p1: Animation) {}
                    override fun onAnimationEnd(p1: Animation) {
                        addrs.setVisibility(View.GONE)
                    }

                    override fun onAnimationRepeat(p1: Animation) {}
                })
                addrs.startAnimation(animation)
            }
        })
        return row
    }
}
