package com.uday.android.toolkit.runnable;


import com.uday.android.toolkit.*;
import com.uday.android.toolkit.fragments.*;
import java.io.*;

public class KernelAction implements Runnable
{
	private KernelFragment fragment;
	public KernelAction(KernelFragment fragment){
		this.fragment=fragment;
	}
	
	
	@Override
	public void run(){
		switch(fragment.mOption){
			case fragment.SELECTED_BACKUP:
				File backup=new File(fragment.DIR.getAbsolutePath()+"/backups/Kernel/"+fragment.kernVersion);
				backup.mkdirs();
				MainActivity.rootSession.addCommand(fragment.context.getFilesDir()+"/common/kern_backup.sh "+MainActivity.TOOL+" "+fragment.BLOCK+" '"+backup.getAbsolutePath()+"'",fragment.SELECTED_BACKUP,fragment.commandLineListener);
				
				break;
			case fragment.SELECTED_RESTORE:
				MainActivity.rootSession .addCommand(fragment.context.getFilesDir()+"/common/kern_install.sh "+MainActivity.TOOL+" "+fragment.BLOCK+" '"+fragment.selected.getAbsolutePath()+"/kernel' false",fragment.SELECTED_RESTORE,fragment.commandLineListener);
				
				break;
			case fragment.SELECTED_INSTALL:
				MainActivity.rootSession.addCommand(fragment.context.getFilesDir()+"/common/kern_install.sh "+MainActivity.TOOL+" "+fragment.BLOCK+" "+'"'+fragment.file.getAbsolutePath()+'"'+" "+fragment.isImg,fragment.SELECTED_INSTALL,fragment.commandLineListener);
				
				break;
		}
	}
	
}
