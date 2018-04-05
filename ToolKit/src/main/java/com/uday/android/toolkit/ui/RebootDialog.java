package com.uday.android.toolkit.ui;
import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import com.uday.android.toolkit.*;

public class RebootDialog implements MenuItem.OnMenuItemClickListener
{

	private Context context;
	private RadioGroup rebootLayout;
	private String command;
	private AlertDialog dialog;
	public RebootDialog(Context context){
		this.context=context;
	}
	
	@Override
	public boolean onMenuItemClick(MenuItem p1)
	{
		// TODO: Implement this method
		if(dialog==null){
			rebootLayout=(RadioGroup)((Activity)context).getLayoutInflater().inflate(R.layout.reboot_dialog,null);
			rebootLayout.check(R.id.reboot_button);
			dialog=new AlertDialog.Builder(context).setView(rebootLayout)
							.setTitle("Advanced Reboot Menu")
							.setNegativeButton("cancel",null)
							.setPositiveButton("ok",
							new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface p1,int p2){
									p1.cancel();
									String notify="Are you sure you want to ";
									switch(rebootLayout.getCheckedRadioButtonId()){
										case R.id.power_off:
											notify=notify+"Power Off..?";
											command="setprop sys.powerctl shutdown\nsleep 3\nreboot -p";
											break;
										case R.id.reboot_button:
											notify=notify+"Reboot..?";
											command="setprop sys.powerctl reboot\nsleep 3\nreboot";
											break;
										case R.id.reboot_recovery:
											notify=notify+"reboot to recovery..?";
											command="setprop ctl.start pre-recovery\nsleep 3\nreboot recovery";
											break;
										case R.id.reboot_bootloader:
											notify=notify+"reboot to bootloader..?";
											command="reboot bootloader\n"+MainActivity.TOOL+" reboot bootloader";
											break;
										case R.id.soft_reboot:
											notify=notify+"soft reboot..? (not recomended)";
											command="setprop ctl.restart zygote\nsleep 3\n"+MainActivity.TOOL+" pkill zygote";
											break;
										case R.id.system_ui_restart:
											notify=notify+"restart SystemUi..?";
											command=MainActivity.TOOL+" pkill com.android.systemui";
											break;
									}
									
									if(context.getSharedPreferences("general",0).getBoolean("show_reboot_confirm_dialog",true))
											new DialogUtils(context).showConfirmDialog(false,0,"confirm",notify,"cancel","yes",new DialogUtils.OnClickListener(){
												@Override
												public void onClick(AlertDialog p1){
													p1.cancel();
													carryAction(command);
												}
											});
									else carryAction(command);
								}
							}).create();
			dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		}
		rebootLayout.findViewById(R.id.soft_reboot).setEnabled(context.getSharedPreferences("general",0).getBoolean("allow_soft_reboot",false));
		dialog.show();
		return true;
	}
	
	private void carryAction(String command){
		MainActivity.rootSession.addCommand(command);
	}
}
