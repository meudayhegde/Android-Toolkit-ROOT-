package com.uday.android.toolkit.listeners;
import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import com.github.angads25.filepicker.view.*;
import com.uday.android.toolkit.fragments.*;
import java.io.*;
import com.uday.android.toolkit.ui.*;
import android.text.*;

import com.uday.android.toolkit.R;

public class AndImgListListener implements DialogInterface.OnClickListener
{
	private AndroidImagesFragment fragment;
	private RadioGroup group;
	public AndImgListListener(AndroidImagesFragment fragment,RadioGroup group){
		this.fragment=fragment;
		this.group=group;
	}
	
	@Override
	public void onClick(DialogInterface p1,int which){
		p1.cancel();
		fragment.backupDir=new File(fragment.DIR.getAbsolutePath()+"/backups");
		switch(group.getCheckedRadioButtonId()){
			case R.id.boot_selection:fragment.BLOCK=fragment.BOOT;fragment.BLOCK_NAME="boot.img";fragment.choosen="Boot Image";
				fragment.backupDir=new File(fragment.backupDir.getAbsolutePath()+"/Boot");break;
			case R.id.recovery_selection:fragment.BLOCK=fragment.RECOVERY;fragment.BLOCK_NAME="recovery.img";fragment.choosen="Recovery Image";
				fragment.backupDir=new File(fragment.backupDir.getAbsolutePath()+"/Recovery");break;
			case R.id.logo_selection:fragment.BLOCK=fragment.LOGO;fragment.BLOCK_NAME="logo.bin";fragment.choosen="Boot Logo";
				fragment.backupDir=new File(fragment.backupDir.getAbsolutePath()+"/Logo");			
		}
		
		switch (fragment.mOption){
			case fragment.SELECTED_BACKUP:
				DialogUtils.showConfirmDialog(fragment.getContext(),"Backup "+fragment.choosen,null,Html.fromHtml("Do you want to <b>backup</b> current <b>"+fragment.choosen+"</b>..?"),"confirm",fragment.mListener);
				break;
			case fragment.SELECTED_RESTORE:
				if(!fragment.backupDir.exists())fragment.backupDir.mkdirs();
					final String[] list=fragment.backupDir.list();
				new AlertDialog.Builder(fragment.getContext())
							.setTitle("Restore "+fragment.BLOCK_NAME)
					.setAdapter(new ArrayAdapter(fragment.getContext(),
														android.R.layout.simple_list_item_1,list) {
												@Override
												public View getView(int pos, View view, ViewGroup parent) {
													view = super.getView(pos, view, parent);
													((TextView) view).setSingleLine(false);
													return view;
												}
										},new DialogInterface.OnClickListener(){
											@Override
											public void onClick(DialogInterface p1,int p2){
												p1.cancel();
												fragment.choosen=list[p2];
												DialogUtils.showConfirmDialog(fragment.getContext(),"Restore","are you sure you want to restore "+fragment.choosen,null,"confirm",fragment.mListener);
											}
										})
							.show();
				fragment.mOption=fragment.SELECTED_RESTORE_ITEM;
				break;
				
			case fragment.SELECTED_INSTALL:
				fragment.properties.extensions=new String[]{".bin",".img"};
				fragment.dialog = new FilePickerDialog(fragment.getContext(),fragment.properties,R.style.AppTheme);
				fragment.dialog.setTitle("Select an img file to unpack");
				fragment.dialog.setDialogSelectionListener(fragment.fileListener);
				fragment.dialog.show();
				break;
			case fragment.SELECTED_RESTORE_ITEM:
				DialogUtils.showConfirmDialog(fragment.getContext(),"Restore "+fragment.BLOCK_NAME,null,Html.fromHtml("Are you sure you want to <b>restore</b> <br><b>"+fragment.choosen+"</b>..?"),"confirm",fragment.mListener);
		}
		
	}
}
