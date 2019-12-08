package com.uday.android.toolkit.ui

import android.widget.*
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.R

class CustomToast(private val context: Context) : Toast(context) {
    companion object {

        fun showSuccessToast(context: Context, message: String, duration: Int) {
            (context as AppCompatActivity).runOnUiThread {
                val toastLayout =
                    context.layoutInflater.inflate(R.layout.toast_custom, null) as LinearLayout
                val text = toastLayout.findViewById(R.id.toast_text) as TextView
                text.text = message
                toastLayout.background =
                    context.getResources().getDrawable(R.drawable.toast_success)
                val toast = Toast(context)
                toast.duration = duration
                toast.view = toastLayout
                toast.show()
            }
        }

        fun showFailureToast(context: Context, message: String, duration: Int) {
            (context as AppCompatActivity).runOnUiThread {
                val toastLayout =
                    context.layoutInflater.inflate(R.layout.toast_custom, null) as LinearLayout
                val text = toastLayout.findViewById(R.id.toast_text) as TextView
                text.text = message
                toastLayout.background =
                    context.getResources().getDrawable(R.drawable.toast_failure)
                val toast = Toast(context)
                toast.duration = duration
                toast.view = toastLayout
                toast.show()
            }
        }

        fun showNotifyToast(context: Context, message: String, duration: Int) {
            (context as AppCompatActivity).runOnUiThread {
                val toastLayout =
                    context.layoutInflater.inflate(R.layout.toast_custom, null) as LinearLayout
                val text = toastLayout.findViewById(R.id.toast_text) as TextView
                text.text = message
                toastLayout.background = context.getResources().getDrawable(R.drawable.toast_notify)
                val toast = Toast(context)
                toast.duration = duration
                toast.view = toastLayout
                toast.show()
            }
        }
    }
}
