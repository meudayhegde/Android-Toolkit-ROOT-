
package com.uday.android.toolkit.ui;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.uday.android.toolkit.*;
import com.uday.android.util.*;
import eu.chainfire.libsuperuser.*;
import java.io.*;
import java.util.*;

import com.uday.android.toolkit.R;
import android.view.View.*;

public class EnvSetup
{
	private Context context;
	private Dialog fullScreen;
	private AlertDialog agreement;
	private ProgressDialog pDialog;
	private SharedPreferences prefs;
	private SharedPreferences.Editor edit;
	private boolean isFirstRun;
	private String abi;
	private boolean hasBusyBox;
	private int currentVersionCode,previousVersionCode;
	
	public void requestPermissions(String[] permissions,int requestCode){
		((Activity)context).requestPermissions(permissions,requestCode);
	}
	
	public void finishActivity(){
		((Activity)context).finish();
	}
	public void runOnMainThread(Runnable action){
		((Activity)context).runOnUiThread(action);
	}
	
	public EnvSetup(Context context){
		this.context=context;
		fullScreen=new Dialog(context,R.style.FullScreen);
		fullScreen.setCancelable(false);
		fullScreen.requestWindowFeature(Window.FEATURE_NO_TITLE);
		fullScreen.show();
		
		
		pDialog=new ProgressDialog(context);
		pDialog.setTitle("Please wait...");
		pDialog.setCancelable(false);
		pDialog.setMessage("Obtaining root access...");
		pDialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;

		prefs=context.getSharedPreferences("general",0);
		edit=prefs.edit();
		isFirstRun=prefs.getBoolean("isFirstRun", true);
		abi=prefs.getString("abi","");
		previousVersionCode=prefs.getInt("versionCode",1);
		
		try{
		currentVersionCode=context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionCode;
		}catch(PackageManager.NameNotFoundException ex){
			currentVersionCode=previousVersionCode+1;
		}
		
		if(isFirstRun){
			ShowAgreement();
		}
		else if(currentVersionCode>previousVersionCode){
			pDialog.show();
			FirstRunSettup();
		}
		else{
		
			pDialog.show();
			new Thread(){
				@Override
				public void run(){
					obtainRootShell();
				}
			}.start();
		}
		
	}

	private void clearData(){
		try{
		for(File file:new File(context.getFilesDir().getAbsolutePath()+"/common/").listFiles()){
			if(file.exists() && !file.isDirectory()){
				file.delete();
			}
		}
		}
		catch(Exception ex){
			Log.d(MainActivity.TAG,"Fresh installation");
		}
	}
	
	private void ShowAgreement(){
		TextView AgreeTxt=new TextView(context);
		AgreeTxt.setText(Html.fromHtml(Utils.getStringFromInputStream(context.getResources().openRawResource(R.raw.agreement))));
		AgreeTxt.setPadding(30,0,15,0);
		agreement=new AlertDialog.Builder(context)
			.setPositiveButton("agree",new DialogInterface.OnClickListener(){
					@Override
					public void onClick(DialogInterface p1,int p2){
						FirstRunSettup();
						p1.cancel();
					}
				})
			.setNegativeButton("exit",new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1,int p2){
					finishActivity();
				}
			})
			.setTitle("Terms & Conditions")
			.setView(AgreeTxt)
			.show();
		agreement.setCancelable(false);
	}
	
	private void FirstRunSettup(){
		
		for(String test:Build.SUPPORTED_ABIS){
			switch(test){
				case "armeabi":abi="arm";break;
				case "armeabi-v7a":abi="arm";break;
				case "armeabi-v7a-hard":abi="arm";break;
				case "arm64-v8a":abi="arm64";break;
				case "x86_64":abi="x64";break;
				case "x86":abi="x86";break;
			}
			if(!abi.equalsIgnoreCase("")){
				break;
			}
		}
		if(abi.equalsIgnoreCase("")){
			Toast.makeText(context,"Unsupported Architecture...",Toast.LENGTH_LONG).show();
			finishActivity();
		}

		edit.putString("abi",abi);
		
		pDialog.show();
			clearData();
			Utils.copyAsset(context.getAssets(),  "utils.tar.xz",  context.getFilesDir().getAbsolutePath());
			Utils.unpackXZ(new File(context.getFilesDir().getAbsolutePath()+"/utils.tar.xz"),false);
		//	Utils.copyAsset(context.getAssets(), abi+".zip",  context.getFilesDir().getAbsolutePath());
			edit.putInt("versionCode",currentVersionCode);
			edit.apply();
			if(!isFirstRun)
				Toast.makeText(context,"Application updated",Toast.LENGTH_SHORT).show();
		obtainRootShell();
	}

//###############################################################################################

	private void error(String title,String message,String btn,DialogInterface.OnClickListener listener){
		pDialog.cancel();
		
		DialogInterface.OnClickListener exit=new DialogInterface.OnClickListener(){
			@Override
			public void onClick(DialogInterface p1,int p2){
				finishActivity();
			}
		};
		AlertDialog.Builder errorBuilder;
		if(btn==null) btn="exit";
		errorBuilder=new AlertDialog.Builder(context)
			.setTitle(title)
			.setMessage(message);
		if(listener==null)	
			errorBuilder.setPositiveButton(btn,exit);
		else
			errorBuilder.setPositiveButton(btn,listener)
			.setNegativeButton("exit",exit);
		
		AlertDialog error=errorBuilder.create();
		error.setCancelable(false);
		error.getWindow().getAttributes().windowAnimations = R.style.DialogTheme;
		error.show();
	}
	
	private void saveBlockdevs(){
		Shell.OnCommandResultListener listener=new Shell.OnCommandResultListener(){
			@Override
			public void onCommandResult(int commandCode, int exitCode,final List<String> output) {
				if (exitCode < 0) {
					Log.e(MainActivity.TAG,"Root access failed"+Utils.getString(output));
				} else {
					Log.i(MainActivity.TAG,Utils.getString(output));
					prefs=context.getSharedPreferences("block_devs",0);
					edit=prefs.edit();
					try{
						for(String Str: output){
						edit.putString(Str.split(" ")[0],Str.split(" ")[1]);
						edit.apply();
					}
					}
					catch(Exception ex){
						runOnMainThread(new Runnable(){
							@Override
							public void run(){
								Toast.makeText(context,"Failed to detect block devices\nPlease consider manually setting block devices.",Toast.LENGTH_SHORT).show();
							}
						});
					}
					NormalStartup();
				}
			}
		};
		try{
			pDialog.setMessage("Enumerating partitions...");
			MainActivity.rootSession.addCommand("cd "+context.getFilesDir().getAbsolutePath()+"\n"+
												MainActivity.TOOL+" tar -xvf utils.tar\n"+
												MainActivity.TOOL+" ls | while read file ; do\n"+
													"if [ $file != "+abi+" ] && [ $file != common ]; then\n"+
														MainActivity.TOOL+" rm -rf $file\n"+
													"fi\n"+
												"done\n"+
												MainActivity.TOOL+" mv "+abi+" bin\n"+
												MainActivity.TOOL+" mv common/parted bin/parted\n"+
												MainActivity.TOOL+" chmod 755 bin/* common/*");
			MainActivity.rootSession.addCommand(context.getFilesDir()+"/common/find_blockdev.sh "+MainActivity.TOOL,0,listener);
		}catch(Exception ex){
			Log.e(MainActivity.TAG,ex.toString());
		}
	}
	
//###############################################################################################
	
	private void obtainRootShell(){
		if(MainActivity.rootSession==null){
			MainActivity.rootSession=new Shell.Builder().
				useSU().
				setWantSTDERR(true).
				setWatchdogTimeout(0).
				setMinimalLogging(false).
				open(new Shell.OnCommandResultListener() {

					// Callback to report whether the shell was successfully started up 
					@Override
					public void onCommandResult(int commandCode, int exitCode,final List<String> output) {
						// note: this will FC if you rotate the phone while the dialog is up
						if (exitCode != 0) {
							Log.e(MainActivity.TAG,"error obtaining root shell "+exitCode);
							MainActivity.rootSession=null;
							runOnMainThread(new Runnable(){
									@Override
									public void run(){
										try{
											if(!Utils.findInPath("su")){
												error("Oops...","Root not found...!!\n"+Utils.getString(output),null,null);
												CustomToast.showFailureToast(context,"Root not found...!!",Toast.LENGTH_SHORT);
											}else{
												error("Oops...","Root access failed...!!\n"+Utils.getString(output),null,null);
												CustomToast.showFailureToast(context,"Root access failed...!!",Toast.LENGTH_SHORT);
											}
										}catch(Exception ex){
											Log.e(MainActivity.TAG,ex.toString());
											error("Oops...","Root not found...!!\n"+Utils.getString(output),null,null);
											CustomToast.showFailureToast(context,"Root not found...!!",Toast.LENGTH_SHORT);
										}
											
									}
								});

						} else {
							// Shell is up: send our first request 
							Log.i(MainActivity.TAG,"Root shell successfully obtained "+exitCode);
							
							CustomToast.showSuccessToast(context,"Root shell successfully obtained ",Toast.LENGTH_SHORT);
							checkForBusyBox();
						}
					}
				});
		}
		else
			checkForBusyBox();
	}

	public void checkForBusyBox(){
		hasBusyBox=false;
		if(Utils.findInPath("busybox")){hasBusyBox=true;MainActivity.TOOL="busybox";}
		else if(Utils.findInPath("toybox")){hasBusyBox=true;MainActivity.TOOL="toybox";CustomToast.showNotifyToast(context,"busybox not found, we use toybox,\nplease consider installing busybox in case of any incoveniences.",Toast.LENGTH_SHORT);}
		else if(Utils.findInPath("toolbox")){hasBusyBox=true;MainActivity.TOOL="toolbox";CustomToast.showNotifyToast(context,"busybox not found, we use toolbox,\nplease consider installing busybox in case of any incoveniences.",Toast.LENGTH_SHORT);}
		runOnMainThread(new Runnable(){
			@Override
			public void run(){
				if(!hasBusyBox){
					error("Oops...","busybox not found","Install",new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface p1,int p2){
							try{
								Intent intent = new Intent(Intent.ACTION_VIEW); 
								intent.setData(Uri.parse("market://details?id=ru.meefik.busybox")); 
								context.startActivity(intent);
								finishActivity();
							}catch(Exception ex){
								Log.e(MainActivity.TAG,ex.toString());
								CustomToast.showFailureToast(context,"No market is found\nplease manually install busybox to continue",Toast.LENGTH_SHORT);
							}
						}
					});
					
				}
				else{
					if(!((MainActivity)context).opened){
						((MainActivity)context).opened=true;
						if(!isFirstRun)
							NormalStartup();
						else
							saveBlockdevs();
					}
				}
			}
		});
	}
	
	
	public void NormalStartup(){
		runOnMainThread(new Runnable(){
				@Override
				public void run(){
					if (android.os.Build.VERSION.SDK_INT > 22 && ( context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED || context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED )) {
						pDialog.setMessage("Obtaining storage permissions");
						requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},69243);
					}
					else{
						context.getSharedPreferences("general",0).edit().putBoolean("isFirstRun",false).apply();
						onStartup();
					}
				}

			});
	}
//###############################################################################################
	
	public void onStartup(){
		pDialog.dismiss();
		fullScreen.dismiss();
	}
	
//###############################################################################################
	
	public void onRequestPermissionsResult(int requestCode,
										   String permissions[], int[] grantResults) {
		pDialog.dismiss();
		switch (requestCode) {
			case 69243: 
				if (grantResults.length > 0
					&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					context.getSharedPreferences("general",0).edit().putBoolean("isFirstRun",false).apply();
					onStartup();
				} else {
					Toast.makeText(context,
								   "This app will not work unless you grant permissions..!!",
								   Toast.LENGTH_LONG).show();
					error("Oops...","Storage permissions Denied..!!",null,null);
					return;
				}

				// other 'case' lines to check for other
				// permissions this app might request
		}
	}
	
}
