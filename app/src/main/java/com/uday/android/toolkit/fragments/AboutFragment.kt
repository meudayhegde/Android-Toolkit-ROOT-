package com.uday.android.toolkit.fragments

import android.annotation.*
import android.app.*
import android.content.*
import android.net.*
import android.os.*
import android.text.*
import android.text.method.*
import android.view.*
import android.widget.*
import com.uday.android.toolkit.*
import com.uday.android.util.*
import android.graphics.drawable.*
import android.graphics.*
import androidx.fragment.app.Fragment

@SuppressLint("NewApi")
class AboutFragment : androidx.fragment.app.Fragment {

    private var rootView: View? = null
    private var context: Context? = null
    private var fabLicense: LinearLayout? = null
    private var LibSuLicense: LinearLayout? = null
    private var filePickerLicense: LinearLayout? = null
    private var qrDialog: Dialog? = null

    constructor() {}

    @SuppressLint("ValidFragment")
    constructor(context: Context) {
        this.context = context
    }

    override fun getContext(): Context? {
        if (context == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context = super.getContext()
        }
        return context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            rootView = inflater
                .inflate(R.layout.home, container, false)

            val findMe = rootView!!.findViewById(R.id.find_me) as TextView
            val donateMe = rootView!!.findViewById(R.id.donate_me) as TextView
            val donatePaytm = rootView!!.findViewById(R.id.donate_me_paytm) as TextView
            val xdaLink = rootView!!.findViewById(R.id.xda_link) as TextView
            findMe.isClickable = true
            donateMe.isClickable = true
            xdaLink.isClickable = true
            findMe.movementMethod = LinkMovementMethod.getInstance()
            findMe.text = Html.fromHtml(
                Utils.getStringFromInputStream(
                    getContext()!!.resources.openRawResource(R.raw.find_me)
                )
            )
            donateMe.movementMethod = LinkMovementMethod.getInstance()
            donateMe.text = Html.fromHtml(
                Utils.getStringFromInputStream(
                    getContext()!!.resources.openRawResource(R.raw.donate_me)
                )
            )
            xdaLink.movementMethod = LinkMovementMethod.getInstance()
            xdaLink.text =
                Html.fromHtml("<a href=" + '"'.toString() + "https://forum.xda-developers.com/android/apps-games/app-android-toolkit-t3772040/post76093810#post76093810" + '"'.toString() + "><b>Link to Xda official thread</b></a>")

            qrDialog = Dialog(getContext()!!)
            qrDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
            val qrImage = ImageView(getContext())
            qrImage.setImageResource(R.drawable.qr_code_paytm)
            qrDialog!!.setContentView(qrImage)
            qrDialog!!.window!!.setLayout(
                (MainActivity.SCREEN_WIDTH * 0.7).toInt(),
                (MainActivity.SCREEN_WIDTH * 0.7).toInt()
            )
            qrDialog!!.window!!.attributes.windowAnimations = R.style.DialogTheme
            qrDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            qrDialog!!.setCanceledOnTouchOutside(false)

            donatePaytm.setOnClickListener { qrDialog!!.show() }

            fabLicense = rootView!!.findViewById(R.id.fab_license) as LinearLayout
            fabLicense!!.setOnClickListener {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.apache.org/licenses/LICENSE-2.0")
                )
                startActivity(browserIntent)
            }

            LibSuLicense = rootView!!.findViewById(R.id.lib_su_license) as LinearLayout
            LibSuLicense!!.setOnClickListener {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.apache.org/licenses/LICENSE-2.0")
                )
                startActivity(browserIntent)
            }

            filePickerLicense = rootView!!.findViewById(R.id.filpicker_license) as LinearLayout
            filePickerLicense!!.setOnClickListener {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://www.apache.org/licenses/LICENSE-2.0")
                )
                startActivity(browserIntent)
            }
        }
        rootView!!.startAnimation(MainActivity.mFadeIn)
        return rootView
    }

}
