package com.uday.android.toolkit.listeners;

import android.app.*;
import android.view.*;
import com.uday.android.toolkit.fragments.*;
import eu.chainfire.libsuperuser.*;
import android.preference.*;
import android.content.*;
import com.uday.android.util.*;
import android.os.*;
import com.uday.android.toolkit.ui.*;
import android.widget.*;

public class TermListener implements Shell.OnCommandLineListener
{
	private AndroidImagesFragment fragment;
	
	public TermListener(AndroidImagesFragment fragment){
		this.fragment=fragment;
	}
	
	@Override
	public void onCommandResult(int commandcode,final int exitcode){
		((Activity)fragment.getContext()).runOnUiThread(new Runnable(){
				@Override
				public void run(){
					fragment.termProgress.setVisibility(View.GONE);
					fragment.termDialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
					fragment.termDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
					fragment.refreshList();
					if(exitcode==0){
						fragment.termDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
							@Override public void onClick(View p1){
								fragment.termDialog.cancel();
								switch(fragment.mOption){
									case fragment.SELECTED_UNPACK:Utils.openFolder(fragment.getContext(),Environment.getDataDirectory().getAbsolutePath()+"/local/ToolKit/"+fragment.pName);
										break;
									case fragment.SELECTED_REPACK:Utils.openFolder(fragment.getContext(),Environment.getDataDirectory().getAbsolutePath()+"/local/ToolKit/"+fragment.choosen);
										break;
								}
							}
						});
					}
					else{
						fragment.termTextView.append("\nSomething went wrong,\nOperation failed...!!");
						fragment.termDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
						fragment.termDialog.getButton(DialogInterface.BUTTON_POSITIVE).setText("finish");
					}
				}
			});
	}
	@Override
	public void onLine(final String line){
		((Activity)fragment.getContext()).runOnUiThread(new Runnable(){
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
