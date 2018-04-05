package com.uday.android.toolkit.listeners;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.uday.android.toolkit.*;
import com.uday.android.toolkit.ui.*;
import com.uday.android.util.*;
import eu.chainfire.libsuperuser.*;
import java.util.*;
import android.view.View.*;
import com.uday.android.toolkit.fragments.*;

public class DeleteClickListener implements DialogUtils.OnClickListener{

	private ApkListData apkListData;
	private int position;
	private ApkListAdapter adapter;
	private Context context;
	private Animation anim;
	private Shell.OnCommandResultListener commandResultListener;
	private View row;

	private static final Shell.Interactive rootSession=MainActivity.rootSession;

	public DeleteClickListener(final ApkListAdapter adapter){
		this.adapter=adapter;
		this.context=adapter.getContext();
		anim = AnimationUtils.loadAnimation(
			context, android.R.anim.slide_out_right
		);
		anim.setDuration(500);

		commandResultListener=new Shell.OnCommandResultListener(){
			@Override
			public void onCommandResult(int commandCode,int exitCode,List<String> output){
				((Activity)context).runOnUiThread(new Runnable(){
						@Override
						public void run(){
							if(!apkListData.apkFile.exists()){
								row.startAnimation(anim);
								new Handler().postDelayed(new Runnable(){
										@Override
										public void run(){
											adapter.remove(apkListData);
											BatchInstallerFragment.apkFilesOrig.remove(apkListData);
										}
									},400);
								CustomToast.showSuccessToast(context,apkListData.PATH+" is deleted",Toast.LENGTH_SHORT);
							}
							else
								CustomToast.showFailureToast(context,"failed to delete"+apkListData.PATH,Toast.LENGTH_SHORT);

						}
					});
			}
		};
	}

	@Override
	public void onClick(final AlertDialog sweet)
	{
		sweet.cancel();
		AlertDialog dialog=DialogUtils.showConfirmDialog(context,"Are you sure..?","do you want to permanently delete\n"+apkListData.PATH+"..?",null,"confirm",new DialogUtils.OnClickListener(){
										@Override
										public void onClick(AlertDialog p1){
											rootSession.addCommand(MainActivity.TOOL+" rm -f "+'"'+apkListData.PATH+'"',position,commandResultListener);
											p1.cancel();
										}
									});
		dialog.setIcon(apkListData.ICON);
	}

	public DeleteClickListener setPosition(int position,View row){
		this.position=position;
		this.apkListData=adapter.getItem(position);
		this.row=row;
		return this;
	}
}
