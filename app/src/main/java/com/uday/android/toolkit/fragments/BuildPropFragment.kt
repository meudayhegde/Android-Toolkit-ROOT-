package com.uday.android.toolkit.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import android.widget.AbsListView.OnScrollListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.github.clans.fab.FloatingActionButton
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R
import com.uday.android.toolkit.ui.BuildPropAdapter
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.util.BuildProperty
import com.uday.android.util.Utils
import eu.chainfire.libsuperuser.Shell
import java.io.File
import java.util.*

@SuppressLint("NewApi", "ValidFragment")
class BuildPropFragment @SuppressLint("ValidFragment")
    constructor(private val context:Context): androidx.fragment.app.Fragment() {
    var dialogContent:LinearLayout
    var editPropView:EditText
    var editValView:EditText
    var PropTextView:TextView
    var ValTextView:TextView
    var dialog: AlertDialog? = null
    var buildProperties:ArrayList<BuildProperty>
    var positive:Button?=null
    var negative:Button?=null
    var neutral:Button?=null
    var selected:Int = 0
    var BuildProp:File? = null

    private val ConfirmTextView:TextView
    private val PropContent:LinearLayout
    private var rootView:RelativeLayout? = null
    private var fab:FloatingActionButton? = null
    private var buildListView:ListView? = null
    private var adapter:BuildPropAdapter? = null
    private var n:Int = 0
    private val onSaveClicked:OnClickListener
    private var mPreviousVisibleItem:Int = 0
    private var scrollState:Int = 0
    private val nameComparator:Comparator<BuildProperty>
    var mOption:Int = 0


    init{
        buildProperties = ArrayList()
        buildPropertiesOrig = ArrayList()
        rootsession = MainActivity.rootSession
        dialogContent = (context as AppCompatActivity).layoutInflater.inflate(R.layout.build_prop_edit_dialog, null) as LinearLayout
        PropContent = dialogContent.findViewById(R.id.prop_content) as LinearLayout
        ConfirmTextView = dialogContent.findViewById(R.id.confirm_txt) as TextView
        editPropView = dialogContent.findViewById(R.id.build_edit_prop) as EditText
        editValView = dialogContent.findViewById(R.id.build_edit_value) as EditText
        PropTextView = dialogContent.findViewById(R.id.prop_text) as TextView
        ValTextView = dialogContent.findViewById(R.id.val_text) as TextView

        onSaveClicked = OnClickListener {
            dialog!!.cancel()
            val command:String
            when (mOption) {
                DELETE -> {
                    command = (this@BuildPropFragment.context.getFilesDir()).toString() + "/common/build_prop_edit.sh " + MainActivity.TOOL + " del_prop " + buildProperties[selected].PROPERTY
                    buildProperties.removeAt(selected)
                }
                SAVE -> {
                    command = ((this@BuildPropFragment.context.getFilesDir()).toString() + "/common/build_prop_edit.sh " + MainActivity.TOOL + " set_prop '" + buildProperties[selected].PROPERTY + "' '"
                            + editPropView.text.toString() + "' '" + editValView.text.toString() + "'")
                    buildProperties[selected].PROPERTY = editPropView.text.toString()
                    buildProperties[selected].VALUE = editValView.text.toString()
                }
                NEW -> command = ((this@BuildPropFragment.context.getFilesDir()).toString() + "/common/build_prop_edit.sh new_prop '" + editPropView.text.toString() + "'='" + editValView.text.toString() + "'")
                else -> command = "echo Invalid Option\nreturn 1"
            }
            rootsession!!.addCommand(command, mOption
            ) { _, resultcode, output ->
                runOnUiThread(Runnable {
                    if (resultcode == 0) {
                        CustomToast.showSuccessToast(getContext(), "Operation successful\n" + Utils.getString(output), Toast.LENGTH_SHORT)
                    } else
                        CustomToast.showFailureToast(getContext(), "Operation failed ..!!\n" + Utils.getString(output), Toast.LENGTH_SHORT)
                    refreshProp()
                })
            }
        }

        nameComparator = Comparator { p1, p2 -> (p1.PROPERTY + p1.VALUE).compareTo(p2.PROPERTY + p2.VALUE, ignoreCase = true) }
    }

    override fun getContext():Context{
        return context
    }

    override fun onResume() {
        fab!!.hide(false)
        Handler().postDelayed({ fab!!.show(true) }, 400)
        super.onResume()
    }

    override fun onCreateOptionsMenu(menu:Menu, inflater:MenuInflater) {
        inflater.inflate(R.menu.build_prop_menu, menu)

        val search = menu.findItem(R.id.action_search_build_prop).actionView as SearchView
            search.queryHint = "type to search..."
        search.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText:String):Boolean {
                adapter!!.filter(newText)
                return true
            }

            override fun onQueryTextSubmit(txt:String):Boolean {
                return false
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onCreateView(inflater:LayoutInflater, container:ViewGroup?, savedInstanceState:Bundle?):View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.build_prop_fragment, container, false) as RelativeLayout
            fab = rootView!!.findViewById(R.id.add_prop) as FloatingActionButton
            fab!!.setOnClickListener {
                if (dialog == null) initDialog()
                else dialog!!.show()
                mOption = NEW
                setDialog(EDIT_TYPE)
                dialog!!.setTitle("Add new property")
                editPropView.setText("")
                editValView.setText("")
            }

            buildListView = rootView!!.findViewById(R.id.build_list_view) as ListView
            adapter = BuildPropAdapter(this)
                buildListView!!.adapter = adapter

            buildListView!!.setOnScrollListener(object:OnScrollListener {
                override fun onScrollStateChanged(view:AbsListView, scrollState:Int) {
                    this@BuildPropFragment.scrollState = scrollState
                    if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                        Handler().postDelayed({
                            if (fab!!.isHidden && this@BuildPropFragment.scrollState == OnScrollListener.SCROLL_STATE_IDLE)
                                fab!!.show(true)
                        }, 400)
                    }
                }

                override fun onScroll(view:AbsListView, firstVisibleItem:Int, visibleItemCount:Int, totalItemCount:Int) {
                    if (firstVisibleItem > mPreviousVisibleItem)
                    fab!!.hide(true)
                    else if (firstVisibleItem < mPreviousVisibleItem)
                    fab!!.show(true)
                    mPreviousVisibleItem = firstVisibleItem
                }
            })

            rootView!!.visibility = View.GONE
            refreshProp()
        }
        else rootView!!.startAnimation(MainActivity.mFadeIn)
        return rootView
    }

    fun refreshProp() {
        n = 0
        rootsession!!.addCommand(context.filesDir.absolutePath + "/common/refresh_prop.sh " + MainActivity.TOOL, 1512, object:Shell.OnCommandLineListener {
            override fun onCommandResult(commandCode:Int, exitCode:Int) {
                runOnUiThread(Runnable {
                    Collections.sort(buildPropertiesOrig, nameComparator)
                    buildProperties.clear()
                    buildProperties.addAll(buildPropertiesOrig!!)
                    adapter!!.notifyDataSetChanged()
                    rootView!!.visibility = View.VISIBLE
                    rootView!!.startAnimation((context as MainActivity).mGrowIn)
                    setHasOptionsMenu(true)
                })
            }
            override fun onLine(line:String) {
                if (!line.startsWith("#") && line.split(("=").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size == 2) {
                    try {
                        val str = line.split(("=").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                        addIntoList(BuildProperty(str[0], str[1]))
                    }
                    catch (ex:Exception) {}
                }
            }
        })
    }

    fun runOnUiThread(action:Runnable) {
        (context as AppCompatActivity).runOnUiThread(action)
    }

    fun toast(toast:String) {
        Toast.makeText(context, toast, Toast.LENGTH_SHORT).show()
    }

    private fun addIntoList(buildProperty:BuildProperty) {
        if (Collections.binarySearch(buildPropertiesOrig, buildProperty, nameComparator) < 0) {
            buildPropertiesOrig?.add(buildProperty)
            n++
        }
    }

    fun initDialog() {
        dialog = AlertDialog.Builder(context)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .setNeutralButton("Delete", null)
            .setView(dialogContent)
            .show()
        positive = dialog!!.getButton(AlertDialog.BUTTON_POSITIVE)
        negative = dialog!!.getButton(AlertDialog.BUTTON_NEGATIVE)
        neutral = dialog!!.getButton(AlertDialog.BUTTON_NEUTRAL)
    }

    fun setDialog(type:Int) {
        when (type) {
            EDIT_TYPE -> {
                PropContent.visibility = View.VISIBLE
                ConfirmTextView.visibility = View.GONE
                PropTextView.visibility = View.GONE
                ValTextView.visibility = View.GONE
                editPropView.visibility = View.VISIBLE
                editValView.visibility = View.VISIBLE

                positive?.text = "Save"
                neutral?.visibility = View.GONE
                positive?.setOnClickListener {
                    ConfirmTextView.text = "Are you sure you want to save changes..?"
                    setDialog(CONFIRM_TYPE)
                }
            }

            PRIMARY_TYPE -> {
                PropContent.visibility = View.VISIBLE
                ConfirmTextView.visibility = View.GONE
                PropTextView.visibility = View.VISIBLE
                ValTextView.visibility = View.VISIBLE
                editPropView.visibility = View.GONE
                editValView.visibility = View.GONE

                positive?.text = "Edit"
                neutral?.text = "Delete"
                neutral?.visibility = View.VISIBLE
                positive?.setOnClickListener {
                    mOption = SAVE
                    setDialog(EDIT_TYPE)
                }
                neutral?.setOnClickListener {
                    mOption = DELETE
                    ConfirmTextView.text = "Are you sure you want to delete this property..?"
                    setDialog(CONFIRM_TYPE)
                }
            }
            CONFIRM_TYPE -> {
                PropContent.visibility = View.GONE
                ConfirmTextView.visibility = View.VISIBLE
                positive?.text = "Confirm"
                positive?.setOnClickListener(onSaveClicked)
                neutral?.visibility = View.GONE
            }
        }

    }

    companion object {

        var buildPropertiesOrig:ArrayList<BuildProperty>?=null
        private var rootsession:Shell.Interactive?=null

        const val EDIT_TYPE = 0
        const val PRIMARY_TYPE = 1
        const val CONFIRM_TYPE = 2

        const val DELETE = 3
        const val SAVE = 4
        const val NEW = 5
    }

}
