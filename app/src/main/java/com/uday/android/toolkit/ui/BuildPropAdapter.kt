package com.uday.android.toolkit.ui

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.uday.android.toolkit.R
import com.uday.android.toolkit.fragments.BuildPropFragment
import com.uday.android.util.BuildProperty
import java.util.*

@SuppressLint("NewApi")
class BuildPropAdapter(private val fragment: BuildPropFragment) : ArrayAdapter<BuildProperty>(fragment.context, R.layout.build_prop_list_item, fragment.buildProperties) {
    @SuppressLint("ViewHolder", "DefaultLocale")
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
                regx += value[start + i]
            }
            value = value.replace(regx, "<font color=\"#00AEFF\">$regx</font>")
        }

        if (txtSearch != null && property.toLowerCase().contains(txtSearch.toLowerCase())) {
            var regx = ""
            val start = property.toLowerCase().indexOf(txtSearch.toLowerCase())
            val length = txtSearch.length
            for (i in 0 until length) {
                regx += property[start + i]
            }
            property = property.replace(regx, "<font color=\"#00AEFF\">$regx</font>")
        }

        @Suppress("DEPRECATION")
        valTxt.text = Html.fromHtml(value)
        @Suppress("DEPRECATION")
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
            fragment.propTextView.text = prop.PROPERTY
            fragment.valTextView.text = prop.VALUE

        }
    }

    fun filter(charText_: String) {
        var charText = charText_
        charText = charText.toLowerCase(Locale.getDefault())
        fragment.buildProperties.clear()
        if (charText.isEmpty()) {
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
