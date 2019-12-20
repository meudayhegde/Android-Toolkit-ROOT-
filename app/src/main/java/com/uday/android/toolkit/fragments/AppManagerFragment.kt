package com.uday.android.toolkit.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R

class AppManagerFragment : androidx.fragment.app.Fragment {

    private var rootView: RelativeLayout? = null
    private var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    private var context: Context? = null

    constructor()
    @SuppressLint("ValidFragment")
    constructor(context: Context) {
        this.context = context
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.app_fragment, container, false) as RelativeLayout
            onViewFirstCreated()
        }
        rootView!!.startAnimation(MainActivity.mFadeIn)
        return rootView
    }

    override fun getContext(): Context? {
        if (context == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            context = super.getContext()
        return context
    }


    private fun onViewFirstCreated() {
        swipeRefreshLayout = rootView!!.findViewById(R.id.swiperefresh) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        swipeRefreshLayout!!.setOnRefreshListener { swipeRefreshLayout!!.isRefreshing = false }
    }

}
