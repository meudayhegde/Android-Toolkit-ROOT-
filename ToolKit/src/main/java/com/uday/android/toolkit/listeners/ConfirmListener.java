package com.uday.android.toolkit.listeners;

import android.content.*;
import android.util.*;
import android.view.inputmethod.*;
import com.uday.android.toolkit.*;
import com.uday.android.toolkit.fragments.*;
import com.uday.android.toolkit.ui.*;
import android.app.*;
import android.widget.*;

public class ConfirmListener implements DialogUtils.OnClickListener
{
	private AndroidImagesFragment fragment;
	public ConfirmListener(AndroidImagesFragment fragment){
		this.fragment=fragment;
	}
	
	@Override
	public void onClick(final AlertDialog utils){
		utils.cancel();
		switch(fragment.mOption){
			case fragment.SELECTED_REPACK:
				Object[] obj=DialogUtils.showTermDialog(fragment.context,"Repack img","Repacking project please wait...","open folder","finish");
				fragment.termDialog=(AlertDialog)obj[0];
				fragment.termTextView=(TextView)obj[1];
				fragment.termProgress=(ProgressBar)obj[2];
				new Thread(fragment.executer).start();
				break;
			case fragment.SELECTED_INSTALL:
				fragment.pDialog=new ProgressDialog(fragment.context).show(fragment.context,"Installing","Installing selected "+fragment.choosen+"\nplease wait...",false,false);
				new Thread(fragment.executer).start();
				break;
			case fragment.SELECTED_BACKUP:
				fragment.pDialog=new ProgressDialog(fragment.context).show(fragment.context,"Backup","Backup process in progress please wait...",false,false);
				new Thread(fragment.executer).start();
				break;
			case fragment.SELECTED_RESTORE:
				fragment.pDialog=new ProgressDialog(fragment.context).show(fragment.context,"Restoring","Restoring "+fragment.BLOCK_NAME.toUpperCase()+".\nplease wait...",false,false);
				new Thread(fragment.executer).start();
				break;
			case fragment.SELECTED_RESTORE_ITEM:
				fragment.pDialog=new ProgressDialog(fragment.context).show(fragment.context,"Restoring","Restoring "+fragment.BLOCK_NAME.toUpperCase()+".\nplease wait...",false,false);
				new Thread(fragment.executer).start();
		}
	}
}
