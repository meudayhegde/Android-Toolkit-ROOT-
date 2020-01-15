package com.uday.android.toolkit.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.text.Spanned
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R

class DialogUtils(private val context: Context) {
    var dialog: AlertDialog? = null
        private set
    private val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        .setPositiveButton("ok", null)
        .setTitle(" ").setMessage(" ")
        .setNegativeButton("cancel", null)
        .setNeutralButton("another", null)
    var positiveButton: Button? = null
        private set
    var negativeButton: Button? = null
        private set
    private var mNeutral: Button? = null
    private var mView: View? = null

    val neutralButton: Button?
        get() {
            if (mNeutral != null) {
                mNeutral!!.visibility = View.VISIBLE
            }
            return mNeutral
        }

    fun create(): DialogUtils {
        dialog = dialogBuilder.create()
        dialog!!.window!!.attributes.windowAnimations = R.style.DialogTheme
        return this
    }

    fun setView(view: View): DialogUtils {
        mView = view
        dialogBuilder.setView(view)
        return this
    }

    fun setView(layoutRes: Int): DialogUtils {
        mView = (context as AppCompatActivity).layoutInflater.inflate(layoutRes, null)
        dialogBuilder.setView(mView)
        return this
    }

    fun setTitle(title: String): DialogUtils {
        dialog!!.setTitle(title)
        return this
    }

    fun setMessage(message: String): DialogUtils {
        dialog!!.setMessage(message)
        return this
    }

    fun show(): DialogUtils {
        dialog!!.show()
        if (positiveButton == null)
            positiveButton = dialog!!.getButton(DialogInterface.BUTTON_POSITIVE)
        if (negativeButton == null)
            negativeButton = dialog!!.getButton(DialogInterface.BUTTON_NEGATIVE)
        if (mNeutral == null)
            mNeutral = dialog!!.getButton(DialogInterface.BUTTON_NEUTRAL)
        return this
    }

    fun cancel(): DialogUtils {
        dialog!!.cancel()
        return this
    }

    fun showConfirmDialog(
        retainView: Boolean,
        icRes: Int,
        title: String?,
        message: String?,
        cancelText: String?,
        confirmText: String?,
        confirmListener: OnClickListener?
    ): DialogUtils {
        if (dialog == null) create()
        show()
        mNeutral!!.visibility = View.GONE
        if (mView != null && !retainView) mView!!.visibility = View.GONE
        if (title != null) dialog!!.setTitle(title) else dialog!!.setTitle("")
        if (message != null) dialog!!.setMessage(message) else dialog!!.setMessage("")
        if (cancelText != null) negativeButton!!.text =
            cancelText else negativeButton!!.visibility = View.GONE
        if (confirmText != null) positiveButton!!.text =
            confirmText else positiveButton!!.visibility = View.GONE
        if (icRes != 0) dialog!!.setIcon(icRes) else dialog!!.setIcon(null)
        if (confirmListener != null)
            positiveButton!!.setOnClickListener { confirmListener.onClick(dialog) }

        return this
    }

    interface OnClickListener {
        fun onClick(p1: AlertDialog?)
    }

    companion object {

        @SuppressLint("InflateParams")
        fun showEditTextDialog(context: Context, title: String?, message: String?, editorContent: String?, description: String?, confirmText: String?, confirmListener: OnClickListener): EditText {
            val contentView = (context as AppCompatActivity).layoutInflater.inflate(
                R.layout.edit_text,
                null
            ) as LinearLayout
            val edtTxt = contentView.findViewById(R.id.edt_txt) as EditText
            edtTxt.setText(editorContent)
            edtTxt.hint = description
            edtTxt.setSelectAllOnFocus(true)
            edtTxt.setSingleLine(true)
            val dialog = AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setView(contentView)
                .setPositiveButton(confirmText, null)
                .setNegativeButton("cancel", null)
                .create()
            dialog.window!!.attributes.windowAnimations = R.style.DialogTheme
            dialog.show()
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setOnClickListener { confirmListener.onClick(dialog) }

            return edtTxt
        }

        @SuppressLint("InflateParams")
        fun showTermDialog(context: Context, title: String?, subtitle: String?, positive: String?,
                           negative: String?): Array<Any> {
            val titleView = (context as AppCompatActivity).layoutInflater.inflate(
                R.layout.term_dialog_header,
                null
            ) as LinearLayout
            (titleView.findViewById(R.id.term_title) as TextView).text = title
            (titleView.findViewById(R.id.term_description) as TextView).text = subtitle

            val contentView =
                context.layoutInflater.inflate(R.layout.term_dialog_content, null) as LinearLayout
            contentView.findViewById<ScrollView>(R.id.term_scroller).layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (MainActivity.SCREEN_HEIGHT * 0.5).toInt()
            )
            val dialog = AlertDialog.Builder(context)
                .setCustomTitle(titleView)
                .setView(contentView)
                .setNegativeButton(negative, null)
                .setPositiveButton(positive, null)
                .create()
            dialog.window!!.attributes.windowAnimations = R.style.DialogTheme
            dialog.show()
            dialog.setCancelable(false)
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).visibility = View.GONE
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).isEnabled = false

            return arrayOf(
                dialog,
                contentView.findViewById(R.id.term_text),
                contentView.findViewById(R.id.term_progress)
            )
        }


        fun showConfirmDialog(context: Context,
            title: String?, content: String?,
            spannedContent: Spanned?, confirmTxt: String?,
            listener: OnClickListener?): AlertDialog {
            val dialogBuilder = AlertDialog.Builder(context).setTitle(title)
                .setMessage(content)
                .setNegativeButton("cancel", null)
                .setPositiveButton(confirmTxt, null)
            if (spannedContent != null) {
                val tw = TextView(context)
                tw.setPadding(40, 15, 20, 0)
                tw.text = spannedContent
                dialogBuilder.setView(tw)
            }
            val dialog = dialogBuilder.create()
            dialog.window!!.attributes.windowAnimations = R.style.DialogTheme
            dialog.show()
            if (listener != null) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener { listener.onClick(dialog) }
            }
            return dialog
        }
    }
}
