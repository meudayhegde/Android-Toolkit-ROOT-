package com.uday.android.util;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.*;
import java.io.*;
import com.uday.android.toolkit.*;

public class ApkListData
{
	public File apkFile;
	public String PATH;
	public String NAME;
	public String VERSION_NAME="Invalid apk file";
	public int VERSION_CODE=0;
	public Drawable ICON;
	public boolean isSelectable=false,isSelected=false;
	public String SIZE;
	public String PACKAGE_NAME="parse error..!";
	public int titleColor=Color.BLACK;
	public boolean isInstalled=false,isOld=false,isInstalledVer=false;
	public String txtSearch;
	
	private PackageInfo pi;

	public ApkListData(File apkFile,final PackageManager pm,Drawable icDefault){
		this.apkFile=apkFile;
		PATH=apkFile.getAbsolutePath();
		NAME=apkFile.getName();
		ICON=icDefault;
		SIZE=Utils.getSize(apkFile);
		try{
		pi=pm.getPackageArchiveInfo(PATH,0);
		pi.applicationInfo.sourceDir       = PATH;
		pi.applicationInfo.publicSourceDir = PATH;
			NAME=pi.applicationInfo.loadLabel(pm).toString();
			VERSION_NAME=pi.versionName;
			VERSION_CODE=pi.versionCode;
			ICON=pi.applicationInfo.loadIcon(pm);
			PACKAGE_NAME=pi.packageName;
			if(!PACKAGE_NAME.equals("com.uday.android.toolkit"))
				isSelectable=true;
		
				ApplicationInfo info = pm.getApplicationInfo(  
					PACKAGE_NAME, PackageManager.GET_UNINSTALLED_PACKAGES);
				isInstalled=true;
				Log.d(MainActivity.TAG,  "checkAppInstalledByName: "+PACKAGE_NAME+" : found");
				PackageInfo pInfo=pm.getPackageInfo(PACKAGE_NAME,0);
				if(pInfo.versionCode==VERSION_CODE){
					isInstalledVer=true;
				}else if(pInfo.versionCode>VERSION_CODE) isOld=true;
		
			
		}catch(Exception ex){
		//	Log.d(MainActivity.TAG,ex.toString());
		//	Log.d(MainActivity.TAG,  "checkAppInstalledByName:"+PACKAGE_NAME+" not found");
			isInstalled=false;
		}
	
	}
	
}
