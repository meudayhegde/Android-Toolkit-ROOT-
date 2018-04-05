package com.uday.android.toolkit.listeners;

import android.app.*;
import android.content.*;
import android.view.*;
import com.uday.android.toolkit.fragments.*;
import eu.chainfire.libsuperuser.*;
import com.uday.android.toolkit.ui.*;
import android.widget.*;

public class KernCommandLineListener implements Shell.OnCommandLineListener
{
	private KernelFragment fragment;
	public KernCommandLineListener(KernelFragment fragment){
		this.fragment=fragment;
	}
	
	@Override
	public void onCommandResult(final int commandcode,final int exitcode){
		fragment.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					fragment.termProgress.setVisibility(View.GONE);
					fragment.termDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
					fragment.termDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
					if(exitcode==0){
						switch(commandcode){
							case fragment.SELECTED_BACKUP:
								CustomToast.showSuccessToast(fragment.context,"kernel backup completed.\n"
													 +"backups dir: "+fragment.DIR+"/backups/Kernel",Toast.LENGTH_SHORT);
								break;
							case fragment.SELECTED_RESTORE:
								CustomToast.showSuccessToast(fragment.context,"selected kernel has been restored successfully\n"
																 +"reboot the device to activate the kernel",Toast.LENGTH_SHORT);
								break;
							case fragment.SELECTED_INSTALL:
								CustomToast.showSuccessToast(fragment.context,"Installation completed.."+
																 "reboot the device to change to take effect\n\n"+
																 "Note: device will not boot if you flashed a wrong kernel",Toast.LENGTH_LONG);
								break;
						}
					}
					else{
						
					
						CustomToast.showSuccessToast(fragment.context,"Something went wrong.\nplease check log for more info",Toast.LENGTH_LONG);
					}
				}
			});
	}
	@Override
	public void onLine(final String line){
		((Activity)fragment.context).runOnUiThread(new Runnable(){
				@Override
				public void run(){
					appendLineToOutput(line);
					
				}
			});
	}
	

	private void appendLineToOutput(String line) {
        StringBuilder sb = (new StringBuilder()).
			append(line).
			append((char)10);
        fragment.termTextView.append(sb.toString());
    }
	
}
