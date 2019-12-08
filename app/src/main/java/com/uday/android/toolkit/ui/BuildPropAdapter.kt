package com.uday.android.toolkit.ui

import android.annotation.SuppressLint
import android.content.*
import android.view.*
import android.widget.*
import com.uday.android.toolkit.*
import com.uday.android.toolkit.fragments.*
import com.uday.android.util.*
import java.util.*
import android.text.*

@SuppressLint("NewApi")
class BuildPropAdapter(private val fragment: BuildPropFragment) : ArrayAdapter<BuildProperty>(fragment.context, R.layout.build_prop_list_item, fragment.buildProperties) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val row = inflater.inflate(R.layout.build_prop_list_item, parent, false)
        val propTxt = row.findViewById(R.id.build_property) as TextView
        val valTxt = row.findViewById(R.id.build_value) as TextView
        var value = fragment.buildProperties[position].VALUE
        var property = fragment.buildProperties[position].PROPERTY
        val txtSearch = fragment.buildProperties[position].textSearch
        if (txtSearch != null && value.toLowerCase().contains(txtSearch.toLowerCase())) {
            var regx = ""
            val start = value.toLowerCase().indexOf(txtSearch.toLowerCase())
            val length = txtSearch.length
            for (i in 0 until length) {
                regx = regx + value[start + i]
            }
            value = value.replace(regx, "<font color=\"#00AEFF\">$regx</font>")
        }

        if (txtSearch != null && property.toLowerCase().contains(txtSearch.toLowerCase())) {
            var regx = ""
            val start = property.toLowerCase().indexOf(txtSearch.toLowerCase())
            val length = txtSearch.length
            for (i in 0 until length) {
                regx = regx + property[start + i]
            }
            property = property.replace(regx, "<font color=\"#00AEFF\">$regx</font>")
        }

        valTxt.text = Html.fromHtml(value)
        propTxt.text = Html.fromHtml(property)

        row.setOnClickListener(OnItemClick(fragment.buildProperties[position], position))

        return row
    }

    private inner class OnItemClick(private val prop: BuildProperty, private val position: Int) :
        View.OnClickListener {

        override fun onClick(p1: View) {
            if (fragment.dialog == null)
                fragment.initDialog()
            else
                fragment.dialog!!.show()

            fragment.selected = position
            fragment.setDialog(BuildPropFragment.PRIMARY_TYPE)
            fragment.editPropView.setText(prop.PROPERTY)
            fragment.editValView.setText(prop.VALUE)
            fragment.PropTextView.text = prop.PROPERTY
            fragment.ValTextView.text = prop.VALUE

        }
    }

    fun filter(charText: String) {
        var charText = charText
        charText = charText.toLowerCase(Locale.getDefault())
        fragment.buildProperties.clear()
        if (charText.length == 0) {
            fragment.buildProperties.addAll(BuildPropFragment.buildPropertiesOrig!!)
            for (property in fragment.buildProperties) {
                property.textSearch = null
            }
        } else {
            for (data in BuildPropFragment.buildPropertiesOrig!!) {
                if ((data.PROPERTY + data.VALUE).toLowerCase(Locale.getDefault()).contains(charText)) {
                    data.textSearch = charText
                    fragment.buildProperties.add(data)
                }
            }
        }
        notifyDataSetChanged()
    }

}
