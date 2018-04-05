package com.uday.android.toolkit.listeners;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.widget.*;
import com.uday.android.toolkit.*;
import com.uday.android.toolkit.ui.*;
import com.uday.android.util.*;
import eu.chainfire.libsuperuser.*;
import java.util.*;

import com.uday.android.toolkit.R;
import android.view.*;
import android.view.View.*;

public class InstallClickListener implements DialogUtils.OnClickListener{
	
	private ApkListData apkListData;
	private int position;
	private ApkListAdapter adapter;
	private Context context;
	private DialogUtils.OnClickListener launcher;
	private Shell.OnCommandResultListener commandResultListener;
	private AlertDialog sweet;
	private ProgressDialog pDialog;
	
	
	private static final Shell.Interactive rootSession=MainActivity.rootSession;
	
	public InstallClickListener(final ApkListAdapter adapter){
		this.adapter=adapter;
		this.context=adapter.getContext();
		
		launcher=new DialogUtils.OnClickListener(){
			@Override
			public void onClick(AlertDialog p1){
				p1.cancel();
				try{
				Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(apkListData.PACKAGE_NAME);
				if (launchIntent != null) { 
					context.startActivity(launchIntent);//null pointer check in case package name was not found
				}
				}catch(Exception ex){
					CustomToast.showFailureToast(context,"Failed to launch "+apkListData.NAME,Toast.LENGTH_SHORT);
					Log.e(MainActivity.TAG,ex.toString());
				}
			}
		};
		
		commandResultListener=new Shell.OnCommandResultListener(){
			@Override
			public void onCommandResult(int coomandcode,final int exitcode,final List<String> output){
				((Activity)context).runOnUiThread(new Runnable(){
						@Override
						public void run(){
							pDialog.cancel();
							AlertDialog dialog;
							if(exitcode==0){
								apkListData.isInstalled=true;
								apkListData.titleColor=Color.rgb(0,202,0);
								apkListData.isInstalledVer=true;
								dialog=DialogUtils.showConfirmDialog(context,"Installation Success",apkListData.NAME+"_"+apkListData.VERSION_NAME+" Successfully installed...",null,"Launch",launcher);
								dialog.setIcon(apkListData.ICON);
								CustomToast.showSuccessToast(context,apkListData.NAME+"_"+apkListData.VERSION_NAME+" Successfully installed...",Toast.LENGTH_SHORT);
							}
							else{
								Log.e(MainActivity.TAG,Utils.getString(output));
								apkListData.titleColor=Color.rgb(255,25,0);
								apkListData.isInstalled=false;
								dialog=DialogUtils.showConfirmDialog(context,"Installation failed","Failed to install "+apkListData.NAME+"_"+apkListData.VERSION_NAME,null,null,null);
								dialog.setIcon(apkListData.ICON);
								CustomToast.showFailureToast(context,"Failed to install "+apkListData.NAME+"_"+apkListData.VERSION_NAME+"\n"+Utils.getString(output),Toast.LENGTH_SHORT);
							}
							adapter.notifyDataSetChanged();
						}
					});
			}
		};
	}

	@Override
	public void onClick(AlertDialog sweet)
	{
		this.sweet=sweet;
		sweet.cancel();
		pDialog=new ProgressDialog(context);
		pDialog.setIcon(apkListData.ICON);
		pDialog.setTitle("Installing...");
		pDialog.setMessage("Installing "+apkListData.NAME+" please wait...");
		pDialog.setCancelable(false);
		pDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		pDialog.show();
		rootSession.addCommand("pm install -rd "+'"'+apkListData.PATH+'"',position,commandResultListener);
		
	}

	public InstallClickListener setPosition(int position){
		this.position=position;
		this.apkListData=adapter.getItem(position);
		return this;
	}

}
