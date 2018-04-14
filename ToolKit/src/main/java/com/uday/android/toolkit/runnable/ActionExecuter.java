package com.uday.android.toolkit.runnable;
import android.app.*;
import android.content.*;
import android.util.*;
import com.uday.android.toolkit.*;
import com.uday.android.toolkit.fragments.*;
import com.uday.android.util.*;
import eu.chainfire.libsuperuser.*;
import java.io.*;
import java.util.*;
import com.uday.android.toolkit.ui.*;
import android.view.*;
import android.widget.*;

public class ActionExecuter implements Runnable
{
	private Context context;
	private AndroidImagesFragment fragment;
	public ActionExecuter(AndroidImagesFragment fragment){
		this.context=fragment.getContext();
		this.fragment=fragment;
		
	}
	
	@Override
	public void run(){
		switch(fragment.mOption){
			case fragment.SELECTED_UNPACK:
				try{
					fragment.tmpstr="";
					MainActivity.rootSession.addCommand(context.getFilesDir()+"/common/boot_unpack.sh "+MainActivity.TOOL+" "+'"'+fragment.fileChoosen.getAbsolutePath()+'"'+" "+fragment.pName,fragment.SELECTED_UNPACK,fragment.commandLineListener);
				}catch(Exception ex){
					Log.e(MainActivity.TAG,ex.toString()+"\n"+ex.getMessage());
					CustomToast.showFailureToast(context,ex.toString(),Toast.LENGTH_SHORT);
				}
				break;
			case fragment.SELECTED_REPACK:
				MainActivity.rootSession.addCommand(context.getFilesDir()+"/common/boot_repack.sh "+MainActivity.TOOL+" /data/local/ToolKit/"+fragment.choosen,4,fragment.commandLineListener);
				break;
			case fragment.SELECTED_INSTALL:
				MainActivity.rootSession.addCommand(MainActivity.TOOL+" dd if="+'"'+fragment.fileChoosen.getAbsolutePath()+'"'+" of="+fragment.BLOCK,fragment.SELECTED_INSTALL,new Shell.OnCommandResultListener(){
						@Override
						public void onCommandResult(int commandCode,final int exitCode,final List<String> output){
							((Activity)context).runOnUiThread(new Runnable(){
									@Override
									public void run(){
										if(fragment.pDialog!=null)fragment.pDialog.cancel();
										if(exitCode==0){
											DialogUtils.showConfirmDialog(context,"Success","Installation completed,\nreboot the device for the changes to take effect...",null,"close",null).getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
											CustomToast.showSuccessToast(context,"installation successful",Toast.LENGTH_SHORT);
										}
										else{
											DialogUtils.showConfirmDialog(context,"Failed","Installation Failed...",null,"dismiss",null).getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
											CustomToast.showFailureToast(context,Utils.getString(output),Toast.LENGTH_SHORT);
										}
									}
								});
						}
					});
				break;
			case fragment.SELECTED_BACKUP:
				fragment.backupDir=new File(fragment.backupDir.getAbsolutePath()+"/"+Calendar.getInstance().getTime().toLocaleString().replace(" ","_").replace(",","").replace(":",""));
				fragment.backupDir.mkdirs();
				MainActivity.rootSession.addCommand(MainActivity.TOOL+" mkdir -p '"+fragment.backupDir+"'\n"
													+MainActivity.TOOL+" dd if="+fragment.BLOCK+" of="+fragment.backupDir.getAbsolutePath()+"/"+fragment.BLOCK_NAME,fragment.SELECTED_BACKUP,new Shell.OnCommandResultListener(){
					@Override
					public void onCommandResult(int commandCode,final int exitCode,final List<String> output){
						fragment.runOnUiThread(new Runnable(){
								@Override
								public void run(){
									if(fragment.pDialog!=null)fragment.pDialog.cancel();
									if(exitCode==0){
										DialogUtils.showConfirmDialog(context,"Success","Backup completed.\nyou can find backup in\n"+fragment.backupDir.getAbsolutePath(),null,"close",null).getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
										CustomToast.showSuccessToast(context,"Successfully backed up",Toast.LENGTH_SHORT);
									}
									else{
										DialogUtils.showConfirmDialog(context,"Failed","Failed to backup\nCheck log for more info...",null,"close",null).getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
										CustomToast.showFailureToast(context,Utils.getString(output),Toast.LENGTH_SHORT);
									}
								}
							});
					}
				});
		
	break;
		case fragment.SELECTED_RESTORE_ITEM:
				MainActivity.rootSession.addCommand(MainActivity.TOOL+" dd if="+fragment.backupDir+"/"+fragment.choosen+"/"+fragment.BLOCK_NAME+" of="+fragment.BLOCK,fragment.SELECTED_BACKUP,new Shell.OnCommandResultListener(){
						@Override
						public void onCommandResult(int commandCode,final int exitCode,final List<String> output){
							fragment.runOnUiThread(new Runnable(){
									@Override
									public void run(){
										if(fragment.pDialog!=null)fragment.pDialog.cancel();
										
										if(exitCode==0){
											DialogUtils.showConfirmDialog(context,"Success","Restore completed.\nReboot the device for changes to take effect...",null,"close",null).getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
											CustomToast.showSuccessToast(context,Utils.getString(output),Toast.LENGTH_SHORT);
										}
										else{
											DialogUtils.showConfirmDialog(context,"Failed","Failed to Restore\nCheck log for more info...",null,"close",null).getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
											CustomToast.showFailureToast(context,Utils.getString(output),Toast.LENGTH_SHORT);
										}
									}
								});
						}
					});
				break;
			}
	}
}
