package com.uday.android.toolkit.ui
import android.annotation.SuppressLint
import android.content.Context
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.uday.android.toolkit.MainActivity
import com.uday.android.toolkit.R

class RebootDialog(private val context:Context):MenuItem.OnMenuItemClickListener {
    private var rebootLayout:RadioGroup? = null
    private var command:String? = null
    private var dialog: AlertDialog? = null

    @SuppressLint("InflateParams")
    override fun onMenuItemClick(p1:MenuItem):Boolean {
        if (dialog == null) {
            rebootLayout = (context as AppCompatActivity).layoutInflater.inflate(R.layout.reboot_dialog, null) as RadioGroup
            rebootLayout!!.check(R.id.reboot_button)
            dialog = AlertDialog.Builder(context).setView(rebootLayout)
                .setTitle("Advanced Reboot Menu")
                .setNegativeButton("cancel", null)
                .setPositiveButton("ok") {
                        p2, _ ->
                        p2.cancel()
                        var notify = "Are you sure you want to "
                        when (rebootLayout!!.checkedRadioButtonId) {
                            R.id.power_off -> {
                                notify += "Power Off..?"
                                command = "setprop sys.powerctl shutdown\nsleep 3\nreboot -p"
                            }
                            R.id.reboot_button -> {
                                notify += "Reboot..?"
                                command = "setprop sys.powerctl reboot\nsleep 3\nreboot"
                            }
                            R.id.reboot_recovery -> {
                                notify += "reboot to recovery..?"
                                command = "setprop ctl.start pre-recovery\nsleep 3\nreboot recovery"
                            }
                            R.id.reboot_bootloader -> {
                                notify += "reboot to bootloader..?"
                                command = "reboot bootloader\n" + MainActivity.TOOL + " reboot bootloader"
                            }
                            R.id.soft_reboot -> {
                                notify += "soft reboot..? (not recomended)"
                                command = "setprop ctl.restart zygote\nsleep 3\n" + MainActivity.TOOL + " pkill zygote"
                            }
                            R.id.system_ui_restart -> {
                                notify += "restart SystemUi..?"
                                command = MainActivity.TOOL!! + " pkill com.android.systemui"
                            }
                        }

                        if (context.getSharedPreferences("general", 0).getBoolean("show_reboot_confirm_dialog", true))
                            DialogUtils(context).showConfirmDialog(false, 0, "confirm", notify, "cancel", "yes", object:DialogUtils.OnClickListener {
                                override fun onClick(p1: AlertDialog?) {
                                    p1!!.cancel()
                                    carryAction(command)
                                }
                            })
                        else carryAction(command)
                }.create()
            dialog!!.window!!.attributes.windowAnimations = R.style.DialogTheme
        }
        rebootLayout!!.findViewById<RadioButton>(R.id.soft_reboot).isEnabled = context.getSharedPreferences("general", 0).getBoolean("allow_soft_reboot", false)
        dialog!!.show()
        return true
    }

    private fun carryAction(command:String?) {
        MainActivity.rootSession!!.addCommand(command)
    }
}
