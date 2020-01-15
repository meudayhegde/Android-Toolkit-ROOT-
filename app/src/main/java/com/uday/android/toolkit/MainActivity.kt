package com.uday.android.toolkit

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.uday.android.toolkit.fragments.*
import com.uday.android.toolkit.ui.CustomToast
import com.uday.android.toolkit.ui.DrawerArrowDrawable
import com.uday.android.toolkit.ui.EnvSetup
import com.uday.android.toolkit.ui.RebootDialog
import eu.chainfire.libsuperuser.Shell
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private var mKernel: KernelFragment? = null
    private var fragment: Fragment? = null
    private var mDrawerLayout: DrawerLayout? = null
    private var mAbout: AboutFragment? = null
    private var mBatch: BatchInstallerFragment? = null
    private var mPart: PartitionSchemeFragment? = null
    private var mAndroid: AndroidImagesFragment? = null
    private var mAppManager: AppManagerFragment? = null

    private var isDuplicateActivity = false
    private var mBuildProp: BuildPropFragment? = null
    private var envSetup: EnvSetup? = null
    private var offset: Float = 0.toFloat()
    private var doubleBackToExitPressedOnce = false
    private var drawerArrow: DrawerArrowDrawable? = null

    private var selectedItem: Int = 0
    private var backgroundThread: BackgroundThread? = null
    private var menuReboot: MenuItem? = null
    private var menuSettings: MenuItem?=null
    var backgroundThreadisRunning = false
    var isExcepted = false
    var mGrowIn: AnimationSet?=null
    var opened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isActivityAlive) {
            isDuplicateActivity = true
            finish()
        }
        isActivityAlive = true
        supportActionBar?.setBackgroundDrawable(ColorDrawable(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {getColor(R.color.colorPrimary)}
            else {
                @Suppress("DEPRECATION")
                resources.getColor(R.color.colorPrimary)
            }
        ))
        val metrics = resources.displayMetrics
        SCREEN_HEIGHT = metrics.heightPixels
        SCREEN_WIDTH = metrics.widthPixels

        val value = TypedValue()
        theme.resolveAttribute(R.attr.colorOnBackground, value, true)
        colorOnBackground = value.data

        stringComparator = Comparator<Any> { p1, p2 -> p1.toString().compareTo(p2.toString(), ignoreCase = true) }
        mGrowIn = AnimationUtils.loadAnimation(this,R.anim.activity_push_up_in) as AnimationSet?
        mFadeIn = AnimationUtils.loadAnimation(this,android.R.anim.fade_in)

        envSetup = object : EnvSetup(this) {
            override fun onStartup() {
                super.onStartup()
                createFragments()
                try {
                    drawerSetup()
                } catch (ex: IllegalStateException) {
                    isExcepted = true
                }
            }
        }

    }

    override fun onDestroy() {
        if (!isDuplicateActivity)
            isActivityAlive = false

        super.onDestroy()
    }


    override fun onResume() {
        if (isExcepted) {
            drawerSetup()
            isExcepted = false
        }
        super.onResume()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            File("$filesDir/partition_scheme.info").delete()

            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true
        CustomToast.showNotifyToast(this, "Please click BACK again to exit", Toast.LENGTH_SHORT)

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // TODO: Implement this method
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }


    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        // TODO: Implement this method
        val drawerOpen: Boolean = if (mDrawerLayout != null) {
            mDrawerLayout!!.isDrawerOpen(nav_view!!)
        } else {
            false
        }

        menuSettings = menu.findItem(R.id.settings)
        menuSettings?.setOnMenuItemClickListener {
            val intent = Intent(this@MainActivity, SettingsActivity::class.java)
            startActivity(intent)
            true
        }
        menuReboot = menu.findItem(R.id.reboot)
        menuReboot!!.setOnMenuItemClickListener(RebootDialog(this))

        menuSettings?.isVisible = !drawerOpen
        menuReboot!!.isVisible = !drawerOpen

        if ((fragment === mBatch || fragment === mBuildProp) && fragment != null) {
            hideContextMenu()
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun hideContextMenu() {
        if (menuReboot != null) {
            menuReboot!!.setShowAsAction(0)
            menuSettings?.setShowAsAction(0)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        envSetup!!.onRequestPermissionsResult(requestCode, grantResults)
    }


    private fun createFragments() {
        mKernel = KernelFragment(this)
        mAndroid = AndroidImagesFragment(this)
        mPart = PartitionSchemeFragment(this)
        mBatch = BatchInstallerFragment(this)
        mAbout = AboutFragment(this)
        mAppManager = AppManagerFragment(this)
        mBuildProp = BuildPropFragment(this)
    }

    private fun drawerSetup() {
        setContentView(R.layout.activity_main)
        mDrawerLayout = findViewById(R.id.drawer_layout)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        drawerArrow = DrawerArrowDrawable(resources)
        drawerArrow!!.setStrokeColor(Color.WHITE)
        supportActionBar?.setHomeAsUpIndicator(drawerArrow)

        val toggle = object:DrawerLayout.SimpleDrawerListener(){
            override fun onDrawerOpened(drawerView: View) {
                supportActionBar?.title = getString(R.string.app_name)
                super.onDrawerOpened(drawerView)
            }

            override fun onDrawerClosed(drawerView: View) {
                supportActionBar?.title = nav_view.checkedItem?.title
                invalidateOptionsMenu()
                super.onDrawerClosed(drawerView)
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                offset = slideOffset
                if (slideOffset >= .995)
                    drawerArrow!!.setFlip(true)
                else if (slideOffset <= .005)
                    drawerArrow!!.setFlip(false)
                drawerArrow!!.setParameter(offset)
                super.onDrawerSlide(drawerView, slideOffset)
            }
        }
        nav_view.setNavigationItemSelectedListener(this)
        mDrawerLayout!!.addDrawerListener(toggle)
        selectItem(R.id.drawer_about)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        selectItem(item.itemId)

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> if (mDrawerLayout!!.isDrawerOpen(nav_view!!))
                mDrawerLayout!!.closeDrawer(nav_view!!)
            else
                mDrawerLayout!!.openDrawer(nav_view!!)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun selectItem(itemId: Int) {
        // update the main content by replacing fragments
        fragment = null
        when (itemId) {
            R.id.drawer_kernel //kernel
            -> fragment = mKernel
            R.id.drawer_android//android images
            -> fragment = mAndroid
            /*	case 3://Raw Disk image
				fragment=mRaw;
				break;*/
            R.id.drawer_partition//Partition Scheme
            -> fragment = mPart
            R.id.drawer_app_manager -> fragment = mAppManager
            R.id.drawer_apk_manager//BatchApp
            -> fragment = mBatch
            R.id.drawer_build_prop //build prop
            -> fragment = mBuildProp
            R.id.drawer_about//about
            -> fragment = mAbout
            else -> {
            }
        }
        if (fragment != null) {
            selectedItem = itemId
            val fragmentManager = supportFragmentManager
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment!!).commit()

            nav_view.setCheckedItem(selectedItem)
            mDrawerLayout!!.closeDrawer(GravityCompat.START)
        }
    }

    fun refreshApkScreen() {
        BatchInstallerFragment.apkFilesOrig = null
        mBatch = BatchInstallerFragment(this@MainActivity)
        selectItem(4)
    }

    fun runInBackground(action: Runnable) {
        if (backgroundThread == null) {
            backgroundThread = BackgroundThread()
        } else if (!backgroundThread!!.isInBackground)
            backgroundThread = BackgroundThread()
        backgroundThread!!.runNewAction(action)
    }


    private inner class BackgroundThread internal constructor() : Thread() {
        internal// TODO: Implement this method
        var isInBackground = true
        private val actionsToRun: ArrayList<Runnable> = ArrayList()
        init {
            start()
        }

        override fun run() {
            while (isInBackground) {
                while (actionsToRun.isNotEmpty()) {
                    try {
                        backgroundThreadisRunning = true
                        actionsToRun[0].run()
                        actionsToRun.removeAt(0)
                    } catch (ex: NullPointerException) {
                        Log.e(TAG, ex.toString())
                    }

                }
                backgroundThreadisRunning = false
            }

        }

        internal fun runNewAction(action: Runnable) {
            actionsToRun.add(action)
        }

        override fun destroy() {
            isInBackground = false
        }
    }

    companion object {
        private var isActivityAlive: Boolean = false
        var TAG = "ANDROID TOOLKIT"
        var rootSession: Shell.Interactive? = null
        var SCREEN_HEIGHT: Int = 0
        var SCREEN_WIDTH: Int = 0
        var stringComparator: Comparator<*>?=null
        var TOOL: String? = null
        var mFadeIn: Animation? = null
        var colorOnBackground = 0
    }

}



